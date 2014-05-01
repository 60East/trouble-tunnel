package com.crankuptheamps.ttunnel.filters;

import com.crankuptheamps.ttunnel.ConnectionProcessor;
import com.crankuptheamps.ttunnel.ConnectionProcessorImpl;

import java.util.Properties;
import java.util.Random;

/**
 * The chaoticFilter is a latency-inducing filter like the WanFilter.
 * Unlike WanFilter, it takes no configuration parameters but introduces
 * a random latency > between 0ms and 1000ms on each filter call.
 * </table>
 */
public class ChaoticFilter extends Filter {

    private final Random r = new Random(System.currentTimeMillis());

    public ChaoticFilter(final ConnectionProcessor proc, final Properties props) {
        super(proc, props);
    }

    public int filter(int datum) {
        try {
            Thread.sleep(r.nextInt(1000));
        } catch (InterruptedException ignore) {
        }
        return datum;
    }

    public int filter(byte[] b, int off, int len) {
        try {
            Thread.sleep(r.nextInt(1000));
        } catch (InterruptedException ignore) {
        }
        return len;
    }

}
