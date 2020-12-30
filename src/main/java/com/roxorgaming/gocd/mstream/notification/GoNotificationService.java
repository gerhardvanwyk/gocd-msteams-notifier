package com.roxorgaming.gocd.mstream.notification;

import com.roxorgaming.gocd.msteams.jsonapi.*;
import com.thoughtworks.go.plugin.api.logging.Logger;
import in.ashwanthkumar.utils.lang.StringUtils;

import java.io.IOException;
import java.util.List;

public class GoNotificationService {

    private Logger LOG = Logger.getLoggerFor(GoNotificationService.class);

    private final GoCdClient goApiClient;

    // Internal cache of pipeline history data from GoCD's JSON API.
  //  private History mRecentPipelineHistory;

    public GoNotificationService(GoCdClient goCdClient) {
        goApiClient  = goCdClient;
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

    /**
     * Try to find the build in history with the pipelineName and counter
     * @param pipelineName
     * @param counter
     * @return
     * @throws IOException
     * @throws BuildDetailsNotFoundException
     */
    public Pipeline fetchDetailsFromHistory(String pipelineName, int counter) throws  IOException,
            BuildDetailsNotFoundException
    {
        History history = goApiClient.getPipelineHistory(pipelineName);
        if (history != null) {
            List<Pipeline> pipelines = history.getPipelines();
            // Search through the builds in our recent history, and hope that
            // we can find the build we want.
            for (Pipeline build: pipelines) {
                if (build.getCounter() == counter)
                    return build;
            }
        }
        throw new BuildDetailsNotFoundException(pipelineName, counter);
    }

    /**
     * Logic to get a more correct representation of the current state of a pipeline
     * @param pipeline
     */
    public void tryToFixStageResult(PipelineInfo pipeline)
    {
        String currentStatus = pipeline.getStage().getState().toUpperCase();
        String currentResult = pipeline.getStage().getResult().toUpperCase();

        //We assume building when state = building and result = unknown
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
            history = goApiClient.getPipelineHistory(pipeline.getName());
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
        if (currentResult.equals("PASSED") && !previousResult.equals("PASSED")) {
            pipeline.getStage().setResult("Fixed");
        }

        else if (currentResult.equals("FAILED") &&
                 previousResult.equals("PASSED")) {
            pipeline.getStage().setResult("Broken");
        }
    }

    public History fetchHistory(String pipelineName) throws IOException {
        return goApiClient.getPipelineHistory(pipelineName);
    }

    public List<MaterialRevision> fetchChanges(PipelineInfo pipeline) throws IOException {
        Pipeline pipelineInstance =
                goApiClient.getPipelineInstance(pipeline.getName(), Integer.parseInt(pipeline.getCounter()));
        return pipelineInstance.rootChanges(goApiClient);
    }
}
