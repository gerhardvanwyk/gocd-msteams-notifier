package com.roxorgaming.gocd.mstream.configuration;

import com.roxorgaming.gocd.mstream.Status;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ConfigurationTest {

    @Test
    public void shouldFindMatch() {
        Configuration configuration = new Configuration();

        configuration.setPipelineRules(Arrays.asList(
                pipelineRule("pipeline1", "stage1", "ch1", statuses(PipelineStatus.BUILDING, PipelineStatus.FAILED)),
                pipelineRule("pipeline1", "stage2", "ch2", statuses(PipelineStatus.FIXED, PipelineStatus.PASSED)),
                pipelineRule("pipeline2", "stage2", "ch3", statuses(PipelineStatus.CANCELLED, PipelineStatus.BROKEN))
        ));

        List<PipelineConfig> foundRules1 = configuration.find("pipeline1", "stage1", "ci", Status.Building.getStatus());
        assertThat(foundRules1.size(), is(1));
        assertThat(foundRules1.get(0).getNameRegex(), is("pipeline1"));
        assertThat(foundRules1.get(0).getStageRegex(), is("stage1"));

        List<PipelineConfig> foundRules2 = configuration.find("pipeline2", "stage2", "ci", Status.Cancelled.getStatus());
        assertThat(foundRules2.size(), is(1));
        assertThat(foundRules2.get(0).getNameRegex(), is("pipeline2"));
        assertThat(foundRules2.get(0).getStageRegex(), is("stage2"));

        List<PipelineConfig> foundRules3 = configuration.find("pipeline2", "stage2", "ci", Status.Passed.getStatus());
        assertThat(foundRules3.size(), is(0));
    }

    @Test
    @Disabled
    public void shouldFindMatchWithRegexp() {
        Configuration configuration = new Configuration();

        configuration.setPipelineRules(Arrays.asList(
                pipelineRule("[a-z]*", "[a-z]*", "ch1", statuses(PipelineStatus.BUILDING)),
                pipelineRule("\\d*", "\\d*", "ch2", statuses(PipelineStatus.BUILDING)),
                pipelineRule("\\d*", "\\d*", "ch3", statuses(PipelineStatus.PASSED)),
                pipelineRule("\\d*", "[a-z]*", "ch4", statuses(PipelineStatus.BUILDING))
        ));

        List<PipelineConfig> foundRules1 = configuration.find("abc", "efg", "ci", Status.Building.getStatus());
        assertThat(foundRules1.size(), is(1));
        assertThat(foundRules1.get(0).getNameRegex(), is("[a-z]*"));
        assertThat(foundRules1.get(0).getStageRegex(), is("[a-z]*"));
        assertThat(foundRules1.get(0).getChannel(), is("ch1"));

        List<PipelineConfig> foundRules2 = configuration.find("123", "456", "ci", Status.Building.getStatus());
        assertThat(foundRules2.size(), is(1));
        assertThat(foundRules2.get(0).getNameRegex(), is("\\d*"));
        assertThat(foundRules2.get(0).getStageRegex(), is("\\d*"));
        assertThat(foundRules2.get(0).getChannel(), is("ch2"));

        List<PipelineConfig> foundRules3 = configuration.find("123", "456", "ci", Status.Passed.getStatus());
        assertThat(foundRules3.size(), is(1));
        assertThat(foundRules3.get(0).getNameRegex(), is("\\d*"));
        assertThat(foundRules3.get(0).getStageRegex(), is("\\d*"));
        assertThat(foundRules3.get(0).getChannel(), is("ch3"));

        List<PipelineConfig> foundRules4 = configuration.find("pipeline1", "stage1", "ci", Status.Passed.getStatus());
        assertThat(foundRules4.size(), is(0));
    }

    @Test
    @Disabled
    public void shouldFindAllMatchesIfProcessAllRules() {
        Configuration configuration = new Configuration();
        configuration.setProcessAllRules(true);

        configuration.setPipelineRules(Arrays.asList(
                pipelineRule("[a-z]*", "stage\\d+", "ch1", statuses(PipelineStatus.BUILDING)),
                pipelineRule("[a-z]*", "stage2", "ch2", statuses(PipelineStatus.BUILDING))
        ));

        List<PipelineConfig> foundRules1 = configuration.find("abc", "stage1", "ci", Status.Building.getStatus());
        assertThat(foundRules1.size(), is(1));
        assertThat(foundRules1.get(0).getChannel(), is("ch1"));

        List<PipelineConfig> foundRules2 = configuration.find("abc", "stage2", "ci", Status.Building.getStatus());
        assertThat(foundRules2.size(), is(2));
        assertThat(foundRules2.get(0).getChannel(), is("ch1"));
        assertThat(foundRules2.get(1).getChannel(), is("ch2"));

        List<PipelineConfig> foundRules3 = configuration.find("abc1", "stage2", "ci", Status.Building.getStatus());
        assertThat(foundRules3.size(), is(0));
    }

    @Test
    public void shouldFindMatchAll() {
        Configuration configuration = new Configuration();

        configuration.setPipelineRules(Arrays.asList(
                pipelineRule("p1", "s1", "ch1", statuses(PipelineStatus.ALL))
        ));

        assertThat(configuration.find("p1", "s1", "ci", Status.Building.getStatus()).size(), is(1));
        assertThat(configuration.find("p1", "s1", "ci", Status.Broken.getStatus()).size(), is(1));
        assertThat(configuration.find("p1", "s1", "ci", Status.Cancelled.getStatus()).size(), is(1));
        assertThat(configuration.find("p1", "s1", "ci", Status.Failed.getStatus()).size(), is(1));
        assertThat(configuration.find("p1", "s1", "ci", Status.Failing.getStatus()).size(), is(1));
        assertThat(configuration.find("p1", "s1", "ci", Status.Fixed.getStatus()).size(), is(1));
        assertThat(configuration.find("p1", "s1", "ci", Status.Passed.getStatus()).size(), is(1));
        assertThat(configuration.find("p1", "s1", "ci", Status.Unknown.getStatus()).size(), is(1));
    }

    @Test
    public void shouldGetAPIServerHost() {
        Configuration configuration = new Configuration();

        configuration.setGoServerHost("https://gocd.com");
        assertThat(configuration.getGoAPIServerHost(), is("https://gocd.com"));

        configuration.setGoAPIServerHost("http://localhost");
        assertThat(configuration.getGoAPIServerHost(), is("http://localhost"));
    }

    @Test
    public void shouldGetAPIToken() {
        Configuration configuration = new Configuration();

        configuration.setGoAPIToken("a-valid-token-from-gocd-server");
        assertThat(configuration.getGoAPIToken(), is("a-valid-token-from-gocd-server"));
    }

    private static PipelineConfig pipelineRule(String pipeline, String stage, String channel, Set<PipelineStatus> statuses) {
        PipelineConfig pipelineConfig = new PipelineConfig(pipeline, stage);
        pipelineConfig.setStatus(statuses);
        pipelineConfig.addChannel(channel);
        return pipelineConfig;
    }

    private static Set<PipelineStatus> statuses(PipelineStatus... statuses) {
        return new HashSet<PipelineStatus>(Arrays.asList(statuses));
    }

}
