package com.roxorgaming.gocd.msteams.jsonapi;

import com.roxorgaming.gocd.mstream.notification.GoNotificationMessage;
import com.roxorgaming.gocd.mstream.configuration.PipelineStatus;
import com.roxorgaming.gocd.mstream.configuration.Configuration;
import com.thoughtworks.go.plugin.api.logging.Logger;
import in.ashwanthkumar.utils.collections.Lists;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Message {

    private Logger LOG = Logger.getLoggerFor(Message.class);

    private final String title;

    private final Pipeline details;

    private final Stage stage;

    private final Configuration configuration;

    private final GoNotificationMessage message;

    private final PipelineStatus status;

    public Message(Configuration configuration, PipelineStatus status, GoNotificationMessage message) {
        this.configuration = configuration;
        this.message = message;
        this.status = status;
        try {
            this.details = message.fetchDetails(configuration);
        } catch (IOException | GoNotificationMessage.BuildDetailsNotFoundException e) {
            throw new RuntimeException("Could not fetch message details", e);
        }
        this.stage = pickCurrentStage(details.getStages(), message);
        this.title = String.format("Stage [%s] %s %s", message.fullyQualifiedJobName(), verbFor(status),
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

        buffer.append("<div>Triggered by: ").append(stage.getApprovedBy()).append("</div>\n" +
                "    <div>Reason: ");

        if (details.getBuildCause().isTriggerForced())
            buffer.append("Manual Trigger");
        else
            buffer.append(details.getBuildCause().getTriggerMessage());

        buffer.append("</div>" +
                "<div>Label: ").append(details.getLabel())
                .append("</div>");

        // Describe the root changes that made up this build.
        rootConfigDetails(buffer);

        List<String> consoleLogLinks = createConsoleLogLinks(configuration.getGoServerHost(), details, stage, status);
        if (!consoleLogLinks.isEmpty()) {
            String logLinks = Lists.mkString(consoleLogLinks, "", "", "\n");
            buffer.append("<div>Console Logs: ").append(logLinks).append("</div>");
        }
        buffer.append("</p></body>\n" +
                "</html>");
        LOG.info("Pushing " + title + " notification to Slack");

        return buffer.toString();
    }

    private void rootConfigDetails(StringBuffer buffer) throws URISyntaxException, IOException {
        if (configuration.isDisplayMaterialChanges()) {

            List<MaterialRevision> changes = message.fetchChanges(configuration);
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
                buffer.append("<div>").append(fieldName).append(": ").append(sb.toString()).append("</div");
            }

        }
    }

    private void getStyleColor(StringBuffer buffer) {
        switch (status){
            case FAILED:
            case BROKEN:{
                buffer.append("<p style=\"background-color:Red;\">");
            }
            case PASSED:
            case FIXED:{
                buffer.append("<p style=\"background-color:Green;\">");
            }
            case CANCELLED:
            case BUILDING:{
                buffer.append("<p style=\"background-color:Orange;\">");
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
            consoleLinks.add("<" + link.normalize().toASCIIString() + "| View " + job + " logs>");
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

    private Stage pickCurrentStage(Stage[] stages, GoNotificationMessage message) {
        for (Stage stage : stages) {
            if (message.getStageName().equals(stage.getName())) {
                return stage;
            }
        }
        throw new IllegalArgumentException("The list of stages from the pipeline (" + message.getPipelineName() +
                ") doesn't have the active stage (" + message.getStageName() + ") for which we got the notification.");
    }
}
