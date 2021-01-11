package com.roxorgaming.gocd.mstream.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigList;
import in.ashwanthkumar.utils.collections.Lists;
import in.ashwanthkumar.utils.func.Predicate;
import in.ashwanthkumar.utils.lang.option.Option;
import lombok.*;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Configuration are created form a config file via a Constructor
 *
 * The builder used during testing
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Configuration {

    private static Logger LOGGER = Logger.getLoggerFor(Configuration.class);

    /**
     * feature flag for notification plugin, turning this false will not post anything to MsTeams
     * quite useful while testing / debugging
     * @param enabled
     * @return
     */
    @JsonProperty("enabled")
    private boolean enabled;

    /**
     * MSTeams Configuration
     * ************************/

    // MS Teams Host
    @JsonProperty
    private String apiMsTeamsHost;

    @JsonProperty
    private List<MsTeamsConfig> msTeamsConfigList = new ArrayList<>();

    /*
      GoCD Server
     * *****************/
    /**
     * GoCD Server with Port
     * http://localhost:8153
     */
    @JsonProperty
    private String goServerHost;
    @JsonProperty
    private String goLogin;
    @JsonProperty
    private String goPassword;
    @JsonProperty
    private String goAPIToken;

    /*
      Optional Output Configuration
     *******************/
    @JsonProperty
    private boolean displayConsoleLogLinks;
    @JsonProperty
    private boolean displayMaterialChanges;
    @JsonProperty
    private boolean processAllRules;
    @JsonProperty
    private boolean truncateChanges;
    @JsonProperty
    private Proxy proxy;

    @Setter(AccessLevel.PRIVATE)
    private boolean errors;

    /**
     * Creating configuration from  [HOCON](https://github.com/typesafehub/config) format.
     * @param config
     */
    public Configuration(Config config){
        String error = fromConfig(config);
        if(errors){
            throw new RuntimeException(error);
        }
    }

    public List<PipelineConfig> find(final String pipeline, final String stage, final String group, final String pipelineStatus) {
        Predicate<PipelineConfig> predicate = input -> input.matches(pipeline, stage, group, pipelineStatus);
        List<PipelineConfig> result = new ArrayList<>();
        for(MsTeamsConfig msTeamsConfig: msTeamsConfigList){
            if(processAllRules) {
                result.addAll(Lists.filter(msTeamsConfig.getPipelineConfig(), predicate));
            } else {

                Option<PipelineConfig> match = Lists.find(msTeamsConfig.getPipelineConfig(), predicate);
                if(match.isDefined()) {
                    result.add(match.get());
                }
            }
        }
        return result;
    }

    /**
     * Constructor from config file
     * @param config
     */
    private String fromConfig(final Config config) {

        final StringBuilder errorMsg = new StringBuilder();

        this.enabled = true;
        if(config.hasPath("enabled")){
            this.enabled = config.getBoolean("enabled");
        }

        this.goServerHost = "";
        if(config.hasPath("server-host")){
            this.goServerHost = config.getString("server-host");
        }else{
            fieldError(errorMsg, "server-host");
        }

        this.goLogin = null;
        if (config.hasPath("login")) {
            this.goLogin = config.getString("login");
        }

        this.goPassword = null;
        if (config.hasPath("password")) {
            this.goPassword = config.getString("password");
        }

        this.goAPIToken = null;
        if (config.hasPath("api-token")) {
            this.goAPIToken = config.getString("api-token");
        }

        this.apiMsTeamsHost = "http://localhost";
        if(config.hasPath("api-msteams-host")){
            this.apiMsTeamsHost = config.getString("api-msteams-host");
        }else {
            fieldError(errorMsg, "api-msteams-host");
        }

        if(config.hasPath("msteams")){

            ConfigList teams = config.getList("msteams");
            this.msTeamsConfigList = new ArrayList<>();

            for(Object team : teams.unwrapped()){
                //Add new team
                MsTeamsConfig teamsConfig = new MsTeamsConfig((Map<String, Object>) team, errorMsg);
                this.msTeamsConfigList.add(teamsConfig);
            }
        }else {
            fieldError(errorMsg, "msteams");
        }

        this.displayConsoleLogLinks = true;
        if (config.hasPath("display-console-log-links")) {
            displayConsoleLogLinks = config.getBoolean("display-console-log-links");
        }

        this.displayMaterialChanges = true;
        if (config.hasPath("displayMaterialChanges")) {
            displayMaterialChanges = config.getBoolean("displayMaterialChanges");
        }

        this.processAllRules = true;
        if (config.hasPath("process-all-rules")) {
            processAllRules = config.getBoolean("process-all-rules");
        }

        this.truncateChanges = true;
        if(config.hasPath("truncate-changes")) {
            truncateChanges = config.getBoolean("truncate-changes");
        }

        if (config.hasPath("proxy")) {
            Config proxyConfig = config.getConfig("proxy");
            if (proxyConfig.hasPath("hostname") && proxyConfig.hasPath("port") && proxyConfig.hasPath("type")) {
                String hostname = proxyConfig.getString("hostname");
                int port = proxyConfig.getInt("port");
                String type = proxyConfig.getString("type").toUpperCase();
                Proxy.Type proxyType = Proxy.Type.valueOf(type);
                this.proxy = new Proxy(proxyType, new InetSocketAddress(hostname, port));
            }
        }

        return errorMsg.toString();
    }

    private void fieldError(StringBuilder errors, String field) {
        errors.append(field)
                .append(" field is required for plugin configuration ").append('\n');
        this.errors = true;
    }

}
