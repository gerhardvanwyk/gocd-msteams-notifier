package com.roxorgaming.gocd.mstream;

import com.roxorgaming.gocd.msteams.jsonapi.GoCdClient;
import com.roxorgaming.gocd.msteams.jsonapi.History;
import com.roxorgaming.gocd.mstream.configuration.PipelineConfig;
import com.roxorgaming.gocd.mstream.configuration.PipelineStatus;
import com.roxorgaming.gocd.mstream.notification.PipelineInfo;

public class MsTeamsPipelineListener extends PipelineListener {

    private final MsTeamsClient msTeamsClient;

    public MsTeamsPipelineListener(GoCdClient goCdClient){
        super(goCdClient);
        this.msTeamsClient = new MsTeamsClient();
    }

    public void onMessage(final PipelineConfig config, final PipelineInfo pipelineInfo, final History history){
        for(PipelineStatus status: config.getPipelineStatus()) {
       //     Message msg = new Message(configuration, status);
            //   this.msTeamsClient.push(msg, rule.getChannel() );
        }
    }

}
