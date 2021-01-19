package com.roxorgaming.gocd.msteams.configuration;

import com.roxorgaming.gocd.msteams.configuration.Configuration;
import com.roxorgaming.gocd.msteams.configuration.MsTeamsConfig;
import com.roxorgaming.gocd.msteams.configuration.PipelineConfig;
import com.roxorgaming.gocd.msteams.configuration.PipelineStatus;
import com.roxorgaming.gocd.msteams.Status;
import in.ashwanthkumar.utils.collections.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ConfigurationTest {

    @Test
    @DisplayName("Finding matches with full and regex")
    public void shouldFindMatch() {
        //Add pipelines
        PipelineConfig pipelineConfig1 = PipelineConfig.builder()
                .nameRegex("gocd-slack-build-notifier")
                .groupRegex(".*")
                .stageRegex(".*")
                .pipelineStatus(Lists.of(PipelineStatus.BUILDING))
                .build();

        PipelineConfig pipelineConfig2 = PipelineConfig.builder()
                .nameRegex("my-java-utils")
                .stageRegex("build")
                .groupRegex(".*")
                .pipelineStatus(Lists.of(PipelineStatus.CANCELLED))
                .build();

        List<PipelineConfig> pipeLines = new ArrayList<>();
        pipeLines.add(pipelineConfig1);
        pipeLines.add(pipelineConfig2);

        MsTeamsConfig msTeamsConfig = MsTeamsConfig.builder()
                .pipelineConfig(pipeLines)
                .build();

        List<MsTeamsConfig> msTeamsConfigList = new ArrayList<>();
        msTeamsConfigList.add(msTeamsConfig);

        Configuration configuration = Configuration.builder().enabled(true)
                .msTeamsConfigList(msTeamsConfigList)
                .build();

        List<PipelineConfig> foundRules1 = configuration.find("gocd-slack-build-notifier", "stage1", "ci", Status.Building.getStatus());
        assertThat(foundRules1.size(), is(1));
        assertThat(foundRules1.get(0).getNameRegex(), is("gocd-slack-build-notifier"));
        assertThat(foundRules1.get(0).getStageRegex(), is(".*"));

        List<PipelineConfig> foundRules2 = configuration.find("my-java-utils", "build", "ci", Status.Cancelled.getStatus());
        assertThat(foundRules2.size(), is(1));
        assertThat(foundRules2.get(0).getNameRegex(), is("my-java-utils"));
        assertThat(foundRules2.get(0).getStageRegex(), is("build"));

        List<PipelineConfig> foundRules3 = configuration.find("pipeline2", "stage2", "ci", Status.Passed.getStatus());
        assertThat(foundRules3.size(), is(0));
    }

    @Test
    @DisplayName("Do not Stop processing when the first match is find 'process all rules is true'")
    public void shouldFindMatchWithRegexp() {
        //Add pipelines
        PipelineConfig pipelineConfig1 = PipelineConfig.builder()
                .nameRegex("gocd-slack-build-notifier")
                .groupRegex(".*")
                .stageRegex(".*")
                .pipelineStatus(Lists.of(PipelineStatus.BUILDING))
                .build();

        PipelineConfig pipelineConfig2 = PipelineConfig.builder()
                .nameRegex("gocd-slack-build-notifier")
                .groupRegex(".*")
                .stageRegex(".*")
                .pipelineStatus(Lists.of(PipelineStatus.BUILDING))
                .build();

        List<PipelineConfig> pipeLines1 = new ArrayList<>();
        pipeLines1.add(pipelineConfig1);

        MsTeamsConfig msTeamsConfig1 = MsTeamsConfig.builder()
                .pipelineConfig(pipeLines1)
                .build();

        List<PipelineConfig> pipeLines2 = new ArrayList<>();
        pipeLines1.add(pipelineConfig2);

        MsTeamsConfig msTeamsConfig2 = MsTeamsConfig.builder()
                .pipelineConfig(pipeLines2)
                .build();

        List<MsTeamsConfig> msTeamsConfigList = new ArrayList<>();
        msTeamsConfigList.add(msTeamsConfig1);
        msTeamsConfigList.add(msTeamsConfig2);

        Configuration configuration = Configuration.builder().enabled(true)
                .msTeamsConfigList(msTeamsConfigList)
                .processAllRules(true)
                .build();

        List<PipelineConfig> foundRules1 = configuration.find("gocd-slack-build-notifier", "efg", "ci", Status.Building.getStatus());
        assertThat(foundRules1.size(), is(2));
        assertThat(foundRules1.get(0).getNameRegex(), is("gocd-slack-build-notifier"));
        assertThat(foundRules1.get(0).getStageRegex(), is(".*"));
     //   assertThat(foundRules1.get(0).getChannel(), is("ch1"));

        List<PipelineConfig> foundRules2 = configuration.find("gocd-slack-build-notifier", "456", "ci", Status.Building.getStatus());
        assertThat(foundRules2.size(), is(2));
        assertThat(foundRules2.get(0).getStageRegex(), is(".*"));
    //    assertThat(foundRules2.get(0).getChannel(), is("ch2"));

    }

    private static PipelineConfig pipelineRule(String pipeline, String stage, String channel, List<PipelineStatus> statuses) {
        PipelineConfig pipelineConfig = new PipelineConfig(pipeline, stage);
 //       pipelineConfig.setStatus(statuses);
 //       pipelineConfig.addChannel(channel);
        return pipelineConfig;
    }

    private static List<PipelineStatus> statuses(PipelineStatus... statuses) {
        return Arrays.asList(statuses);
    }

}
