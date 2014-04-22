package com.crankuptheamps.ttunnel.filters;

import com.crankuptheamps.ttunnel.ConnectionProcessor;
import com.crankuptheamps.ttunnel.ConnectionProcessorImpl;

import java.util.Properties;
import java.util.Random;

/**
 * Available WanFilter configuration properties
 * <table cellpadding="10" style="padding:10px;">
 *     <tr><th>key</th><th>type</th><th>description</th></tr>
 *     <tr>
 *         <td>median_latency</td>
 *         <td>long</td>
 *         <td>median latency introduced by filter, in milliseconds</td>
 *     </tr>
 * </table>
 */
public class WanFilter extends Filter {

    private final long medianLatency;
    private final String medianLatencyKey = "median_latency";
    private final Random r = new Random(System.currentTimeMillis());

    public WanFilter(final ConnectionProcessor proc, final Properties props) {
        super(proc, props);
        requireConfigKey(medianLatencyKey);
        medianLatency = Long.parseLong(props.getProperty(medianLatencyKey));

    }

    public int filter(int datum) {
        try {
            Thread.sleep(medianLatency);
        } catch (InterruptedException ignore) {
        }
        return datum;
    }

    public int filter(byte[] b, int off, int len) {
        try {
            Thread.sleep(medianLatency);
        } catch (InterruptedException ignore) {
        }
        return len;
    }

}
