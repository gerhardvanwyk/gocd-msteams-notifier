package com.roxorgaming.gocd.msteams.util;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

public class TUtils {

//    public static ServerFactory createMockServerFactory(GoCdClient goCdClient) {
//        ServerFactory factory = mock(ServerFactory.class);
//        when(factory.getServer(any(Configuration.class))).thenReturn(goCdClient);
//        return factory;
//    }

    public static String getResourceDirectory(String resource) {
        ClassLoader ldr = Thread.currentThread().getContextClassLoader();
        String url = ldr.getResource(resource).toString();
        return url.substring("file:".length(), url.lastIndexOf('/'));
    }
}
