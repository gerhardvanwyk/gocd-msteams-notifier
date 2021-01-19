package com.roxorgaming.gocd.msteams.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.HashMap;
import java.util.Map;

public class AbstractNotificationPlugin {

    private static Logger LOGGER = Logger.getLoggerFor(AbstractNotificationPlugin.class);

    /**
     * Create a configuration field for the plugin.
     *
     * @param displayName  Name of the configuration
     * @param defaultValue Default value if none provided
     * @param displayOrder Order in which it should be displayed
     * @param required     If the field is mandatory.
     * @param secure       If the data in the field should be stored encrypted.
     * @return
     */
    protected Map<String, Object> configField(String displayName, Object defaultValue, String displayOrder, boolean required, boolean secure) {
        Map<String, Object> serverUrlParams = new HashMap<>();
        serverUrlParams.put("display-name", displayName);
        serverUrlParams.put("display-value", defaultValue);
        serverUrlParams.put("display-order", displayOrder);
        serverUrlParams.put("required", required);
        serverUrlParams.put("secure", secure);
        return serverUrlParams;
    }

    protected GoPluginApiResponse renderJSON(final int responseCode, final Object response) {
        String json = null;
        try {
            json = response == null ? null : Utils.getMapper().writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not render json object", e);
        }
        LOGGER.info(json);
        DefaultGoPluginApiResponse pluginApiResponse = new DefaultGoPluginApiResponse(responseCode);
        pluginApiResponse.setResponseBody(json);
        return pluginApiResponse;
    }
}
