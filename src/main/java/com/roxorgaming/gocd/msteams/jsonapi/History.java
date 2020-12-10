package com.roxorgaming.gocd.msteams.jsonapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thoughtworks.go.plugin.api.logging.Logger;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class History {

    private Logger LOG = Logger.getLoggerFor(History.class);

    @JsonProperty("pipelines")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Pipeline> pipelines = new ArrayList<>();

    @JsonProperty("pagination")
    private Pagination pagination;

    /**
     * Find the most recent run of the specified stage _before_ this one.
     */
    public Stage previousRun(int pipelineCounter, String stageName, int stageCounter) {
        LOG.debug(String.format("Looking for stage before %d/%s/%d",
                pipelineCounter, stageName, stageCounter));

        // Note that pipelines and stages are stored in reverse
        // chronological order.
        for (Pipeline pipeline: pipelines) {

            for (int j = 0; j < pipeline.getStages().length; j++) {
                Stage stage = pipeline.getStages()[j];
                LOG.debug(String.format("Checking %d/%s/%d",
                        pipeline.getCounter(), stage.getName(), stage.getCounter()));

                if (stage.getName().equals(stageName)) {

                    // Same pipeline run, earlier instance of stage.
                    if (pipeline.getCounter() == pipelineCounter &&
                            stage.getCounter() < stageCounter)
                        return stage;

                    // Previous pipeline run.
                    if (pipeline.getCounter() < pipelineCounter)
                        return stage;
                }
            }
        }
        // Not found.
        return null;
    }
}

