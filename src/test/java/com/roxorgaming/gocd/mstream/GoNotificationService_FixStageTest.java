package com.roxorgaming.gocd.mstream;

import com.roxorgaming.gocd.msteams.jsonapi.History;
import com.roxorgaming.gocd.msteams.jsonapi.Pipeline;
import com.roxorgaming.gocd.msteams.jsonapi.GoCdClient;
import com.roxorgaming.gocd.msteams.jsonapi.Stage;
import com.roxorgaming.gocd.mstream.configuration.Configuration;
import com.roxorgaming.gocd.mstream.notification.GoNotificationService;
import com.roxorgaming.gocd.mstream.notification.PipelineInfo;
import com.roxorgaming.gocd.mstream.notification.StageInfo;
import com.roxorgaming.gocd.mstream.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GoNotificationService_FixStageTest {

    public static final String PIPELINE_NAME = "PL";
    public static final String STAGE_NAME = "STG";

    private History pipelineHistory;
    private PipelineInfo pipeline;
    private String expectedStatus;

    public GoNotificationService_FixStageTest(History pipelineHistory, PipelineInfo pipeline, String expectedStatus) {
        this.pipelineHistory = pipelineHistory;
        this.pipeline = pipeline;
        this.expectedStatus = expectedStatus;
    }

    public static Stream<Arguments> data() {
        return Stream.of(
        // One history pipeline, same pipeline run
        Arguments.of(
                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Failed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(2), Status.Building))),
                thenExpectStatus(Status.Building)
        ), Arguments.of(
                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Failed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(2), Status.Passed))),
                thenExpectStatus(Status.Fixed)
        ), Arguments.of(
                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Failed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(2), Status.Passed))),
                thenExpectStatus(Status.Fixed)
        ), Arguments.of(
                        givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(2), Status.Failed))),
                thenExpectStatus(Status.Broken)
        ), Arguments.of(
                        givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(2), Status.Passed))),
                thenExpectStatus(Status.Passed)
        ), Arguments.of(
                        givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Failed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(2), Status.Failed))),
                thenExpectStatus(Status.Failed)
        ), Arguments.of(
                        givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Cancelled))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(2), Status.Passed))),
                thenExpectStatus(Status.Fixed)
        ), Arguments.of(
                        givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Cancelled))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(2), Status.Failed))),
                thenExpectStatus(Status.Failed)
        ), Arguments.of(
                        givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Cancelled))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(2), Status.Cancelled))),
                thenExpectStatus(Status.Cancelled)
        ), Arguments.of(

                        // Multiple stages
                givenHistory(pipeline(PIPELINE_NAME, counter(1),
                        stage("other-stage-name-1", counter(1), Status.Failed),
                        stage(STAGE_NAME,           counter(1), Status.Failed),
                        stage("other-stage-name-2", counter(1), Status.Failed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(4), Status.Passed))),
                thenExpectStatus(Status.Fixed)
        ), Arguments.of(
                        givenHistory(pipeline(PIPELINE_NAME, counter(1),
                        stage("other-stage-name-1", counter(1), Status.Passed),
                        stage(STAGE_NAME,           counter(1), Status.Failed),
                        stage("other-stage-name-2", counter(1), Status.Passed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(4), Status.Passed))),
                thenExpectStatus(Status.Fixed)
        ), Arguments.of(

                        // One history pipeline, next pipeline run

                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Failed))),
                thenExpectStatus(Status.Broken)
        ), Arguments.of(
                        givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed))),
                thenExpectStatus(Status.Passed)
        ), Arguments.of(
                        givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Failed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed))),
                thenExpectStatus(Status.Fixed)
        ), Arguments.of(
                        givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Failed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Failed))),
                thenExpectStatus(Status.Failed)
        ), Arguments.of(
                        givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Failed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Cancelled))),
                thenExpectStatus(Status.Cancelled)
        ), Arguments.of(
                        givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Cancelled))),
                thenExpectStatus(Status.Cancelled)
        ), Arguments.of(
                        // No history

                givenHistory(noPipelines()),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed))),
                thenExpectStatus(Status.Passed)
        ), Arguments.of(
                        givenHistory(noPipelines()),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Failed))),
                thenExpectStatus(Status.Failed)
        ), Arguments.of(
                        givenHistory(noPipelines()),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Cancelled))),
                thenExpectStatus(Status.Cancelled)
        ), Arguments.of(
                        givenHistory(noPipelines()),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Building))),
                thenExpectStatus(Status.Building)
        ), Arguments.of(
                        // Longer history, next pipeline run

                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Failed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Failed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Failed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(4), stage(STAGE_NAME, counter(1), Status.Passed))),
                thenExpectStatus(Status.Fixed)
        ), Arguments.of(
                        givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Failed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(4), stage(STAGE_NAME, counter(1), Status.Passed))),
                thenExpectStatus(Status.Fixed)
        ), Arguments.of(
                        givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Failed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Failed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(4), stage(STAGE_NAME, counter(1), Status.Passed))),
                thenExpectStatus(Status.Fixed)
        ), Arguments.of(
                        givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Passed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(4), stage(STAGE_NAME, counter(1), Status.Failed))),
                thenExpectStatus(Status.Broken)
        ), Arguments.of(
                givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Failed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Passed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(4), stage(STAGE_NAME, counter(1), Status.Failed))),
                thenExpectStatus(Status.Broken)
        ), Arguments.of(
                        givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Failed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(4), stage(STAGE_NAME, counter(1), Status.Failed))),
                thenExpectStatus(Status.Failed)
        ), Arguments.of(
                        givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Passed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(4), stage(STAGE_NAME, counter(1), Status.Failed))),
                thenExpectStatus(Status.Broken)
        ), Arguments.of(
                        givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Passed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(4), stage(STAGE_NAME, counter(1), Status.Cancelled))),
                thenExpectStatus(Status.Cancelled)
        ), Arguments.of(
                        givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Failed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(4), stage(STAGE_NAME, counter(1), Status.Cancelled))),
                thenExpectStatus(Status.Cancelled)
        ), Arguments.of(
                        givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Failed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Passed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(4), stage(STAGE_NAME, counter(1), Status.Passed))),
                thenExpectStatus(Status.Passed)
        ), Arguments.of(
                        givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Failed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Failed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Failed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(4), stage(STAGE_NAME, counter(1), Status.Failed))),
                thenExpectStatus(Status.Failed)
        ), Arguments.of(
                        // Longer history, same pipeline as the last in history

                givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Passed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(3), Status.Failed))),
                thenExpectStatus(Status.Broken)
        ), Arguments.of(
                        givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Failed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(3), Status.Passed))),
                thenExpectStatus(Status.Fixed)
                ), Arguments.of(
                        givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Failed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(3), Status.Failed))),
                thenExpectStatus(Status.Failed)
        ), Arguments.of(
                        givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Passed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(3), Status.Passed))),
                thenExpectStatus(Status.Passed)
        ), Arguments.of(
                        givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Failed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(3), Status.Cancelled))),
                thenExpectStatus(Status.Cancelled)
        ), Arguments.of(
                        givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Cancelled))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(3), Status.Failed))),
                thenExpectStatus(Status.Failed)
        ), Arguments.of(
                        givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Cancelled))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(3), Status.Passed))),
                thenExpectStatus(Status.Fixed)
        ), Arguments.of(
                        givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Failed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Passed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(3), Status.Passed))),
                thenExpectStatus(Status.Passed)
        ), Arguments.of(
                        givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Failed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Failed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Failed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(3), Status.Failed))),
                thenExpectStatus(Status.Failed)
                )

        );
    }

    @ParameterizedTest
    @MethodSource("data")
    @Disabled
    public void shouldResolveCorrectStageStatus() throws IOException {
//        GoCdClient goCdClient = mock(GoCdClient.class);
//        when(goCdClient.getPipelineHistory(PIPELINE_NAME)).thenReturn(pipelineHistory);
//
//        GoNotificationService message = new GoNotificationService(
//                TestUtils.createMockServerFactory(goCdClient),
//                pipeline
//        );
//
//        message.tryToFixStageResult(new Configuration());
//
//        Assertions.assertEquals(message.getStageResult(), is(expectedStatus));
    }

    /**
     * @param pipelines Pipelines in chronological order, oldest one first.
     * @return History object
     */
    private static History givenHistory(Pipeline... pipelines) {
        History history = new History();
        List<Pipeline> helperList = Arrays.asList(pipelines);
        Collections.reverse(helperList);
        history.setPipelines(helperList);
        return history;
    }

    private static Pipeline pipeline(String name, int counter, Stage... stages) {
        Pipeline pipeline = new Pipeline(name, counter, stages);
        return pipeline;
    }

    private static Pipeline[] noPipelines() {
        return new Pipeline[0];
    }

    private static Stage stage(String name, int counter, Status status) {
        Stage stage = new Stage(name, counter, status.getResult());
        return stage;
    }

    private static PipelineInfo whenPipelineFinished(Pipeline pipeline) {
        PipelineInfo info = new PipelineInfo();
        info.setName(pipeline.getName());
        info.setCounter(Integer.toString(pipeline.getCounter()));
        info.setStage( new StageInfo() );

        Stage stage = pipeline.getStages()[0];
        info.getStage().setCounter(Integer.toString(stage.getCounter()));
        info.getStage().setName(stage.getName());
        info.getStage().setState( Status.valueOf(stage.getResult()).getStatus() );
        info.getStage().setResult(Status.valueOf(stage.getResult()).getResult() );
        return info;
    }

    private static String thenExpectStatus(Status status) {
        return status.getStatus();
    }

    private static int counter(int value) {
        return value;
    }

}
