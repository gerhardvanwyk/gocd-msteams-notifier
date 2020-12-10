package com.roxorgaming.gocd.mstream.configuration;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

public class ConfigReader {
    private Logger LOG = Logger.getLoggerFor(ConfigReader.class);

    public static Configuration read() {
        return new ConfigReader().load();
    }

    public static Configuration read(File file) {
        return new ConfigReader().load(file);
    }

    public static Configuration read(String file) {
        return new ConfigReader().load(ConfigFactory.parseResources(file));
    }

    protected Configuration load(Config config) {
        Config envThenSystem = ConfigFactory.systemEnvironment().withFallback(ConfigFactory.systemProperties());
        Config configWithFallback = config.withFallback(ConfigFactory.load(getClass().getClassLoader())).resolveWith(envThenSystem);
        return Configuration.fromConfig(configWithFallback.getConfig("gocd.msteams"));
    }

    public Configuration load() {
        return load(ConfigFactory.load());
    }

    public Configuration load(File file) {
        return load(ConfigFactory.parseFile(file));
    }
}
