package com.roxorgaming.gocd.mstream.configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import in.ashwanthkumar.utils.collections.Sets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.roxorgaming.gocd.mstream.configuration.PipelineStatus.FAILED;
import static com.roxorgaming.gocd.mstream.configuration.PipelineStatus.PASSED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

public class PipelineConfigTest {

    @Test
    @Disabled
    public void shouldGenerateRuleFromConfig() {
        Config config = ConfigFactory.parseResources("configs/pipeline-rule-1.conf").getConfig("pipeline");
        PipelineConfig build = PipelineConfig.fromConfig(config);
        assertThat(build.getNameRegex(), is(".*"));
        assertThat(build.getStageRegex(), is(".*"));
        assertThat(build.getGroupRegex(), is(".*"));
        assertThat(build.getStatus(), hasItem(FAILED));
        assertThat(build.getChannel(), is("#gocd"));
        assertThat(build.getOwners(), is(Sets.of("ashwanthkumar", "gobot")));
    }

    @Test
    @Disabled
    public void shouldSetValuesFromDefaultsWhenPropertiesAreNotDefined() {
        Config defaultConf = ConfigFactory.parseResources("configs/default-pipeline-rule.conf").getConfig("pipeline");
        PipelineConfig defaultRule = PipelineConfig.fromConfig(defaultConf);

        Config config = ConfigFactory.parseResources("configs/pipeline-rule-2.conf").getConfig("pipeline");
        PipelineConfig build = PipelineConfig.fromConfig(config);

        PipelineConfig mergedRule = PipelineConfig.merge(build, defaultRule);
        assertThat(mergedRule.getNameRegex(), is("gocd-slack-build-notifier"));
        assertThat(mergedRule.getGroupRegex(), is("ci"));
        assertThat(mergedRule.getStageRegex(), is("build"));
        assertThat(mergedRule.getStatus(), hasItem(FAILED));
        assertThat(mergedRule.getChannel(), is("#gocd"));
        assertThat(mergedRule.getOwners(), is(Sets.of("ashwanthkumar", "gobot")));
    }

    @Test
    public void shouldMatchThePipelineAndStageAgainstRegex() {
        PipelineConfig pipelineConfig = new PipelineConfig("gocd-.*", ".*").setGroupRegex("ci").setStatus(Sets.of(FAILED, PASSED));
        Assertions.assertTrue(pipelineConfig.matches("gocd-slack-build-notifier", "build", "ci", "failed"));
        Assertions.assertTrue(pipelineConfig.matches("gocd-slack-build-notifier", "package", "ci", "passed"));
        Assertions.assertTrue(pipelineConfig.matches("gocd-slack-build-notifier", "publish", "ci", "passed"));

        Assertions.assertFalse(pipelineConfig.matches("gocd", "publish", "ci", "failed"));
    }


}
