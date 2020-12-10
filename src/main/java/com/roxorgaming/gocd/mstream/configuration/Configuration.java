package com.roxorgaming.gocd.mstream.configuration;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.roxorgaming.gocd.mstream.PipelineListener;
import in.ashwanthkumar.utils.collections.Lists;
import in.ashwanthkumar.utils.func.Function;
import in.ashwanthkumar.utils.func.Predicate;
import in.ashwanthkumar.utils.lang.StringUtils;
import in.ashwanthkumar.utils.lang.option.Option;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

import static com.roxorgaming.gocd.mstream.configuration.PipelineConfig.merge;

public class Configuration {

    private static Logger LOGGER = Logger.getLoggerFor(Configuration.class);

    private boolean enabled;
    private String webHookUrl;
    private String slackChannel;
    private String slackDisplayName;
    private String slackUserIconURL;
    private String goServerHost;
    private String goAPIServerHost;
    private String goLogin;
    private String goPassword;
    private String goAPIToken;
    private boolean displayConsoleLogLinks;
    private boolean displayMaterialChanges;
    private boolean processAllRules;
    private boolean truncateChanges;

    private Proxy proxy;

    private List<PipelineConfig> pipelineConfigs = new ArrayList<>();
    private PipelineListener pipelineListener;

    public Configuration(){}

