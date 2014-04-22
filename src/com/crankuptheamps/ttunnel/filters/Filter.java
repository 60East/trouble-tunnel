package com.crankuptheamps.ttunnel.filters;

import com.crankuptheamps.ttunnel.ConnectionProcessor;
import com.crankuptheamps.ttunnel.ConnectionProcessorImpl;

import java.util.Properties;

public abstract class Filter {

    private final ConnectionProcessor connectionProcessor;
    private final Properties config;

    public Filter(ConnectionProcessor connectionProcessor, Properties config) {
        this.connectionProcessor = connectionProcessor;
        this.config = config;
    }

    public abstract int filter(int datum);

    public abstract int filter(byte[] b, int off, int len);

    protected ConnectionProcessor getConnectionProcessor() {
        return connectionProcessor;
    }

    protected void requireConfigKey(final String key) {
        if (config.getProperty(key) == null) {
            throw new Error("missing required configuration key: '" + key + "'");
        }
    }

}
