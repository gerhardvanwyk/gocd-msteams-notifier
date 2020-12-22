package com.roxorgaming.gocd.mstream;

import com.roxorgaming.gocd.msteams.jsonapi.MaterialRevision;
import com.roxorgaming.gocd.msteams.jsonapi.Modification;
import com.roxorgaming.gocd.msteams.jsonapi.Pipeline;
import com.roxorgaming.gocd.msteams.jsonapi.Stage;
import com.roxorgaming.gocd.mstream.configuration.Configuration;
import com.roxorgaming.gocd.mstream.configuration.PipelineStatus;
import com.roxorgaming.gocd.mstream.notification.PipelineInfo;
import com.thoughtworks.go.plugin.api.logging.Logger;
import in.ashwanthkumar.utils.collections.Lists;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Building the MsTeams
 */
public class Message {

    private Logger LOG = Logger.getLoggerFor(Message.class);

    private final String title;

    private final Pipeline details;

    private final Stage stage;

    private final Configuration configuration;

    private final List<MaterialRevision> changes;

    private final PipelineStatus status;

    public Message(Configuration configuration, Pipeline details, PipelineInfo pipelineInfo,
                   PipelineStatus status, List<MaterialRevision> changes ) {
        this.configuration = configuration;
        this.status = status;
        this.details = details;
        this.changes = changes;
        this.stage = pickCurrentStage(details.getStages(), pipelineInfo.getStage().getName(), details.getName());
        this.title = String.format("Stage [%s] %s %s", pipelineInfo.fullyQualifiedJobName(), verbFor(status),
                status).replaceAll("\\s+", " ");
    }

    public String get() throws IOException, URISyntaxException {

        StringBuffer buffer = new StringBuffer("<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>");
        buffer.append(title).append("</title>\n" +
                "</head>\n" +
                "<body>");

        getStyleColor(buffer);

        buffer.append("<div><h1>").append(title).append("</h1></div>");
        buffer.append("<p>").append("</p>");

        int rc = 1;
        buffer.append("<div>").append("<h3>").append("Revisions").append("</h3></div>");
        for(MaterialRevision revision: changes){
            buffer.append("<div><h4>").append(" Revision ").append(rc++).append("</h4></div>");
            buffer.append("Pipeline: ").append(revision.isPipeline()).append("</div>");
            for(Modification md: revision.getModifications()){
                buffer.append("<div>").append("Revision: ").append(md.getRevision()).append("</div>");
                buffer.append("<div>").append("Comment: ").append(md.getComment()).append("</div>");
                buffer.append("<div>").append("Username: ").append(md.getUserName()).append("</div>");
                buffer.append("<div>").append("Email: ").append(md.getEmail()).append("</div>");
                buffer.append("<p>").append("</p>");
            }
        }

        buffer.append("<div>Triggered by: ").append(stage.getApprovedBy()).append("</div>\n" +
                "    <div>Reason: ");

        if (details.getBuildCause().isTriggerForced())
            buffer.append("Manual Trigger");
        else
            buffer.append(details.getBuildCause().getTriggerMessage());

        buffer.append("<p>").append("</p");

        buffer.append("</div>" +
                "<div>Label: ").append(details.getLabel())
                .append("</div>");

        // Describe the root changes that made up this build.
      //  rootConfigDetails(buffer);

        //Console Logs
        buffer.append("<h4>").append("<div>Console Logs: ").append("</div>").append("</h4>").append("<p>");

        List<String> consoleLogLinks = createConsoleLogLinks(configuration.getGoServerHost(), details, stage, status);
        if (!consoleLogLinks.isEmpty()) {
            for(String links: consoleLogLinks){
                buffer.append("<div>").append(links).append("</div>").append('\n');
            }
          //  String logLinks = Lists.mkString(consoleLogLinks, "", "", "\n");

        }
        buffer.append("</p></body>\n" +
                "</html>");
        LOG.info("Pushing " + title + " notification to Slack");

        return buffer.toString();
    }

    private void rootConfigDetails(StringBuffer buffer) throws URISyntaxException, IOException {
        if (configuration.isDisplayMaterialChanges()) {

            for (MaterialRevision change : changes) {
                StringBuilder sb = new StringBuilder();
                boolean isTruncated = false;
                if (configuration.isTruncateChanges() && change.getModifications().size() > 5) {
                    change.getModifications().addAll( Lists.take(change.getModifications(), 5));
                    isTruncated = true;
                }
                for (Modification mod : change.getModifications()) {
                    String url = change.modificationUrl(mod);
                    if (url != null) {
                        sb.append("<").append(url).append("|").append(mod.getRevision()).append(">");
                        sb.append(": ");
                    } else if (mod.getRevision() != null) {
                        sb.append(mod.getRevision());
                        sb.append(": ");
                    }
                    String comment = mod.summarizeComment();
                    if (comment != null) {
                        sb.append(comment);
                    }
                    if (mod.getUserName() != null) {
                        sb.append(" - ");
                        sb.append(mod.getUserName());
                    }
                    sb.append("\n");
                }
                String fieldNamePrefix = (isTruncated) ? String.format("Latest %d", 5) : "All";
                String fieldName = String.format("%s changes for %s", fieldNamePrefix, change.getMaterial().getDescription());
                buffer.append("<div>").append(fieldName).append(": ").append(sb.toString()).append("</div>");
            }

        }
    }

    private void getStyleColor(StringBuffer buffer) {
        switch (status){
            case FAILED:
            case BROKEN:{
                buffer.append("<p style=\"background-color:Red;\">");
                break;
            }
            case PASSED:
            case FIXED:{
                buffer.append("<p style=\"background-color:Green;\">");
                break;
            }
            case CANCELLED:
            case BUILDING:{
                buffer.append("<p style=\"background-color:Orange;\">");
                break;
            }
            default:
                //do nothing
        }
    }

    private List<String> createConsoleLogLinks(String host, Pipeline pipeline, Stage stage, PipelineStatus pipelineStatus)
            throws URISyntaxException {
        List<String> consoleLinks = new ArrayList<String>();
        for (String job : stage.jobNames()) {
            URI link;
            // We should be linking to Console Tab when the status is building,
            // while all others will be the console.log artifact.
            if (pipelineStatus == PipelineStatus.BUILDING) {
                link = new URI(String.format("%s/go/tab/build/detail/%s/%d/%s/%d/%s#tab-console", host, pipeline.getName(),
                        pipeline.getCounter(), stage.getName(), stage.getCounter(), job));
            } else {
                link = new URI(String.format("%s/go/files/%s/%d/%s/%d/%s/cruise-output/console.log", host, pipeline.getName(),
                        pipeline.getCounter(), stage.getName(), stage.getCounter(), job));
            }
            // TODO - May be it's only useful to show the failed job logs instead of all jobs?
            consoleLinks.add("<a href=" + link.normalize().toASCIIString() + ">" + "| View " + job + " logs </a>");
        }
        return consoleLinks;
    }

    private String verbFor(PipelineStatus pipelineStatus) {
        switch (pipelineStatus) {
            case BROKEN:
            case FIXED:
            case BUILDING:
                return "is";
            case FAILED:
            case PASSED:
                return "has";
            case CANCELLED:
                return "was";
            default:
                return "";
        }
    }

    private Stage pickCurrentStage(Stage[] stages, String stageName, String pipelineName) {
        for (Stage stage : stages) {
            if (stageName.equals(stage.getName())) {
                return stage;
            }
        }
        throw new IllegalArgumentException("The list of stages from the pipeline (" + pipelineName +
                ") doesn't have the active stage (" + stageName + ") for which we got the notification.");
    }
}
