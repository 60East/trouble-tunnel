package com.crankuptheamps.ttunnel;

import junit.framework.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;

public class ConnectionProcessorImplTest {

    @Test
    public void testGetGoogle() throws Exception {
        final Properties[] configs = new Properties[] {
                new Properties(),
        };
        configs[0].setProperty("type", "No");
        String site = "www.google.com";
        String expected_substring = "domain=.google.com";
//        site = "arstechnica.com";
//        expected_substring = "arstechnica";
        final Socket sock = new Socket(site, 80);
        sock.setSoTimeout(1000);
        final byte[] get = ("GET /index.html HTTP/1.1\nhost: " + site + "\n\n").getBytes();
        final InputStream localIn = new ByteArrayInputStream(get);
        final ByteArrayOutputStream localOut = new ByteArrayOutputStream();
        final ConnectionProcessorImpl proc = new ConnectionProcessorImpl(localIn, localOut,
                sock.getInputStream(),
                sock.getOutputStream(),
                "test_route",
                null,
                configs);
        Thread t = new Thread(proc);
        t.start();
        t.join();
//        System.out.println("output: '" + new String(localOut.toByteArray()) + "'");
        Assert.assertTrue(new String(localOut.toByteArray()).indexOf(expected_substring) != -1);
        Assert.assertEquals(proc.pipes[0].getStatistics().get("bytes_in"), proc.pipes[0].getStatistics().get("bytes_out"));
        Assert.assertEquals(proc.pipes[1].getStatistics().get("bytes_in"), proc.pipes[1].getStatistics().get("bytes_out"));
        Assert.assertEquals("Read timed out", proc.getException().getMessage());
    }

}
