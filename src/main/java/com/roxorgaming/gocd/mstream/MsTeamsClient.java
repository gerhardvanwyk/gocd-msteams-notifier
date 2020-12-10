package com.roxorgaming.gocd.mstream;

import com.microsoft.graph.models.extensions.ChatMessage;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.models.extensions.ItemBody;
import com.microsoft.graph.models.generated.BodyType;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.roxorgaming.gocd.msteams.jsonapi.Message;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

public class MsTeamsClient {

    public MsTeamsClient push(Message message, Set<String> channels) {

        IGraphServiceClient graphClient = GraphServiceClient.builder().authenticationProvider(null).buildClient();

        ChatMessage chatMessage = new ChatMessage();
        ItemBody body = new ItemBody();
        body.contentType = BodyType.HTML;
        try {
            body.content = message.get();
        } catch (IOException e) {
            throw new RuntimeException("Could not read message body", e);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Could not read message body", e);
        }
        chatMessage.body = body;

        graphClient.teams("{id}").channels("{id}").messages("{id}").replies()
                .buildRequest()
                .post(chatMessage);
        return this;
    }
}
