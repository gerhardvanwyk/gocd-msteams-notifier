package com.roxorgaming.gocd.mstream;

import com.roxorgaming.gocd.msteams.jsonapi.*;
import com.roxorgaming.gocd.mstream.configuration.Configuration;
import com.roxorgaming.gocd.mstream.notification.GoNotificationMessage;
import com.roxorgaming.gocd.mstream.util.TestUtils;
import in.ashwanthkumar.utils.collections.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GoNotificationMessageTest {

    private static final String PIPELINE_NAME = "pipeline";

    @Test
    public void shouldFetchPipelineDetails() throws Exception {
        GoCdClient goCdClient = mock(GoCdClient.class);

        History pipelineHistory = new History();
        pipelineHistory.setPipelines(Arrays.asList(new Pipeline[]{
                pipeline(PIPELINE_NAME, 8),
                pipeline(PIPELINE_NAME, 9),
                pipeline(PIPELINE_NAME, 10),
                pipeline(PIPELINE_NAME, 11),
                pipeline(PIPELINE_NAME, 12)
        }));
        when(goCdClient.getPipelineHistory(PIPELINE_NAME)).thenReturn(pipelineHistory);

        GoNotificationMessage message = new GoNotificationMessage(
                TestUtils.createMockServerFactory(goCdClient),
                info(PIPELINE_NAME, 10)
        );

        Pipeline result = message.fetchDetails(new Configuration());

        assertThat(result.getName(), is(PIPELINE_NAME));
        assertThat(result.getCounter(), is(10));
    }

    @Test
    public void shouldFetchPipelineDetailsNotFound() throws Exception {
        GoCdClient goCdClient = mock(GoCdClient.class);

        History pipelineHistory = new History();
        pipelineHistory.setPipelines(Arrays.asList(new Pipeline[]{
                pipeline(PIPELINE_NAME, 8),
                pipeline(PIPELINE_NAME, 9)
        }));
        when(goCdClient.getPipelineHistory(PIPELINE_NAME)).thenReturn(pipelineHistory);

        GoNotificationMessage message = new GoNotificationMessage(
                TestUtils.createMockServerFactory(goCdClient),
                info(PIPELINE_NAME, 10)
        );
        Assertions.assertThrows(GoNotificationMessage.BuildDetailsNotFoundException.class, () ->
            message.fetchDetails(new Configuration())
        );
    }

    @Test
    public void shouldFetchPipelineDetailsNothingFound() throws Exception {
        GoCdClient goCdClient = mock(GoCdClient.class);

        History pipelineHistory = new History();
        pipelineHistory.setPipelines(Arrays.asList(new Pipeline[]{
                pipeline("something-different", 10)
        }));
        when(goCdClient.getPipelineHistory("something-different")).thenReturn(pipelineHistory);

        GoNotificationMessage message = new GoNotificationMessage(
                TestUtils.createMockServerFactory(goCdClient),
                info(PIPELINE_NAME, 10)
        );
        Assertions.assertThrows(GoNotificationMessage.BuildDetailsNotFoundException.class, () ->
            message.fetchDetails(new Configuration())
        );
    }

    private static Pipeline pipeline(String name, int counter) {
        Pipeline pipeline = new Pipeline(name, counter, null);
        return pipeline;
    }

    private static PipelineInfo info(String name, int counter) {
        PipelineInfo pipeline = new PipelineInfo(name, counter);
        return pipeline;
    }

}
