package com.roxorgaming.gocd.mstream;

import com.roxorgaming.gocd.msteams.jsonapi.*;
import com.roxorgaming.gocd.mstream.configuration.Configuration;
import com.roxorgaming.gocd.mstream.configuration.MsTeamsConfig;
import com.roxorgaming.gocd.mstream.configuration.PipelineConfig;
import com.roxorgaming.gocd.mstream.configuration.PipelineStatus;
import com.roxorgaming.gocd.mstream.notification.PipelineInfo;
import com.roxorgaming.gocd.mstream.notification.StageInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessageTest {

    @Test
    public void message_html() throws IOException, URISyntaxException {
        Message msg = new Message(configuration(), pipeline(),  pipelineInfo(), PipelineStatus.FAILED, getMaterialRevision());
        String html = msg.get();

        Assertions.assertEquals("<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Stage [pipeline/11/stage_name_build/11] has FAILED</title>\n" +
                "</head>\n" +
                "<body><p style=\"background-color:Red;\"><div><h1>Stage [pipeline/11/stage_name_build/11] has FAILED</h1></div><p></p><div><h3>Revisions</h3></div><div><h4> Revision 1</h4></div>Pipeline: true</div><div>Revision: 5</div><div>Comment: Git commit message</div><div>Username: username</div><div>Email: user@gmail.com</div><p></p><div>Revision: 485214789632589632145896325632</div><div>Comment: Git commit message modification 2</div><div>Username: username-agina</div><div>Email: user@gmail.com</div><p></p><div><h4> Revision 2</h4></div>Pipeline: true</div><div>Revision: 5</div><div>Comment: Git commit message</div><div>Username: username</div><div>Email: user@gmail.com</div><p></p><div>Revision: 485214789632589632145896325632</div><div>Comment: Git commit message modification 2</div><div>Username: username-agina</div><div>Email: user@gmail.com</div><p></p><div>Triggered by: null</div>\n" +
                "    <div>Reason: Trigger Message<p></p</div><div>Label: pipeline label</div><h4><div>Console Logs: </div></h4><p><div><a href=http://localhost:5183/go/files/pipeline1/11/stage_name_build/11/Job1/cruise-output/console.log>| View Job1 logs </a></div>\n" +
                "<div><a href=http://localhost:5183/go/files/pipeline1/11/stage_name_build/11/Job2/cruise-output/console.log>| View Job2 logs </a></div>\n" +
                "<div><a href=http://localhost:5183/go/files/pipeline1/11/stage_name_build/11/Job3/cruise-output/console.log>| View Job3 logs </a></div>\n" +
                "</p></body>\n" +
                "</html>", html);
    }

    private Configuration configuration(){
        Configuration configuration = Configuration.builder()
                .goServerHost("http://localhost:5183")
                .apiMsTeamsHost("http://mircosoft.com")
                .displayConsoleLogLinks(true)
                .displayMaterialChanges(true)
                .enabled(true)
                .goLogin("user")
                .goPassword("password")
                .msTeamsConfigList(msTeamsConfig())
                .build();

        return configuration;
    }

    private PipelineInfo pipelineInfo(){
        PipelineInfo pi = new PipelineInfo();
        pi.setCounter("11");
        pi.setGroup("PipelineGroup");
        pi.setName("pipeline");
        pi.setStage(StageInfo.builder()
                .counter("11")
                .name("stage_name_build")
                .state("Failed")
                .build());

        return pi;
    }

    private List<MsTeamsConfig> msTeamsConfig(){
        MsTeamsConfig config1 = new MsTeamsConfig();
        config1.setDisplayName("Test Configuration");
        config1.setIconUrl("https://tse4.mm.bing.net/th?id=OIP.0COZjQ5pixxahqLG57-PKgHaHa&pid=Api");
        config1.setTeamsId("#7889-9874");
        List<String> channels = new ArrayList<>();
        channels.add("147-987-258");
        channels.add("963-852-147");
        config1.setChannelWebhooks(channels);
        config1.setPipelineConfig(pipelineConfig());

        MsTeamsConfig config2 = new MsTeamsConfig();
        config2.setDisplayName("Test Configuration");
        config2.setIconUrl("https://tse4.mm.bing.net/th?id=OIP.ds5UtVS48_1QvqpD3tkp4AHaHA&pid=Api");
        config2.setTeamsId("#7889-9874");
        List<String> channels1 = new ArrayList<>();
        channels1.add("147-987-789");
        channels1.add("123-8547-147");
        config2.setChannelWebhooks(channels);
        config2.setPipelineConfig(pipelineConfig());

        List<MsTeamsConfig> msTeamsConfigList = new ArrayList<>();
        msTeamsConfigList.add(config1);
        msTeamsConfigList.add(config2);

        return msTeamsConfigList;

    }

    private List<PipelineConfig> pipelineConfig(){
        PipelineConfig pc = new PipelineConfig();
        pc.setGroupRegex(".*");
        pc.setNameRegex("Name");
        pc.setStageRegex("[A-Za-z0-9]+");
        List<PipelineStatus> statuses = new ArrayList<>();
        statuses.add(PipelineStatus.BROKEN);
        statuses.add(PipelineStatus.CANCELLED);

        PipelineConfig pc1 = new PipelineConfig();
        pc1.setGroupRegex(".*");
        pc1.setNameRegex("pipeline1");
        pc1.setStageRegex("[A-Za-z0-9]+");
        List<PipelineStatus> statuses1 = new ArrayList<>();
        statuses1.add(PipelineStatus.BROKEN);
        statuses1.add(PipelineStatus.CANCELLED);
        pc1.setPipelineStatus(statuses1);

        List<PipelineConfig> plc = new ArrayList<>();
        plc.add(pc);
        plc.add(pc1);
        return plc;
    }

    private Pipeline pipeline(){
        Pipeline pipeline1 = Pipeline.builder()
                 .comment("pipeline comment")
                .name("pipeline1")
                .counter(11)
                .label("pipeline label")
                .id(147)
                .buildCause(buildCause())
                .stages(stages())
                .build();

        return pipeline1;

    }

    private Stage[] stages() {
        Stage[] stages = new Stage[2];
        stages[0] = Stage.buildIt()
                .counter(11)
                .name("stage_name_build")
                .jobs(jobs())
                .result("Failed")
                .build();

        stages[1] = Stage.buildIt()
                .counter(10)
                .name("stage test")
                .jobs(jobs())
                .result("Pass")
                .build();

        return stages;
    }

    private Job[] jobs(){
        Job[] jobs = new Job[3];
        Job job1 = Job.builder()
                .name("Job1")
                .result("Passed")
                .id(1)
                .build();

        Job job2 = Job.builder()
                .name("Job2")
                .result("Passed")
                .id(2)
                .build();

        Job job3 = Job.builder()
                .name("Job3")
                .result("Passed")
                .id(3)
                .build();

        jobs[0] = job1;
        jobs[1] = job2;
        jobs[2] = job3;

        return jobs;
    }

    private BuildCause buildCause(){
        BuildCause bc = new BuildCause();
        bc.setApprover("Approve Name");
        bc.setTriggerMessage("Trigger Message");
        bc.setTriggerForced(false);
        bc.setMaterialRevisions(materialRevisions());
        return bc;
    }

    private MaterialRevision[] materialRevisions(){
        MaterialRevision mr = new MaterialRevision(true, modifications());
        mr.setMaterial(Material.builder()
                .type("Pipeline")
                .build());
        MaterialRevision mr1 = new MaterialRevision(false, modifications());
        mr1.setMaterial(Material.builder()
                .type("Pipeline")
                .fingerPrint("pipeline fingerprint")
                .build());

        MaterialRevision[] mrs = new MaterialRevision[]{mr, mr1};
        return mrs;
    }

    private List<MaterialRevision> getMaterialRevision(){
        return Arrays.asList(materialRevisions());
    }

    private List<Modification> modifications() {
        Modification md = Modification.builder()
                .comment("Git commit message")
                .email("user@gmail.com")
                .modifiedtime("14:14 14/10/2020")
                .userName("username")
                .revision("5")
                .build();

        Modification md1 = Modification.builder()
                .comment("Git commit message modification 2")
                .email("user@gmail.com")
                .modifiedtime("14:14 25/10/2020")
                .userName("username-agina")
                .revision("485214789632589632145896325632")
                .build();

        List<Modification> modifications = new ArrayList<>();
        modifications.add(md);
        modifications.add(md1);
        return modifications;
    }
}
