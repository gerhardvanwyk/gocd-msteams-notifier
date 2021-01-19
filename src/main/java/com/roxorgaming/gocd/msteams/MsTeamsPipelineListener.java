package com.roxorgaming.gocd.msteams;

import com.roxorgaming.gocd.msteams.jsonapi.GoCdClient;
import com.roxorgaming.gocd.msteams.jsonapi.MaterialRevision;
import com.roxorgaming.gocd.msteams.jsonapi.Pipeline;
import com.roxorgaming.gocd.msteams.configuration.PipelineConfig;
import com.roxorgaming.gocd.msteams.configuration.PipelineStatus;
import com.roxorgaming.gocd.msteams.notification.PipelineInfo;

import java.util.List;

public class MsTeamsPipelineListener extends PipelineListener {

    private final MsTeamsClient msTeamsClient;

    public MsTeamsPipelineListener(GoCdClient goCdClient){
        super(goCdClient);
        this.msTeamsClient = new MsTeamsClient();
    }

    public void onMessage(final PipelineConfig config, final Pipeline details, final PipelineInfo pipelineInfo,
                          final List<MaterialRevision> changes){
        for(PipelineStatus status: config.getPipelineStatus()) {
            Message msg = new Message(configuration, details, pipelineInfo, status, changes);
            this.msTeamsClient.push(msg );
        }
    }

}
