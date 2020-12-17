package com.roxorgaming.gocd.mstream.configuration;

import in.ashwanthkumar.utils.collections.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationReaderTest {

    @Test
    @DisplayName("Read configuration with no issues, all mandatory fields")
    public void read_config_all_good(){
        Configuration configuration = ConfigReader.read("configs/basic.conf");
        Assertions.assertFalse(configuration.isErrors());
    }

    @Test
    @DisplayName("Read all mandatory fields no issues")
    public void read_nonMandatory_fields() {
        Configuration configuration = ConfigReader.read("configs/all_config.conf");

        PipelineConfig pipelineConfig1 = PipelineConfig.builder()
                .nameRegex("gocd-slack-build-notifier")
                .groupRegex(".*")
                .stageRegex(".*")
                .pipelineStatus(Lists.of(PipelineStatus.BROKEN))
                .build();

        PipelineConfig pipelineConfig2 = PipelineConfig.builder()
                .nameRegex("my-java-utils")
                .stageRegex("build")
                .groupRegex(".*")
                .pipelineStatus(Lists.of(PipelineStatus.FAILED))
                .build();

        List<PipelineConfig> pipeLines = new ArrayList<>();
        pipeLines.add(pipelineConfig1);
        pipeLines.add(pipelineConfig2);

        List<String> channels = new ArrayList<>();
        channels.add("0345820-3345-099");
        channels.add("039485-345345");

        MsTeamsConfig msTeamsConfig = MsTeamsConfig.builder()
                .pipelineConfig(pipeLines)
                .teamsId("09-987")
                .displayName("GoCD Build Bot")
                .channels(channels)
                .iconUrl("http://iconlib.com/brokonbuild")
                .build();

        List<MsTeamsConfig> msTeamsConfigList = new ArrayList<>();
        msTeamsConfigList.add(msTeamsConfig);

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 900));

        Configuration expected = Configuration.builder().enabled(true)
                .goServerHost("http://localhost:5138")
                .apiMsTeamsHost("http://msteams-server:8080/")
                .goLogin("user1")
                .goPassword("password1")
                .goAPIToken("89777-987-098-9876543d2")
                .displayConsoleLogLinks(true)
                .displayMaterialChanges(true)
                .processAllRules(true)
                .truncateChanges(true)
                .msTeamsConfigList(msTeamsConfigList)
                .proxy(proxy)
                .build();

        Assertions.assertEquals(expected, configuration);
    }

    @Test
    @DisplayName("Invalid configuration")
    public void validate_config() {
        Exception exp = Assertions.assertThrows(
                RuntimeException.class, () -> ConfigReader.read("configs/test-config-invalid.conf"));

        Assertions.assertEquals(
                "server-host field is required for plugin configuration \n" +
                "api-msteams-host field is required for plugin configuration \n" +
                "team field is required for teams configuration \n" +
                "channels field is required for teams configuration \n" +
                "name field is required for pipeline configuration \n" +
                "invalid status iuy", exp.getMessage());

    }


}
