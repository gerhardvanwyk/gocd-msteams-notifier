package com.roxorgaming.gocd.mstream;

import com.roxorgaming.gocd.mstream.configuration.Configuration;
import com.roxorgaming.gocd.mstream.configuration.MsTeamsConfig;
import com.thoughtworks.go.plugin.api.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class MsTeamsClient {

    private Logger LOG = Logger.getLoggerFor(MsTeamsClient.class);

    public void push(Message message) {

        Configuration  configuration = message.getConfiguration();
        List<MsTeamsConfig> teams = configuration.getMsTeamsConfigList();
        for(MsTeamsConfig config: teams){
            for(String webhooks: config.getChannelWebhooks()){
                CloseableHttpClient httpclient = HttpClients.createDefault();
                HttpPost httpPost = new HttpPost(webhooks);
                httpPost.setHeader("Content-Type","application/json");
                EntityBuilder builder = EntityBuilder.create();
                builder.setContentType(ContentType.APPLICATION_JSON);
                try {
                    builder.setText(message.get());
                    HttpEntity httpEntity = builder.build();
                    httpPost.setEntity(httpEntity);
                    CloseableHttpResponse response = httpclient.execute(httpPost);
                    if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                        LOG.debug("Notification send to MS Team channel " + webhooks);
                    }else{
                        LOG.error("Failed to send notification to MSTeams.  Response " + EntityUtils.toString(response.getEntity()));
                    }
                } catch (IOException | URISyntaxException e) {
                    LOG.error("Failed to send notification to MSTeams.", e);
                }

            }
        }
    }

}
