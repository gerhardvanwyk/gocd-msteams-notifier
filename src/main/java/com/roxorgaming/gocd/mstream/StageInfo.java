package com.roxorgaming.gocd.mstream;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public
class StageInfo {

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
