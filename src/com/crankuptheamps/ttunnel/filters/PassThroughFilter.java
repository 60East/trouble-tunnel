package com.crankuptheamps.ttunnel.filters;

import com.crankuptheamps.ttunnel.ConnectionProcessor;

import java.util.Properties;

/**
 * Created by gibbs on 6/27/14.
 */
public class PassThroughFilter extends Filter {

    public PassThroughFilter(ConnectionProcessor connectionProcessor, Properties config) {
        super(connectionProcessor, config);
    }

    @Override
    public int filter(int datum) {
        return datum;
    }

    @Override
    public int filter(byte[] b, int off, int len) {
        return len;
    }
}

