package com.crankuptheamps.ttunnel.filters;

import com.crankuptheamps.ttunnel.ConnectionProcessor;

import java.util.Properties;
import java.util.Random;

public class DisconnectFilter extends Filter {

    private final long min_uptime;
    private final long max_uptime;
    private final long timeout;
    private final long created_at = System.currentTimeMillis();
    private final String min_uptime_key = "min_uptime";
    private final String max_uptime_key = "max_uptime";

    public DisconnectFilter(final ConnectionProcessor proc, final Properties props) {
        super(proc, props);
        requireConfigKey(min_uptime_key);
        requireConfigKey(max_uptime_key);
        min_uptime = Long.parseLong(props.getProperty(min_uptime_key));
        max_uptime = Long.parseLong(props.getProperty(max_uptime_key));
        if (min_uptime > max_uptime) {
            throw new RuntimeException("min_uptime(" + min_uptime + ")" +
                    "must be less than max_uptime(" + max_uptime + ")");
        }
        final Random random = new Random();
        final long uptime = max_uptime > 0 ? min_uptime+random.nextInt((int)(max_uptime - min_uptime)):0;
        timeout = uptime + created_at;
    }


    @Override
    public int filter(int datum) {
        maybe_disconnect();
        return datum;
    }

    @Override
    public int filter(byte[] b, int off, int len) {
        maybe_disconnect();
        return len;
    }

    public void maybe_disconnect() {
        if (System.currentTimeMillis() > timeout) {
            getConnectionProcessor().disconnect();
        }
    }

}
