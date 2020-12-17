package com.roxorgaming.gocd.msteams.jsonapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import in.ashwanthkumar.utils.collections.Lists;
import in.ashwanthkumar.utils.func.Function;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stage extends Job {
    @JsonProperty("id")
    private int id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("counter")
    private int counter;

    @JsonProperty("result")
    private String result;

    @JsonProperty("approved_by")
    private String approvedBy;

    @JsonProperty("jobs")
    private Job[] jobs;

    @JsonProperty("approval_type")
    private String approvalType;

    @JsonProperty("can_run")
    private Boolean canRun;

    @JsonProperty("operate_permission")
    private String operatePermission;

    @JsonProperty("rerun_of_counter")
    private String rerunOfCounter;

    @JsonProperty("scheduled")
    private Boolean scheduled;

    public Stage(String result){
        this.result = result;
    }

    public Stage(String name, int counter, String result){
        this.name = name;
        this.counter = counter;
        this.result = result;
    }

    public List<String> jobNames() {
        return Lists.map(Lists.of(jobs), input -> input.getName());
    }
}
