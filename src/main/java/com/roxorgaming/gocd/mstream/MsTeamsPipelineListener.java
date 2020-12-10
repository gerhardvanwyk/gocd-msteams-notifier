package com.roxorgaming.gocd.mstream;

import com.roxorgaming.gocd.msteams.jsonapi.Message;
import com.roxorgaming.gocd.mstream.configuration.PipelineConfig;
import com.roxorgaming.gocd.mstream.configuration.PipelineStatus;
import com.roxorgaming.gocd.mstream.configuration.Configuration;
import com.roxorgaming.gocd.mstream.notification.GoNotificationMessage;

public class MsTeamsPipelineListener extends PipelineListener {

    private final MsTeamsClient msTeamsClient;

    public MsTeamsPipelineListener(Configuration configuration){
        super(configuration);
        this.msTeamsClient = new MsTeamsClient();
    }

    /**
     *
     * @param rule
     * @param message
     * @param status
     */
    private void onMessage(final PipelineConfig rule, final GoNotificationMessage message, final PipelineStatus status){
        Message msg = new Message(configuration, status, message );
        this.msTeamsClient.push(msg, rule.getChannel() );
    }

    @Override
    public void onBuilding(final PipelineConfig rule, final GoNotificationMessage message) {
        onMessage(rule, message, PipelineStatus.BUILDING);
    }

    @Override
    public void onPassed(final PipelineConfig rule, final GoNotificationMessage message) {
        onMessage(rule, message, PipelineStatus.PASSED);
    }

    @Override
    public void onFailed(final PipelineConfig rule, final GoNotificationMessage message)  {
        onMessage(rule, message, PipelineStatus.FAILED);
    }

    @Override
    public void onBroken(final PipelineConfig rule, final GoNotificationMessage message) {
        onMessage(rule, message, PipelineStatus.BROKEN);
    }

    @Override
    public void onFixed(final PipelineConfig rule, final GoNotificationMessage message) {
        onMessage(rule, message, PipelineStatus.FIXED);
    }

    @Override
    public void onCancelled(final PipelineConfig rule, final GoNotificationMessage message)  {
        onMessage(rule, message, PipelineStatus.CANCELLED);
    }
}
