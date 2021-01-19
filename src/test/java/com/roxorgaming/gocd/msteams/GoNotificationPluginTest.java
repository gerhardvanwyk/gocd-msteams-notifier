package com.roxorgaming.gocd.msteams;


import com.roxorgaming.gocd.GoNotificationPlugin;
import com.roxorgaming.gocd.msteams.util.TUtils;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GoNotificationPluginTest {

    public static final String USER_HOME = "user.home";

    public static final String NOTIFICATION_INTEREST_RESPONSE = "{\"notifications\":[\"stage-status\"]}";
    public static final String GET_CONFIGURATION_RESPONSE = "{\"pipelineConfig\":{\"display-value\":\"\"," +
            "\"display-order\":\"2\",\"display-name\":\"Pipeline Notification Rules\",\"secure\":false," +
            "\"required\":true},\"server-url-external\":{\"display-value\":\"\",\"display-order\":\"1\"," +
            "\"display-name\":\"External GoCD Server URL\",\"secure\":false,\"required\":true}}";
    private static final String GET_CONFIG_VALIDATION_RESPONSE = "[]";

    @Test
    public void canHandleConfigValidationRequest() {
        Assertions.fail();
        GoNotificationPlugin plugin = createGoNotificationPluginFromConfigAtHomeDir();

        GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestName()).thenReturn("go.plugin-settings.validate-configuration");
        when(request.requestBody()).thenReturn("{\"plugin-settings\":" +
                "{\"external_server_url\":{\"value\":\"bob\"}}}");

        GoPluginApiResponse rv = plugin.handle(request);

        assertThat(rv, is(notNullValue()));
        assertThat(rv.responseBody(), equalTo(GET_CONFIG_VALIDATION_RESPONSE));
    }

    @Test
    public void canHandleConfigurationRequest() {
        GoNotificationPlugin plugin = createGoNotificationPluginFromConfigAtHomeDir();

        GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestName()).thenReturn("go.plugin-settings.get-configuration");

        GoPluginApiResponse rv = plugin.handle(request);

        assertThat(rv, is(notNullValue()));
        assertThat(rv.responseBody(), equalTo(GET_CONFIGURATION_RESPONSE));
    }

    @Test
    public void canHandleGetViewRequest() {
        GoNotificationPlugin plugin = createGoNotificationPluginFromConfigAtHomeDir();

        GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestName()).thenReturn("go.plugin-settings.get-view");

        GoPluginApiResponse rv = plugin.handle(request);

        assertThat(rv, is(notNullValue()));
        assertThat(rv.responseBody(), containsString("<div class=\\\""));
    }

    @Test
    public void canHandleNotificationInterestedInRequest() {
        GoNotificationPlugin plugin = createGoNotificationPluginFromConfigAtHomeDir();

        GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestName()).thenReturn("notifications-interested-in");

        GoPluginApiResponse rv = plugin.handle(request);

        assertThat(rv, is(notNullValue()));
        assertThat(rv.responseBody(), equalTo(NOTIFICATION_INTEREST_RESPONSE));
    }

    @Test
    public void canHandleNotificationInterestedInRequestForConfigFromEnvVariable() {
        GoNotificationPlugin plugin = createGoNotificationPluginFromConfigAtEnvironmentVariableLocation("GO_NOTIFY_CONF");

        GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestName()).thenReturn("notifications-interested-in");

        GoPluginApiResponse rv = plugin.handle(request);

        assertThat(rv, is(notNullValue()));
        assertThat(rv.responseBody(), equalTo(NOTIFICATION_INTEREST_RESPONSE));
    }

    @Test
    public void canHandleNotificationInterestedInRequestForConfigFromGoServerPath() {
        GoNotificationPlugin plugin = createGoNotificationPluginFromConfigAtEnvironmentVariableLocation("CRUISE_SERVER_DIR");

        GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestName()).thenReturn("notifications-interested-in");

        GoPluginApiResponse rv = plugin.handle(request);

        assertThat(rv, is(notNullValue()));
        assertThat(rv.responseBody(), equalTo(NOTIFICATION_INTEREST_RESPONSE));
    }

    public GoNotificationPlugin createGoNotificationPluginFromConfigAtHomeDir() {
        String folder = TUtils.getResourceDirectory("configs/basic.conf");

        String oldUserHome = System.getProperty(USER_HOME);
        System.setProperty(USER_HOME, folder);
        GoEnvironment goEnvironment = new GoEnvironment().setEnv("GO_NOTIFY_CONF", folder + File.separator + "reference.conf");
        GoNotificationPlugin plugin = new GoNotificationPlugin(goEnvironment);


        System.setProperty(USER_HOME, oldUserHome);
        return plugin;
    }

    public GoNotificationPlugin createGoNotificationPluginFromConfigAtEnvironmentVariableLocation(String envVariable) {
        String folder = TUtils.getResourceDirectory("configs/basic.conf");
        GoEnvironment goEnvironment = new GoEnvironment().setEnv(envVariable, folder + File.separator + "reference.conf");
        return new GoNotificationPlugin(goEnvironment);
    }

}
