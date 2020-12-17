package com.roxorgaming.gocd.msteams.jsonapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuildCause {
    
    @JsonProperty("approver")
    private String approver;

    @JsonProperty("trigger_forced")
    private boolean triggerForced;

    @JsonProperty("trigger_message")
    private String triggerMessage;

    @JsonProperty("material_revisions")
    private MaterialRevision[] materialRevisions;
}
