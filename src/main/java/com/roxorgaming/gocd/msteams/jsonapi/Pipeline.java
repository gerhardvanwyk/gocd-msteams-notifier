package com.roxorgaming.gocd.msteams.jsonapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Pipeline  {

    @JsonProperty("build_cause")
    private BuildCause buildCause;

    @JsonProperty("name")
    private String name;

    @JsonProperty("natural_order")
    private String naturalOrder;

    @JsonProperty("can_run")
    private boolean canRun;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("stages")
    private Stage[] stages;

    @JsonProperty("counter")
    private int counter;

    @JsonProperty("id")
    private int id;

    @JsonProperty("preparing_to_schedule")
    private boolean preparingToSchedule;

    @JsonProperty("label")
    private String label;

    public Pipeline(String name, int counter, Stage[] stages) {
        this.name = name;
        this.counter = counter;
        this.stages = stages;
    }

    // "comment"
    // "natural_order"
    // "stages"

    /**
     * Collect all changed MaterialRevision objects, walking changed
     * "Pipeline" objects recursively instead of including them directly.
     */
    public List<MaterialRevision> rootChanges(GoCdClient goCdClient) throws IOException {
        List result = new ArrayList();
        addChangesRecursively(goCdClient, result);
        return result;
    }

    void addChangesRecursively(GoCdClient goCdClient, List<MaterialRevision> outChanges) throws IOException {
        for (MaterialRevision mr : buildCause.getMaterialRevisions()) {
            mr.addChangesRecursively(goCdClient, outChanges);
        }
    }

    @Override
    public String toString() {
        if (stages != null && stages.length > 0) {
            return name + "/" + counter + "/" + stages[0].getName() + "/" + stages[0].getResult();
        } else {
            return name + "/" + counter;
        }
    }
}
