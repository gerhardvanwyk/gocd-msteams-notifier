package com.roxorgaming.gocd.notification;

import com.roxorgaming.gocd.msteams.jsonapi.GoCdClient;
import com.roxorgaming.gocd.msteams.jsonapi.History;
import com.roxorgaming.gocd.msteams.jsonapi.Pipeline;
import com.roxorgaming.gocd.msteams.jsonapi.Stage;
import com.roxorgaming.gocd.mstream.configuration.Configuration;
import com.roxorgaming.gocd.mstream.notification.GoNotificationService;
import com.roxorgaming.gocd.mstream.notification.PipelineInfo;
import com.roxorgaming.gocd.mstream.notification.StageInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GoNotificationServiceTest {

    private static final String PIPELINE_NAME = "pipeline";

    @Test
    @DisplayName("Fetch a details for a pipeline from the pipelines history")
    public void shouldFetchPipelineDetails() throws Exception {
        GoCdClient goCdClient = mock(GoCdClient.class);

        History pipelineHistoryMock = new History();
        pipelineHistoryMock.setPipelines(Arrays.asList(
                pipeline(PIPELINE_NAME, 8, new Stage[] { new Stage("Success") }),
                pipeline(PIPELINE_NAME, 9, new Stage[] { new Stage("Broken") } ),
                pipeline(PIPELINE_NAME, 10, new Stage[] { new Stage("Broken") } ),
                pipeline(PIPELINE_NAME, 11,  new Stage[] { new Stage("Failed") } ),
                pipeline(PIPELINE_NAME, 12,  new Stage[] { new Stage("Broken") } )
        ));


        when(goCdClient.getPipelineHistory(PIPELINE_NAME)).thenReturn(pipelineHistoryMock);

        GoNotificationService service = new GoNotificationService(goCdClient);
        Pipeline result = service.fetchDetailsFromHistory(PIPELINE_NAME, 11);
        Assertions.assertEquals(result.getName(), PIPELINE_NAME);
        Assertions.assertEquals(result.getStages()[0].getResult(), "Failed");
    }



    @Test
    public void fix_stage_results() throws Exception {
        GoCdClient goCdClient = mock(GoCdClient.class);

        Configuration configuration = Configuration.builder()
                .apiMsTeamsHost("http://localhost")
                .build();

        StageInfo stageInfo = StageInfo.builder()
                .name("pipeline1")
                .result("Fixed")
                .state("Broken")
                .build();

        PipelineInfo info = PipelineInfo.builder()
                .name("pipeline1")
                .stage(stageInfo)
                .build();

        History pipelineHistory = new History();
        Stage stage = new Stage();
        stage.setState("Broken");
        stage.setState("Failed");
        pipelineHistory.setPipelines(Arrays.asList(new Pipeline("pipeline1", 10, new Stage[]{stage})));
        when(goCdClient.getPipelineHistory("something-different")).thenReturn(pipelineHistory);

        GoNotificationService message = new GoNotificationService(goCdClient);

        message.tryToFixStageResult(info);

        pipelineHistory.getPipelines();

    }

    private Pipeline pipeline(String name, int counter, Stage[] stages) {
        Pipeline pipeline = new Pipeline(name, counter, stages);
        return pipeline;
    }

    private PipelineInfo info(String name, int counter) {
        PipelineInfo pipeline = new PipelineInfo(name, counter);
        return pipeline;
    }

}
