package com.roxorgaming.gocd.msteams.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StageInfo {

    private String name;

    private String counter;

    private String state;

    private String result;

    private String createTime;

    private String lastTransitionTime;
}
