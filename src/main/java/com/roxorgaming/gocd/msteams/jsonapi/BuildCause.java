package com.roxorgaming.gocd.msteams.jsonapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
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
