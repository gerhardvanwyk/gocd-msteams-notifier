package com.roxorgaming.gocd.mstream.configuration;

import com.roxorgaming.gocd.mstream.notification.GoNotificationMessage;
import com.roxorgaming.gocd.mstream.PipelineListener;

public enum PipelineStatus {
    /**
     * Status of the pipeline while being built.
     */
    BUILDING {
        @Override
        public void handle(PipelineListener listener, PipelineConfig rule, GoNotificationMessage message) throws Exception {
            listener.onBuilding(rule, message);
        }
    },
    /**
     * The pipeline has passed earlier and also now.
     */
    PASSED {
        @Override
        public void handle(PipelineListener listener, PipelineConfig rule, GoNotificationMessage message) throws Exception {
            listener.onPassed(rule, message);
        }
    },
    /**
     * Pipeline has failed for the first time
     */
    FAILED {
        @Override
        public void handle(PipelineListener listener, PipelineConfig rule, GoNotificationMessage message) throws Exception {
            listener.onFailed(rule, message);
        }
    },
    /**
     * Current and previous run of the pipeline failed hences broken
     */
    BROKEN {
        @Override
        public void handle(PipelineListener listener, PipelineConfig rule, GoNotificationMessage message) throws Exception {
            listener.onBroken(rule, message);
        }
    },
    /**
     * Previous run has failed but now it succeeded
     */
    FIXED {
        @Override
        public void handle(PipelineListener listener, PipelineConfig rule, GoNotificationMessage message) throws Exception {
            listener.onFixed(rule, message);
        }
    },
    /**
     * Pipeline is an unknown state (often temporary?)
     */
    UNKNOWN {
        @Override
        public void handle(PipelineListener listener, PipelineConfig rule, GoNotificationMessage message) throws Exception {
            /*
            * No-op - We never report this status.
            */
        }
    },
    /**
     * Pipeline has been cancelled.
     */
    CANCELLED {
        @Override
        public void handle(PipelineListener listener, PipelineConfig rule, GoNotificationMessage message) throws Exception {
            listener.onCancelled(rule, message);
        }
    },
    /**
     * Pretty obvious ah?
     */
    ALL {
        @Override
        public void handle(PipelineListener listener, PipelineConfig rule, GoNotificationMessage message) throws Exception {
            /*
            * No-op - Since we use this flag only to denote handle all states but not the actual state itself.
            */
        }
    };

    public boolean matches(String state) {
        return this == ALL || this == PipelineStatus.valueOf(state.toUpperCase());
    }

    public abstract void handle(PipelineListener listener, PipelineConfig rule, GoNotificationMessage message) throws Exception;
}
