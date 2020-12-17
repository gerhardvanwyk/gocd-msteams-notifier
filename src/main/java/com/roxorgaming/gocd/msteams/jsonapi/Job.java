package com.roxorgaming.gocd.msteams.jsonapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Job {
    @JsonProperty("name")
    private String name;

    @JsonProperty("result")
    private String result;

    @JsonProperty("state")
    private String state;

    @JsonProperty("id")
    private int id;

    @JsonProperty("scheduled_date")
    private long scheduledDate;
}
