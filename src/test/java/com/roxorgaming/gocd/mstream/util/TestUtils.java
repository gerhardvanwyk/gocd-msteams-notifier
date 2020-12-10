package com.roxorgaming.gocd.mstream.util;

import com.roxorgaming.gocd.msteams.jsonapi.GoCdClient;
import com.roxorgaming.gocd.msteams.jsonapi.ServerFactory;
import com.roxorgaming.gocd.mstream.configuration.Configuration;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtils {

    public static ServerFactory createMockServerFactory(GoCdClient goCdClient) {
        ServerFactory factory = mock(ServerFactory.class);
        when(factory.getServer(any(Configuration.class))).thenReturn(goCdClient);
        return factory;
    }

    public static String getResourceDirectory(String resource) {
        ClassLoader ldr = Thread.currentThread().getContextClassLoader();
        String url = ldr.getResource(resource).toString();
        return url.substring("file:".length(), url.lastIndexOf('/'));
    }
}
