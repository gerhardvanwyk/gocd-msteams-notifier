package com.roxorgaming.gocd;

import com.roxorgaming.gocd.msteams.jsonapi.GoCdClient;
import com.roxorgaming.gocd.mstream.GoEnvironment;
import com.roxorgaming.gocd.mstream.MsTeamsPipelineListener;
import com.roxorgaming.gocd.mstream.PipelineListener;
import com.roxorgaming.gocd.mstream.base.AbstractNotificationPlugin;
import com.roxorgaming.gocd.mstream.base.Utils;
import com.roxorgaming.gocd.mstream.configuration.ConfigReader;
import com.roxorgaming.gocd.mstream.configuration.Configuration;
import com.roxorgaming.gocd.mstream.notification.PipelineInfo;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import in.ashwanthkumar.utils.lang.StringUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.Arrays.asList;

@Extension
public class GoNotificationPlugin extends AbstractNotificationPlugin implements GoPlugin {

    private static Logger LOGGER = Logger.getLoggerFor(GoNotificationPlugin.class);

    private static final long CONFIG_REFRESH_INTERVAL = 10 * 1000; // 10 seconds

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private GoEnvironment environment = new GoEnvironment();
    private Configuration configuration;
    private PipelineListener pipelineListener;

    private final Timer timer = new Timer();
    private long configLastModified = 0L;
    private GoApplicationAccessor accessor;
    // private File pluginConfigFile;

