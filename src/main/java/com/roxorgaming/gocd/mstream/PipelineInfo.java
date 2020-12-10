package com.roxorgaming.gocd.mstream;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PipelineInfo {

    @SerializedName("name")
    private String name;

    @SerializedName("counter")
    private String counter;

    @SerializedName("group")
    private String group;

    @SerializedName("stage")
    private StageInfo stage;

    public PipelineInfo(String name, int counter) {
        this.name = name;
        this.counter = Integer.toString( counter);
    }

    @Override
    public String toString() {
        return name + "/" + counter + "/" + stage.getName() + "/" + stage.getResult();
    }
}
