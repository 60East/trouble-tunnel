package com.crankuptheamps.ttunnel.filters;

import com.crankuptheamps.ttunnel.ConnectionProcessor;
import com.crankuptheamps.ttunnel.ConnectionProcessorImpl;

import java.util.Arrays;
import java.util.Properties;

public class ZeroFilter  extends Filter {

    public ZeroFilter(final ConnectionProcessor proc, final Properties config) {
        super(proc, config);
    }

    public int filter(int datum) {
        return 0;
    }

    public int filter(byte[] b, int off, int len) {
        Arrays.fill(b, off, len, (byte)0);
        return len;
    }

}
