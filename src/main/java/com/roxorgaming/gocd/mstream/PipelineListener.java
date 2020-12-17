package com.roxorgaming.gocd.mstream;

import com.roxorgaming.gocd.msteams.jsonapi.GoCdClient;
import com.roxorgaming.gocd.msteams.jsonapi.History;
import com.roxorgaming.gocd.mstream.configuration.Configuration;
import com.roxorgaming.gocd.mstream.notification.GoNotificationService;
import com.roxorgaming.gocd.mstream.notification.PipelineInfo;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.roxorgaming.gocd.mstream.configuration.PipelineConfig;

import java.util.List;

abstract public class PipelineListener {

    private Logger LOG = Logger.getLoggerFor(PipelineListener.class);

    protected Configuration configuration;

    private GoNotificationService service;

    public PipelineListener(GoCdClient goCdClient) {
        this.configuration = goCdClient.getConfiguration();
        this.service = new GoNotificationService(goCdClient);
    }

    public void notify(PipelineInfo pipelineInfo) throws Exception {

        this.service.tryToFixStageResult(pipelineInfo);

        String stageResult = pipelineInfo.getStage().getResult();
        String stageName = pipelineInfo.getStage().getName();

        LOG.debug(String.format("Finding rules with state %s", stageResult));
        List<PipelineConfig> foundRules = configuration.find(pipelineInfo.getName(), stageName, pipelineInfo.getGroup(), stageResult);
        History history = this.service.fetchHistory(pipelineInfo.getName());
        if (foundRules.size() > 0) {
            for (PipelineConfig pipelineConfig : foundRules) {
                LOG.debug(String.format("Matching rule is %s", pipelineConfig));
                onMessage(pipelineConfig, pipelineInfo, history);
            }

        } else {
            LOG.warn(String.format("Couldn't find any matching rule for %s/%s with status=%s", stageName,
                    stageName, stageResult));
        }
    }

    /**
     * Send a message for each config that matches the notification
     * @param config
     */
    public abstract void onMessage(final PipelineConfig config, PipelineInfo pipelineInfo, History history);

}
