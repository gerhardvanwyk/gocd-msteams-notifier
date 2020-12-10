package com.roxorgaming.gocd.mstream.configuration;

import in.ashwanthkumar.utils.collections.Sets;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@Disabled
public class ConfigurationReaderTest {

    @Test
    public void shouldReadTestConfig() {
        Configuration configuration = ConfigReader.read("configs/test-config-1.conf");
        assertThat(configuration.isEnabled(), is(true));
        assertThat(configuration.getSlackChannel(), is("#gocd"));
        assertThat(configuration.getGoServerHost(), is("http://localhost:8080/"));
        assertThat(configuration.getPipelineRules().size(), is(2));
        assertThat(configuration.getPipelineRules().size(), is(2));
        assertThat(configuration.getDisplayConsoleLogLinks(), is(false));
        assertThat(configuration.getDisplayMaterialChanges(), is(false));

        PipelineConfig pipelineConfig1 = new PipelineConfig()
                .setNameRegex("gocd-slack-build-notifier")
                .setStageRegex(".*")
                .setGroupRegex(".*")
                .addChannel("#gocd")
                .setStatus(Sets.of(PipelineStatus.FAILED));
        assertThat(configuration.getPipelineRules(), CoreMatchers.hasItem(pipelineConfig1));

        PipelineConfig pipelineConfig2 = new PipelineConfig()
                .setNameRegex("my-java-utils")
                .setStageRegex("build")
                .setGroupRegex("ci")
                .addChannel("#gocd-build")
                .setStatus(Sets.of(PipelineStatus.FAILED));
        assertThat(configuration.getPipelineRules(), CoreMatchers.hasItem(pipelineConfig2));

        assertThat(configuration.getPipelineListener(), notNullValue());
    }

    @Test
    public void shouldReadMinimalConfig() {
        Configuration configuration = ConfigReader.read("configs/test-config-minimal.conf");

        assertThat(configuration.isEnabled(), is(true));

        assertThat(configuration.getGoLogin(), is("someuser"));
        assertThat(configuration.getGoPassword(), is("somepassword"));
        assertThat(configuration.getGoAPIToken(), is("a-valid-token-from-gocd-server"));
        assertThat(configuration.getGoServerHost(), is("http://localhost:8153/"));
        assertThat(configuration.getWebHookUrl(), is("https://hooks.slack.com/services/"));

        assertThat(configuration.getSlackChannel(), is("#build"));
        assertThat(configuration.getSlackDisplayName(), is("gocd-slack-bot"));
        assertThat(configuration.getSlackUserIcon(), is("http://example.com/slack-bot.png"));

        // Default rules
        assertThat(configuration.getPipelineRules().size(), is(1));
        assertThat(configuration.getDisplayConsoleLogLinks(), is(true));
        assertThat(configuration.getDisplayMaterialChanges(), is(true));

        PipelineConfig pipelineConfig = new PipelineConfig()
                .setNameRegex(".*")
                .setStageRegex(".*")
                .setGroupRegex(".*")
                .addChannel("#build")
                .setStatus(Sets.of(PipelineStatus.CANCELLED, PipelineStatus.BROKEN, PipelineStatus.FAILED, PipelineStatus.FIXED));
        assertThat(configuration.getPipelineRules(), CoreMatchers.hasItem(pipelineConfig));

        assertThat(configuration.getPipelineListener(), notNullValue());
    }

    @Test
    public void shouldReadMinimalConfigWithPipeline() {
        Configuration configuration = ConfigReader.read("configs/test-config-minimal-with-pipeline.conf");
        assertThat(configuration.isEnabled(), is(true));
        assertThat(configuration.getSlackChannel(), nullValue());
        assertThat(configuration.getGoServerHost(), is("https://go-instance:8153/"));
        assertThat(configuration.getWebHookUrl(), is("https://hooks.slack.com/services/"));
        assertThat(configuration.getPipelineRules().size(), is(1));

        PipelineConfig pipelineConfig = new PipelineConfig()
                .setNameRegex(".*")
                .setStageRegex(".*")
                .setGroupRegex(".*")
                .addChannel("#foo")
                .setStatus(Sets.of(PipelineStatus.FAILED));
        assertThat(configuration.getPipelineRules(), CoreMatchers.hasItem(pipelineConfig));

        assertThat(configuration.getPipelineListener(), notNullValue());
    }

    @Test
    public void shouldReadMinimalConfigWithPipelineAndEnvironmentVariables() {
        Configuration configuration = ConfigReader.read("configs/test-config-minimal-with-env-variables.conf");
        assertThat(configuration.isEnabled(), is(true));
        assertThat(configuration.getSlackChannel(), nullValue());
        assertThat(configuration.getGoServerHost(), is("https://go-instance:8153/"));
        assertThat(configuration.getWebHookUrl(), is("https://hooks.slack.com/services/"));
        assertThat(configuration.getPipelineRules().size(), is(1));
        assertThat(configuration.getGoLogin(), is(System.getenv("HOME")));

        PipelineConfig pipelineConfig = new PipelineConfig()
                .setNameRegex(".*")
                .setStageRegex(".*")
                .setGroupRegex(".*")
                .addChannel("#foo")
                .setStatus(Sets.of(PipelineStatus.FAILED));
        assertThat(configuration.getPipelineRules(), CoreMatchers.hasItem(pipelineConfig));

        assertThat(configuration.getPipelineListener(), notNullValue());
        assertThat(configuration.getProxy(), nullValue());
    }

    @Test
    public void shouldThrowExceptionIfConfigInvalid() {
        Assertions.assertThrows(RuntimeException.class, () ->
        ConfigReader.read("test-config-invalid.conf")
        );
    }

    @Test
    public void shouldReadProxyConfig() {
        Configuration configuration = ConfigReader.read("configs/test-config-with-proxy.conf");
        assertThat(configuration.isEnabled(), is(true));
        Proxy expectedProxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("localhost", 5555));
        assertThat(configuration.getProxy(), is(expectedProxy));
    }

}