    public Configuration(String goServerHost){
        this.goServerHost = goServerHost;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Configuration setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getWebHookUrl() {
        return webHookUrl;
    }

    public Configuration setWebHookUrl(String webHookUrl) {
        this.webHookUrl = webHookUrl;
        return this;
    }

    public String getSlackChannel() {
        return slackChannel;
    }

    public Configuration setSlackChannel(String slackChannel) {
        this.slackChannel = slackChannel;
        return this;
    }

    public String getSlackDisplayName() {
        return slackDisplayName;
    }

    private Configuration setSlackDisplayName(String displayName) {
        this.slackDisplayName = displayName;
        return this;
    }

    public String getSlackUserIcon() {
        return slackUserIconURL;
    }

    private Configuration setSlackUserIcon(String iconURL) {
        this.slackUserIconURL = iconURL;
        return this;
    }

    public List<PipelineConfig> getPipelineRules() {
        return pipelineConfigs;
    }

    public Configuration setPipelineRules(List<PipelineConfig> pipelineConfigs) {
        this.pipelineConfigs = pipelineConfigs;
        return this;
    }

    public String getGoServerHost() {
        return goServerHost;
    }

    public Configuration setGoServerHost(String goServerHost) {
        this.goServerHost = goServerHost;
        return this;
    }


    public String getGoAPIServerHost() {
        if (StringUtils.isNotEmpty(goAPIServerHost)) {
            return goAPIServerHost;
        }
        return getGoServerHost();
    }

    public Configuration setGoAPIServerHost(String goAPIServerHost) {
        this.goAPIServerHost = goAPIServerHost;
        return this;
    }

    public String getGoLogin() {
        return goLogin;
    }

    public Configuration setGoLogin(String goLogin) {
        this.goLogin = goLogin;
        return this;
    }

    public String getGoPassword() {
        return goPassword;
    }

    public Configuration setGoPassword(String goPassword) {
        this.goPassword = goPassword;
        return this;
    }

    public String getGoAPIToken() {
        return goAPIToken;
    }

    public Configuration setGoAPIToken(String goAPIToken) {
        this.goAPIToken = goAPIToken;
        return this;
    }

    public boolean getDisplayConsoleLogLinks() {
        return displayConsoleLogLinks;
    }

    public Configuration setDisplayConsoleLogLinks(boolean displayConsoleLogLinks) {
        this.displayConsoleLogLinks = displayConsoleLogLinks;
        return this;
    }

    public boolean getDisplayMaterialChanges() {
        return displayMaterialChanges;
    }

    public Configuration setDisplayMaterialChanges(boolean displayMaterialChanges) {
        this.displayMaterialChanges = displayMaterialChanges;
        return this;
    }

    public boolean getProcessAllRules() {
        return processAllRules;
    }

    public Configuration setProcessAllRules(boolean processAllRules) {
        this.processAllRules = processAllRules;
        return this;
    }

    public boolean isTruncateChanges() {
        return truncateChanges;
    }

    public Configuration setTruncateChanges(boolean truncateChanges) {
        this.truncateChanges = truncateChanges;
        return this;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public Configuration setProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    public PipelineListener getPipelineListener() {
        return pipelineListener;
    }

    public List<PipelineConfig> find(final String pipeline, final String stage, final String group, final String pipelineStatus) {
        Predicate<PipelineConfig> predicate = new Predicate<PipelineConfig>() {
            public Boolean apply(PipelineConfig input) {
                return input.matches(pipeline, stage, group, pipelineStatus);
            }
        };

        if(processAllRules) {
            return Lists.filter(pipelineConfigs, predicate);
        } else {
            List<PipelineConfig> found = new ArrayList<PipelineConfig>();
            Option<PipelineConfig> match = Lists.find(pipelineConfigs, predicate);
            if(match.isDefined()) {
                found.add(match.get());
            }
            return found;
        }
    }

    public static Configuration fromConfig(Config config) {

        boolean isEnabled = true;
        if(config.hasPath("enabled")){
            isEnabled = config.getBoolean("enabled");
        }

        String webhookUrl = config.getString("webhookUrl");
        String channel = null;
        if (config.hasPath("channel")) {
            channel = config.getString("channel");
        }

        String displayName = "gocd-slack-bot";
        if (config.hasPath("msteamsTitleName")) {
            displayName = config.getString("msteamsTitleName");
        }

        String iconURL = "https://raw.githubusercontent.com/ashwanthkumar/assets/c597777ee749c89fec7ce21304d727724a65be7d/images/gocd-logo.png";
        if (config.hasPath("activityImage")) {
            iconURL = config.getString("activityImage");
        }

        String serverHost = config.getString("server-host");
        String apiServerHost = null;
        if (config.hasPath("api-server-host")) {
            apiServerHost = config.getString("api-server-host");
        }
        String login = null;
        if (config.hasPath("login")) {
            login = config.getString("login");
        }
        String password = null;
        if (config.hasPath("password")) {
            password = config.getString("password");
        }

        String apiToken = null;
        if (config.hasPath("api-token")) {
            apiToken = config.getString("api-token");
        }

        boolean displayConsoleLogLinks = true;
        if (config.hasPath("display-console-log-links")) {
            displayConsoleLogLinks = config.getBoolean("display-console-log-links");
        }

        // TODO - Next major release - change this to - separated config
        boolean displayMaterialChanges = true;
        if (config.hasPath("displayMaterialChanges")) {
            displayMaterialChanges = config.getBoolean("displayMaterialChanges");
        }

        boolean processAllRules = false;
        if (config.hasPath("process-all-rules")) {
            processAllRules = config.getBoolean("process-all-rules");
        }

        boolean truncateChanges = true;
        if(config.hasPath("truncate-changes")) {
            truncateChanges = config.getBoolean("truncate-changes");
        }

        Proxy proxy = null;
        if (config.hasPath("proxy")) {
            Config proxyConfig = config.getConfig("proxy");
            if (proxyConfig.hasPath("hostname") && proxyConfig.hasPath("port") && proxyConfig.hasPath("type")) {
                String hostname = proxyConfig.getString("hostname");
                int port = proxyConfig.getInt("port");
                String type = proxyConfig.getString("type").toUpperCase();
                Proxy.Type proxyType = Proxy.Type.valueOf(type);
                proxy = new Proxy(proxyType, new InetSocketAddress(hostname, port));
            }
        }

        Config defaultConf = ConfigFactory.load();
        if(config.hasPath("default")){
            defaultConf = config.getConfig("default");
        }

        final PipelineConfig defaultRule = PipelineConfig.fromConfig(defaultConf, channel);

        List<PipelineConfig> pipelineConfigs = Lists.map((List<Config>) config.getConfigList("pipelines"), new Function<Config, PipelineConfig>() {
            public PipelineConfig apply(Config input) {
                return merge(PipelineConfig.fromConfig(input), defaultRule);
            }
        });

        Configuration configuration = new Configuration()
                .setEnabled(isEnabled)
                .setWebHookUrl(webhookUrl)
                .setSlackChannel(channel)
                .setSlackDisplayName(displayName)
                .setSlackUserIcon(iconURL)
                .setPipelineRules(pipelineConfigs)
                .setGoServerHost(serverHost)
                .setGoAPIServerHost(apiServerHost)
                .setGoLogin(login)
                .setGoPassword(password)
                .setGoAPIToken(apiToken)
                .setDisplayConsoleLogLinks(displayConsoleLogLinks)
                .setDisplayMaterialChanges(displayMaterialChanges)
                .setProcessAllRules(processAllRules)
                .setTruncateChanges(truncateChanges)
                .setProxy(proxy);
        try {
            if(config.hasPath("listener")) {
                configuration.pipelineListener = Class.forName(config.getString("listener"))
                        .asSubclass(PipelineListener.class).getConstructor(Configuration.class)
                        .newInstance(configuration);
            }
        } catch (Exception e) {
            LOGGER.error("Exception while initializing pipeline listener", e);
            throw new RuntimeException(e);
        }

        return configuration;
    }
}
