package com.roxorgaming.gocd.mstream.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import in.ashwanthkumar.utils.collections.Iterables;
import in.ashwanthkumar.utils.func.Predicate;
import in.ashwanthkumar.utils.lang.StringUtils;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineConfig {

    @JsonProperty
    private String nameRegex;

    @JsonProperty
    private String stageRegex;

    @JsonProperty
    private String groupRegex;

    @JsonProperty
    private List<PipelineStatus> pipelineStatus = new ArrayList<>();

    @Setter(AccessLevel.NONE)
    private boolean hasErrors = false;


    public PipelineConfig(PipelineConfig copy) {
        this.nameRegex = copy.nameRegex;
        this.stageRegex = copy.stageRegex;
        this.groupRegex = copy.groupRegex;
    }

    public PipelineConfig(String nameRegex, String stageRegex) {
        this.nameRegex = nameRegex;
        this.stageRegex = stageRegex;
    }

    /**
     * Build the configuration from a config file also do valdation
     * if parent configuration contains errors it will still construct the configuration
     * to validate the file content.
     * @param config - Config from file
     * @param errors - Error String for user if configration is not  valid
     */
    public PipelineConfig(Map config, StringBuilder errors){
        fromConfig(config, errors);
    }


    public PipelineConfig setStatus(List<String> statuses, StringBuilder errors) {
        if(statuses == null || statuses.isEmpty())
            throw  new RuntimeException(new StringBuilder().append("No status found on pipeline configuration. Group: ")
                    .append(this.groupRegex).append(" Stage: ")
                    .append(this.getStageRegex())
                    .append(" Name: ")
                    .append(this.getNameRegex()).toString());

            for (String status : statuses) {
                try {
                    this.pipelineStatus.add(PipelineStatus.valueOf(status.toUpperCase()));

                }catch(IllegalArgumentException e){
                    errors.append("invalid status ").append(status);
                }
        }

        return this;
    }

    /**
     * Matches all the Regular Expressions.
     * @param pipeline
     * @param stage
     * @param group
     * @param pipelineState
     * @return
     */
    public boolean matches(String pipeline, String stage, String group, final String pipelineState) {
            return pipeline.matches(nameRegex)
                    && stage.matches(stageRegex)
                    && matchesGroup(group)
                    && Iterables.exists(pipelineStatus, hasStateMatching(pipelineState));
    }

    private boolean matchesGroup(String group) {
        return StringUtils.isEmpty(groupRegex) || group.matches(groupRegex);
    }

    private Predicate<PipelineStatus> hasStateMatching(final String pipelineState) {
        return new Predicate<PipelineStatus>() {
            @Override
            public Boolean apply(PipelineStatus input) {
                return input.matches(pipelineState);
            }
        };
    }

    private void fromConfig(Map config, StringBuilder errors) {

        if(config.containsKey("name")){
            this.nameRegex = ((String) config.get("name"));
        }else{
            fieldError(errors, "name");
        }

        if (config.containsKey("group")) {
            this.groupRegex = ((String) config.get("group"));
        }else{
            this.groupRegex = ".*";
        }


        if (config.containsKey("stage")) {
            this.stageRegex = ((String) config.get("stage"));
        }else{
            this.stageRegex = ".*";
        }

        if (config.containsKey("statuses")) {
            List<String> statuses = (List<String>) config.get("statuses");
            this.setStatus(statuses, errors);
        }else{
            fieldError(errors, "statuses");
        }
    }

    private void fieldError(StringBuilder errors, String field) {
        errors.append(field)
        .append(" field is required for pipeline configuration ").append('\n');
        this.hasErrors = true;
    }


}
