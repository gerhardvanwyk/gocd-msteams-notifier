package com.roxorgaming.gocd.msteams.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class MsTeamsConfig {

    @JsonProperty
    private String teamsId;
    @JsonProperty
    private String displayName;
    @JsonProperty
    private String iconUrl;
    @JsonProperty
    private List<String> channelWebhooks;
    @JsonProperty
    private List<PipelineConfig> pipelineConfig = new ArrayList<>();

    @Setter(AccessLevel.NONE)
    private boolean hasErrors = false;

    public MsTeamsConfig(Map<String, Object> config, StringBuilder errors){

        if(config.containsKey("team")){
            this.teamsId = (String) config.get("team");
        }else{
            fieldError(errors, "team");
        }

        if(config.containsKey("display-name")){
            this.displayName = (String) config.get("display-name");
        }else{
            this.displayName = "GoCD Notification Bot";
        }

        if(config.containsKey("icon-url")){
            this.iconUrl = (String) config.get("icon-url");
        }else{
            this.iconUrl = "";
        }

        if(config.containsKey("channels")){
            setChannelWebhooks((List<String>) config.get("channels"));
            if(channelWebhooks == null || channelWebhooks.isEmpty()) {
                errors.append("Channels webhook's list cannot be empty")
                        .append('\n');
                this.hasErrors = true;
            }
        }else{
            fieldError(errors, "channels");
        }

        if(config.containsKey("pipelines")){
            this.pipelineConfig = new ArrayList<>();
            List<Map> pipelines = (List<Map>) config.get("pipelines");
            for (Map pipeline : pipelines) {
                //Add new pipeline
                PipelineConfig pipelineConfig = new PipelineConfig(pipeline, errors);
                this.pipelineConfig.add(pipelineConfig);
            }
        }else {
            fieldError(errors, "pipelines");
        }
    }


    public void addAllPipelineRules(List<PipelineConfig> pipelines) {
        this.pipelineConfig.addAll(pipelines);
    }


    private void fieldError(StringBuilder errors, String field) {
        errors.append(field)
                .append(" field is required for teams configuration ").append('\n');
        this.hasErrors = true;
    }
}
