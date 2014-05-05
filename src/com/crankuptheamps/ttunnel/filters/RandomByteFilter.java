package com.crankuptheamps.ttunnel.filters;


import com.crankuptheamps.ttunnel.ConnectionProcessor;

import java.util.Properties;
import java.util.Random;

public class RandomByteFilter extends Filter {

    private final Random r = new Random(System.currentTimeMillis());
    private final float probability;
    public  static final String probability_key = "probability";

    public RandomByteFilter(final ConnectionProcessor proc, final Properties props) {
        super(proc, props);
        requireConfigKey(probability_key);
        probability = Float.parseFloat(props.getProperty(probability_key));
    }

    public int filter(int datum) {
        if (probability <= r.nextFloat()) {
            return datum;
        }
        return r.nextInt();
    }

    public int filter(byte[] b, int off, int len) {
        for (int i = 0 ; i < len ; ++i) {
            b[off + i] =  (byte)filter(b[off + i]);
        }
        return len;
    }

}
