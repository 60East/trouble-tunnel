package com.crankuptheamps.ttunnel.filters;

import com.crankuptheamps.ttunnel.ConnectionProcessor;

import java.util.Properties;

/**
 * SpyFilter
 * Spy on a connection by writing everything to stdout. This can be useful
 * for debugging filters, for learning about a protocol, or just for
 * filling a terminal with pages and pages of cruft.
 * 
 * Created by dirkm on 11/13/14.
 */
public class SpyFilter extends Filter {

    public SpyFilter(ConnectionProcessor connectionProcessor, Properties config) {
        super(connectionProcessor, config);
    }

    @Override
    public int filter(int datum) {
        System.out.write(datum);
        return datum;
    }

    @Override
    public int filter(byte[] b, int off, int len) {
        System.out.write(b, off, len);
        return len;
    }
}

