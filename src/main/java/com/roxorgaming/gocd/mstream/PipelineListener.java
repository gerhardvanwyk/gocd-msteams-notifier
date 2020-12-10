package com.roxorgaming.gocd.mstream;

import com.roxorgaming.gocd.mstream.configuration.Configuration;
import com.roxorgaming.gocd.mstream.configuration.PipelineStatus;
import com.roxorgaming.gocd.mstream.notification.GoNotificationMessage;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.roxorgaming.gocd.mstream.configuration.PipelineConfig;

import java.util.List;

abstract public class PipelineListener extends Configuration {

    private Logger LOG = Logger.getLoggerFor(PipelineListener.class);

    protected Configuration configuration;

    public PipelineListener(Configuration configuration) {
        this.configuration = configuration;
    }

    public void notify(GoNotificationMessage message) throws Exception {
        message.tryToFixStageResult(configuration);
        LOG.debug(String.format("Finding rules with state %s", message.getStageResult()));
        List<PipelineConfig> foundRules = configuration.find(message.getPipelineName(), message.getStageName(),
                message.getPipelineGroup(), message.getStageResult());
        if (foundRules.size() > 0) {
            for (PipelineConfig pipelineConfig : foundRules) {
                LOG.debug(String.format("Matching rule is %s", pipelineConfig));
                handlePipelineStatus(pipelineConfig, PipelineStatus.valueOf(message.getStageResult().toUpperCase()), message);
                if (! configuration.getProcessAllRules()) {
                    break;
                }
            }
        } else {
            LOG.warn(String.format("Couldn't find any matching rule for %s/%s with status=%s", message.getPipelineName(),
                    message.getStageName(), message.getStageResult()));
        }
    }

    protected void handlePipelineStatus(PipelineConfig rule, PipelineStatus status, GoNotificationMessage message) throws Exception {
        status.handle(this, rule, message);
    }

    /**
     * Invoked when pipeline is BUILDING
     *
     * @param rule
     * @param message
     * @throws Exception
     */
    public abstract void onBuilding(PipelineConfig rule, GoNotificationMessage message) throws Exception;

    /**
     * Invoked when pipeline PASSED
     *
     * @param message
     * @throws Exception
     */
    public abstract void onPassed(PipelineConfig rule, GoNotificationMessage message) throws Exception;

    /**
     * Invoked when pipeline FAILED
     *
     * @param message
     * @throws Exception
     */
    public abstract void onFailed(PipelineConfig rule, GoNotificationMessage message) throws Exception;

    /**
     * Invoked when pipeline is BROKEN
     *
     * Note - This currently is not implemented
     *
     * @param message
     * @throws Exception
     */
    public abstract void onBroken(PipelineConfig rule, GoNotificationMessage message) throws Exception;

    /**
     * Invoked when pipeline is FIXED
     *
     * Note - This currently is not implemented
     *
     * @param message
     * @throws Exception
     */
    public abstract void onFixed(PipelineConfig rule, GoNotificationMessage message) throws Exception;

    /**
     * Invoked when pipeline is CANCELLED
     *
     * @param message
     * @throws Exception
     */
    public abstract void onCancelled(PipelineConfig rule, GoNotificationMessage message) throws Exception;
}
