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

    /**
     * @param datum the next byte of data from the input stream.
     *              0 <= datum <= 255.
     *
     * @return the next byte of filtered data
     */
    public abstract int filter(int datum);

    /**
     *
     * @param b     the buffer into which the data has been read
     * @param off   the start offset in array b at which the data was written
     * @param len   the number of bytes that were read, len >= 0
     * @return      the number of bytes that were read
     */
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
