package com.roxorgaming.gocd.mstream.notification;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StageInfo {

    @SerializedName("name")
    private String name;

    @SerializedName("counter")
    private String counter;

    @SerializedName("state")
    private String state;

    @SerializedName("result")
    private String result;

    @SerializedName("create-time")
    private String createTime;

    @SerializedName("last-transition-time")
    private String lastTransitionTime;
}
