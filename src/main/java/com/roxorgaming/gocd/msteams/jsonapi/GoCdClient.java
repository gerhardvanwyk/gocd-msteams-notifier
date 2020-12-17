package com.roxorgaming.gocd.msteams.jsonapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roxorgaming.gocd.mstream.base.Utils;
import com.roxorgaming.gocd.mstream.configuration.Configuration;
import com.thoughtworks.go.plugin.api.logging.Logger;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import static in.ashwanthkumar.utils.lang.StringUtils.isNotEmpty;

/**
 * Actual methods for contacting the remote server.
 */
public class GoCdClient {
    private Logger LOG = Logger.getLoggerFor(GoCdClient.class);

    private ObjectMapper mapper = Utils.getMapper();

    // Contains authentication credentials, etc.
    private Configuration mConfiguration;

    /**
     * Construct a new server object, using credentials from Rules.
     */
    public GoCdClient(Configuration configuration) {
        this.mConfiguration = configuration;

    }

    /**
     * Get the recent history of a pipeline.
     */
    public History getPipelineHistory(String pipelineName) throws IOException {
        URL url = historyUrl(pipelineName);
        List historyPipelines = getPipeline(url, List.class, null);
        History history = new History();
        history.setPipelines(historyPipelines);
        return history;
    }

    /**
     * Get a specific instance of a pipeline.
     */
    public Pipeline getPipelineInstance(String pipelineName, int pipelineCounter) throws IOException {
        URL url = pipelineUrl(pipelineName, pipelineCounter);
        Pipeline pipeline = getPipeline(url, Pipeline.class, null);
        return pipeline;
    }

    <T> T getPipeline(URL url, Class<T> type, HttpURLConnection connection) throws IOException {
        URL normalizedUrl;
        try {
            normalizedUrl = url.toURI().normalize().toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
        LOG.info("Fetching " + normalizedUrl.toString());

        String content = (String) getResponse(url, connection);

        return mapper.readValue(content.getBytes(), type);
    }

    private Object getResponse(URL url, HttpURLConnection testConn) {
        try {
            if (testConn == null) {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                configureRequest(connection);
                connection.connect();
                return connection.getContent();
            }
            //This is used for testing
            Object content = testConn.getContent();
            return content;
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    // Add in our HTTP authorization credentials if we have them.
    // Favor the API Token over username/password
    void configureRequest(HttpURLConnection request) {
        String authHeader = null;
        if (isNotEmpty(mConfiguration.getGoAPIToken())) {
            authHeader = "Bearer " + mConfiguration.getGoAPIToken();
        } else if (isNotEmpty(mConfiguration.getGoLogin()) && isNotEmpty(mConfiguration.getGoPassword())) {
            String userpass = mConfiguration.getGoLogin() + ":" + mConfiguration.getGoPassword();
            authHeader = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes());
        }
        if (authHeader != null) {
            request.setRequestProperty("Authorization", authHeader);
        }
    }

    URL historyUrl(String pipelineName){
        try {
            String url = String.format("%s/go/api/pipelines/%s/history",
                    mConfiguration.getGoServerHost(), pipelineName);
           return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not create URL for " + pipelineName, e);
        }
    }

    URL pipelineUrl(String pipelineName, int pipelineCounter){
        try {
            String url = String.format("%s/go/api/pipelines/%s/%d",
                    mConfiguration.getGoServerHost(), pipelineName, pipelineCounter);
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not create URL for " + pipelineName, e);
        }
    }
}
