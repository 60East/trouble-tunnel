package com.crankuptheamps.ttunnel.filters;

import com.crankuptheamps.ttunnel.ConnectionProcessor;
import com.crankuptheamps.ttunnel.ConnectionProcessorImpl;

import java.util.Arrays;
import java.util.Properties;

public class NoFilter  extends Filter {

    public NoFilter(final ConnectionProcessor proc, final Properties config) {
        super(proc, config);
    }

    public int filter(int datum) {
        log("datum-" + datum + "returning " + datum);
        return datum;
    }

    public int filter(byte[] b, int off, int len) {
        log(new String(b, off, len)  + "returning" + len);
        return len;
    }

    private void log(final String msg) {
//        System.out.println("[[[[[NoFilter-" + Thread.currentThread().getName() + "-" + this.hashCode() + ": " + msg + "]]]]");
//        System.out.flush();
    }

}
