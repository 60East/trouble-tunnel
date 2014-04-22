package com.crankuptheamps.ttunnel;

import com.crankuptheamps.ttunnel.filters.Filter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

public class ConnectionProcessorPipeTest {

    @Test
    public void simpleTest() throws Exception {
        final byte[] input = new byte[15];
        final byte[] zeroes = new byte[input.length];
        Arrays.fill(zeroes, (byte) 0);
        final int pipe_buf_size = 20;
        new Random().nextBytes(input);
        final Properties[] filter_configs = new Properties[]{
                new Properties(),
        };
        final ByteArrayOutputStream output = new ByteArrayOutputStream(input.length);

        filter_configs[0].setProperty("type", "Zero");

        final ConnectionProcessorPipe pipe = new ConnectionProcessorPipe(new ByteArrayInputStream(input),
                                             new FilterFactory(filter_configs),
                                             output,
                                             new DevnullLogger("test", 1),
                                             pipe_buf_size);
        pipe.run();
        print(input, "input");
        print(output.toByteArray(), "output");
        Assert.assertArrayEquals(zeroes, output.toByteArray());
        final Map<String, Long> stat = pipe.getStatistics();
        Assert.assertNull(pipe.getException());
        Assert.assertEquals(stat.get("exception_at").intValue(), 0);
        Assert.assertEquals(stat.get("bytes_in").intValue(), input.length);
        Assert.assertEquals(stat.get("bytes_out").intValue(), input.length);
        Assert.assertEquals(stat.get("read_count").intValue(), (pipe_buf_size / input.length) + 1); // one read results in -1
        Assert.assertEquals(stat.get("write_count").intValue(), pipe_buf_size / input.length);
        Assert.assertEquals(stat.size(), 9);
        Assert.assertTrue(stat.get("read_ms").intValue() < 100);
        Assert.assertTrue(stat.get("write_ms").longValue() < 100);
        Assert.assertTrue(stat.get("ended_at").longValue() - stat.get("began_at").longValue() < 1000);
    }

    @Test
    public void simpleWithWanFilter() throws Exception {
        final byte[] input = new byte[15];
        final byte[] zeroes = new byte[input.length];
        Arrays.fill(zeroes, (byte) 0);
        final int pipe_buf_size = 20;
        new Random().nextBytes(input);
        final Properties[] filter_configs = new Properties[]{
                new Properties(),
                new Properties(),
        };
        final ByteArrayOutputStream output = new ByteArrayOutputStream(input.length);
        final long latency = 1000;
        filter_configs[0].setProperty("type", "Zero");
        filter_configs[1].setProperty("type", "Wan");
        filter_configs[1].setProperty("median_latency", "" + latency);

        final ConnectionProcessorPipe pipe = new ConnectionProcessorPipe(new ByteArrayInputStream(input),
                new FilterFactory(filter_configs),
                output,
                new DevnullLogger("test", 1),
                pipe_buf_size);
        pipe.run();
        print(input, "input");
        print(output.toByteArray(), "output");
        Assert.assertArrayEquals(zeroes, output.toByteArray());
        final Map<String, Long> stat = pipe.getStatistics();
        Assert.assertNull(pipe.getException());
        Assert.assertEquals(stat.get("exception_at").intValue(), 0);
        Assert.assertEquals(stat.get("bytes_in").intValue(), input.length);
        Assert.assertEquals(stat.get("bytes_out").intValue(), input.length);
        Assert.assertEquals(stat.get("read_count").intValue(), (pipe_buf_size / input.length) + 1); // one read results in -1
        Assert.assertEquals(stat.get("write_count").intValue(), pipe_buf_size / input.length);
        Assert.assertEquals(stat.size(), 9);
        Assert.assertTrue(Math.abs(stat.get("read_ms").intValue()) * 1.00 >= latency*0.90);
        Assert.assertTrue(stat.get("write_ms").longValue() < 10);
        Assert.assertTrue(stat.get("ended_at").longValue() - stat.get("began_at").longValue() * 1.00 >= latency * 0.90);
    }

    private void print(byte[] bytes, String name) {
        System.out.print("byte[" + name + "]:");
        for (int i = 0 ; i < bytes.length ; ++i) {
            System.out.print("" + bytes[i]);
            if (i < bytes.length ) {
                System.out.print(", ");
            }
        }
        System.out.println();
    }

}