    public GoNotificationPlugin() {
        File pluginConfigFile = findGoNotifyConfigPath();
        /**
         * Scheduler to read config file
         * Configuration can change at runtime. Read in the new file.
         * This code instantiate the GoCD API client, and is not tested.
         */
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
            if (pluginConfigFile.lastModified() != configLastModified) {
                if (configLastModified == 0L) {
                    LOGGER.info("Loading configuration file");
                } else {
                    LOGGER.info("Reloading configuration file since some modifications were found");
                }
                try {
                    lock.writeLock().lock();
                    configuration = ConfigReader.read(pluginConfigFile);
                    pipelineListener = new MsTeamsPipelineListener(new GoCdClient(configuration));
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                } finally {
                    lock.writeLock().unlock();
                }
                configLastModified = pluginConfigFile.lastModified();
            }
            }
        }, 0, CONFIG_REFRESH_INTERVAL);
    }

    // used for tests
    public GoNotificationPlugin(GoEnvironment environment) {
        this.environment = environment;
    }

    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
        this.accessor = goApplicationAccessor;
    }

    public GoPluginApiResponse handle(GoPluginApiRequest goPluginApiRequest) {
        String requestName = goPluginApiRequest.requestName();

        LOGGER.info(requestName);

        if (requestName.equals("notifications-interested-in")) {
            return handleNotificationsInterestedIn();

        } else if (requestName.equals("stage-status")) {
            return handleStageNotification(goPluginApiRequest);

        } else if (requestName.equals("go.processor.plugin-settings.get")){
           return handleGetPluginSettings();

        } else if (requestName.equals("go.plugin-settings.get-view")) {
            return handleRequestGetView();

        } else if (requestName.equals("go.plugin-settings.validate-configuration")) {
            return handleValidateConfig(goPluginApiRequest.requestBody());

        } else if (requestName.equals("go.plugin-settings.get-configuration")) {
            return handleRequestGetConfiguration();
        }
        return null;
    }

    /**
     * Here we return the configuration values. This needs to be
     * called explicitly by the PluginUI
     * @return
     */
    private GoPluginApiResponse handleGetPluginSettings() {
        List<Object> response = Arrays.asList();
        return renderJSON(200, response);
    }


    /**
     * Here we validate any specific configuration values
     * @param requestBody
     * @return
     */
    private GoPluginApiResponse handleValidateConfig(String requestBody) {
        LOGGER.info("Request validateConfiguration " + requestBody);
        List<Object> response = Arrays.asList();
        return renderJSON(200, response);
    }


    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier("notification",  asList("1.0"));
    }


    private GoPluginApiResponse handleRequestGetView() {
        Map<String, Object> response = new HashMap<>();

        try {
            String template = IOUtils.toString(getClass().getResourceAsStream("/views/config.template.html"), "UTF-8");
            response.put("template", template);
        } catch (IOException e) {
            response.put("error", "Can't load view template");
            return renderJSON(500, response);
        }
        return renderJSON(200, response);
    }

    /**
     * Here we need to create the Settings Configuration Object
     * - This is used to create the schema in GoCD??
     * @return
     */
    private GoPluginApiResponse handleRequestGetConfiguration() {
        return renderJSON(200, configField("configuration", configuration, "0", true, false));
    }

    private GoPluginApiResponse handleNotificationsInterestedIn() {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("notifications", Arrays.asList("stage-status"));
        return renderJSON(200, response);
    }

    private GoPluginApiResponse handleStageNotification(GoPluginApiRequest goPluginApiRequest) {
        //Get the Pipeline Info as part of notification
        PipelineInfo pipelineInfo = parseNotificationMessage(goPluginApiRequest);
        int responseCode = 200;

        Map<String, Object> response = new HashMap<String, Object>();
        List<String> messages = new ArrayList<String>();
        try {
            response.put("status", "success");
            LOGGER.info(pipelineInfo.fullyQualifiedJobName() + " has " + pipelineInfo.getStage().getName() + "/" + pipelineInfo.getStage().getResult());

            //lock reading any other configuration
            lock.readLock().lock();

            this.pipelineListener.notify(pipelineInfo);
        } catch (Exception e) {
            LOGGER.info(pipelineInfo.fullyQualifiedJobName() + " failed with error", e);
            responseCode = 500;
            response.put("status", "failure");
            if (!isEmpty(e.getMessage())) {
                messages.add(e.getMessage());
            }
        } finally {
            lock.readLock().unlock();
        }

        if (!messages.isEmpty()) {
            response.put("messages", messages);
        }
        return renderJSON(responseCode, response);
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private PipelineInfo parseNotificationMessage(GoPluginApiRequest goPluginApiRequest) {
        return Utils.getMapper().convertValue(goPluginApiRequest.requestBody(), PipelineInfo.class);
    }

    /**
     * Finds the configuration file.
     * @return
     */
    private File findGoNotifyConfigPath() {
        // case 1: Look for an environment variable by GO_NOTIFY_CONF and if a file identified by the value exist
        String goNotifyConfPath = environment.getenv("GO_NOTIFY_CONF");
        if (StringUtils.isNotEmpty(goNotifyConfPath)) {
            File pluginConfig = new File(goNotifyConfPath);
            if (pluginConfig.exists()) {
                LOGGER.info(String.format("Configuration file found using GO_NOTIFY_CONF at %s", pluginConfig.getAbsolutePath()));
                return pluginConfig;
            }
        }
        // case 2: Look for a file called go_notify.conf in the home folder
        File pluginConfig = new File(System.getProperty("user.home") + File.separator + "reference.conf");
        if (pluginConfig.exists()) {
            LOGGER.info(String.format("Configuration file found at Home Dir as %s", pluginConfig.getAbsolutePath()));
            return pluginConfig;
        }
        // case 3: Look for a file - go_notify.conf in the current working directory of the server
        String goServerDir = environment.getenv("CRUISE_SERVER_DIR");
        pluginConfig = new File(goServerDir + File.separator + "reference.conf");
        if (pluginConfig.exists()) {
            LOGGER.info(String.format("Configuration file found using CRUISE_SERVER_DIR at %s", pluginConfig.getAbsolutePath()));
            return pluginConfig;
        }

        throw new RuntimeException("Unable to find go_notify.conf. Please make sure you've set it up right.");
    }
}
