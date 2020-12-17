package com.roxorgaming.gocd.mstream.configuration;

import com.roxorgaming.gocd.mstream.notification.GoNotificationService;
import com.roxorgaming.gocd.mstream.PipelineListener;

public enum PipelineStatus {
    /**
     * Status of the pipeline while being built.
     */
    BUILDING,
    /**
     * The pipeline has passed earlier and also now.
     */
    PASSED,
    /**
     * Pipeline has failed for the first time
     */
    FAILED,
    /**
     * Current and previous run of the pipeline failed hences broken
     */
    BROKEN,
    /**
     * Previous run has failed but now it succeeded
     */
    FIXED,
    /**
     * Pipeline is an unknown state (often temporary?)
     */
    UNKNOWN,
    /**
     * Pipeline has been cancelled.
     */
    CANCELLED,
    /**
     * Pretty obvious ah?
     */
    ALL;

    public boolean matches(String state) {
        return this == ALL || this == PipelineStatus.valueOf(state.toUpperCase());
    }
}
