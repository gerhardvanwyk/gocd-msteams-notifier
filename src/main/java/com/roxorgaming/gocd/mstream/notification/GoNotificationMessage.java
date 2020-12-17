package com.roxorgaming.gocd.mstream.notification;

import com.google.gson.annotations.SerializedName;
import com.roxorgaming.gocd.msteams.jsonapi.*;
import com.roxorgaming.gocd.mstream.PipelineInfo;
import com.roxorgaming.gocd.mstream.configuration.Configuration;
import com.thoughtworks.go.plugin.api.logging.Logger;
import in.ashwanthkumar.utils.lang.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class GoNotificationMessage {

    private Logger LOG = Logger.getLoggerFor(GoNotificationMessage.class);

    private final ServerFactory serverFactory;

    @SerializedName("pipeline")
    private PipelineInfo pipeline;

    // Internal cache of pipeline history data from GoCD's JSON API.
    private History mRecentPipelineHistory;

    public GoNotificationMessage() {
        serverFactory = new ServerFactory();
    }

    public GoNotificationMessage(ServerFactory serverFactory, PipelineInfo pipeline) {
        this.serverFactory = serverFactory;
        this.pipeline = pipeline;
    }

    /**
     * Raised when we can't find information about our build in the array
     * returned by the server.
     */
    static public class BuildDetailsNotFoundException extends Exception {
        public BuildDetailsNotFoundException(String pipelineName,
                                             int pipelineCounter)
        {
            super(String.format("could not find details for %s/%d",
                                pipelineName, pipelineCounter));
        }
    }

    public String goServerUrl(String host) throws URISyntaxException {
        return new URI(String.format("%s/go/pipelines/%s/%s/%s/%s", host, pipeline.getName(), pipeline.getCounter(),
                pipeline.getStage().getName(), pipeline.getStage().getCounter())).normalize().toASCIIString();
    }

    public String fullyQualifiedJobName() {
        return pipeline.getName() + "/" + pipeline.getCounter() + "/" + pipeline.getStage().getName() + "/" + pipeline.getStage().getCounter();
    }

    public String getPipelineName() {
        return pipeline.getName();
    }

    public String getPipelineCounter() {
        return pipeline.getCounter();
    }

    public String getStageName() {
        return pipeline.getStage().getName();
    }

    public String getStageCounter() {
        return pipeline.getStage().getCounter();
    }

    public String getStageState() {
        return pipeline.getStage().getState();
    }

    public String getStageResult() {
        return pipeline.getStage().getResult();
    }

    public String getCreateTime() {
        return pipeline.getStage().getCreateTime();
    }

    public String getLastTransitionTime() {
        return pipeline.getStage().getLastTransitionTime();
    }

    public String getPipelineGroup() {
        return pipeline.getGroup();
    }

    /**
     * Fetch the full history of this pipeline from the server.  We can't
     * get specify a specific version, unfortunately.
     */
    public History fetchRecentPipelineHistory(Configuration configuration)
        throws IOException
    {
        if (mRecentPipelineHistory == null) {
            GoCdClient goCdClient = serverFactory.getServer(configuration);
            mRecentPipelineHistory = goCdClient.getPipelineHistory(pipeline.getName());
        }
        return mRecentPipelineHistory;
    }

    public Pipeline fetchDetailsForBuild(Configuration configuration, int counter) throws  IOException,
            BuildDetailsNotFoundException
    {
        History history = fetchRecentPipelineHistory(configuration);
        if (history != null) {
            List<Pipeline> pipelines = history.getPipelines();
            // Search through the builds in our recent history, and hope that
            // we can find the build we want.
            for (Pipeline build: pipelines) {
                if (build.getCounter() == counter)
                    return build;
            }
        }
        throw new BuildDetailsNotFoundException(getPipelineName(), counter);
    }

    public void tryToFixStageResult(Configuration configuration)
    {
        String currentStatus = pipeline.getStage().getState().toUpperCase();
        String currentResult = pipeline.getStage().getResult().toUpperCase();
        if (currentStatus.equals("BUILDING") && currentResult.equals("UNKNOWN")) {
            pipeline.getStage().setResult("Building");
            return;
        }
        // We only need to double-check certain messages; the rest are
        // trusty-worthy.
        if (!currentResult.equals("PASSED") && !currentResult.equals("FAILED"))
            return;

        // Fetch our history.  If we can't get it, just give up; this is a
        // low-priority tweak.
        History history = null;
        try {
            history = fetchRecentPipelineHistory(configuration);
        } catch(Exception e) {
            LOG.warn(String.format("Error getting pipeline history: " +
                                   e.getMessage()));
            return;
        }

        // Figure out whether the previous run of this stage passed or failed.
        Stage previous = history.previousRun(Integer.parseInt(pipeline.getCounter()),
                                             pipeline.getStage().getName(),
                                             Integer.parseInt(pipeline.getStage().getCounter()));
        if (previous == null || StringUtils.isEmpty(previous.getResult())) {
            LOG.info("Couldn't find any previous run of " +
                     pipeline.getName() + "/" + pipeline.getCounter() + "/" +
                     pipeline.getStage().getName() + "/" + pipeline.getStage().getCounter());
            return;
        }
        String previousResult = previous.getResult().toUpperCase();

        // Fix up our build status.  This is slightly asymmetrical, because
        // we want to be quicker to praise than to blame.  Also, I _think_
        // that the typical representation of stageResult is initial caps
        // only, but our callers should all be using toUpperCase on it in
        // any event.
        //LOG.info("current: "+currentResult + ", previous: "+previousResult);
        if (currentResult.equals("PASSED") && !previousResult.equals("PASSED"))
            pipeline.getStage().setResult("Fixed");
        else if (currentResult.equals("FAILED") &&
                 previousResult.equals("PASSED"))
            pipeline.getStage().setResult( "Broken");
    }

    public Pipeline fetchDetails(Configuration configuration) throws IOException, BuildDetailsNotFoundException
    {
        return fetchDetailsForBuild(configuration, Integer.parseInt(getPipelineCounter()));
    }

    public List<MaterialRevision> fetchChanges(Configuration configuration) throws IOException
    {
        GoCdClient goCdClient = serverFactory.getServer(configuration);
        Pipeline pipelineInstance =
            goCdClient.getPipelineInstance(pipeline.getName(), Integer.parseInt(pipeline.getCounter()));
        return pipelineInstance.rootChanges(goCdClient);
    }
}
