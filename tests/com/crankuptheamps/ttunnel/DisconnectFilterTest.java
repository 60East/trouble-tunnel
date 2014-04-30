package com.crankuptheamps.ttunnel;

import com.crankuptheamps.ttunnel.filters.DisconnectFilter;
import com.crankuptheamps.ttunnel.filters.Filter;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

public class DisconnectFilterTest {

    @Test
    public void testBadRange() throws Exception {
        final Properties config = new Properties();
        final String min_uptime = "2000";
        final String max_uptime = "200";
        config.setProperty("min_uptime",  min_uptime);
        config.setProperty("max_uptime",   max_uptime);
        final ConnectionProcessor proc = new MockConnectionProcessor();
        try {
            final Filter f = new DisconnectFilter(proc, config);
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getMessage().indexOf(min_uptime) != -1);
            Assert.assertTrue(e.getMessage().indexOf(max_uptime) != -1);
        }
    }

    @Test
    public void testDisconnectOnce() throws Exception {
        final Properties config = new Properties();
        final int min_uptime = 20;
        final int max_uptime = 2000;
        config.setProperty("min_uptime",  "" + min_uptime);
        config.setProperty("max_uptime",   "" + max_uptime);
        final long started = System.currentTimeMillis();
        final MockConnectionProcessor proc = new MockConnectionProcessor();
        final Filter f = new DisconnectFilter(proc, config);
        while ((System.currentTimeMillis() - started) < max_uptime) {
            Assert.assertEquals(10, f.filter(10));
            Thread.sleep(max_uptime - min_uptime);
        }
        Assert.assertEquals(1, proc.disconnect_count);
    }

    @Test
    public void testDisconnectDueToMin() throws Exception {
        final Properties config = new Properties();
        final int min_uptime = 20;
        final int max_uptime = 2000;
        config.setProperty("min_uptime",  "" + min_uptime);
        config.setProperty("max_uptime",   "" + max_uptime);
        final long started = System.currentTimeMillis();
        final MockConnectionProcessor proc = new MockConnectionProcessor();
        final Filter f = new DisconnectFilter(proc, config);
        while (proc.disconnect_count == 0) {
            Assert.assertEquals(10, f.filter(10));
            Thread.sleep(min_uptime);
        }
        final long age = System.currentTimeMillis() - started;
        Assert.assertTrue(age < max_uptime && age > min_uptime);
    }
    @Test
    public void testDisconnectDueToMax() throws Exception {
        final Properties config = new Properties();
        final int min_uptime = 20;
        final int max_uptime = 2000;
        config.setProperty("min_uptime",  "" + min_uptime);
        config.setProperty("max_uptime",   "" + max_uptime);
        final long started = System.currentTimeMillis();
        final MockConnectionProcessor proc = new MockConnectionProcessor();
        final Filter f = new DisconnectFilter(proc, config);
        while (proc.disconnect_count == 0) {
            Assert.assertEquals(10, f.filter(10));
            Thread.sleep(max_uptime);
        }
        final long age = System.currentTimeMillis() - started;
        Assert.assertTrue(age > max_uptime && age > min_uptime);
    }

}
