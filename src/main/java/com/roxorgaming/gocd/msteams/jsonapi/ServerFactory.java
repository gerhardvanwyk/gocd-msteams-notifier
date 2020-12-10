package com.roxorgaming.gocd.msteams.jsonapi;

import com.roxorgaming.gocd.mstream.configuration.Configuration;

public class ServerFactory {

    public GoCdClient getServer(Configuration configuration) {
        return new GoCdClient(configuration);
    }
}
