package com.roxorgaming.gocd.mstream;

import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.http.IHttpRequest;
import com.microsoft.graph.httpcore.ICoreAuthenticationProvider;
import com.microsoft.graph.models.extensions.ChatMessage;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.models.extensions.ItemBody;
import com.microsoft.graph.models.generated.BodyType;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.roxorgaming.gocd.mstream.configuration.Configuration;
import com.sun.net.httpserver.BasicAuthenticator;
import okhttp3.Request;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.List;

public class MsTeamsClient {

    public MsTeamsClient push(Message message, Configuration configuration) {

        IGraphServiceClient graphClient = GraphServiceClient.builder().authenticationProvider(authenticationProvider()).buildClient();

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

    private IAuthenticationProvider authenticationProvider() {
        //Base64.getEncoder().encodeToString()
        IAuthenticationProvider provider = request -> request.addHeader("Authentication", "Basic ");
        return provider;

    }
}
