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

    public String fullyQualifiedJobName() {
        return getName() + "/" + getCounter() + "/" + getStage().getName() + "/" + getStage().getCounter();
    }

    @Override
    public String toString() {
        String stg = (stage == null) ? "" : stage.getName();
        String rsl = (stage == null) ? "" : stage.getResult();
        return name + "/" + counter + "/" + stg + "/" + rsl;
    }
}
