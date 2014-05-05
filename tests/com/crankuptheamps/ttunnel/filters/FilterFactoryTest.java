package com.crankuptheamps.ttunnel;

import com.crankuptheamps.ttunnel.filters.Filter;
import com.crankuptheamps.ttunnel.filters.WanFilter;
import com.crankuptheamps.ttunnel.filters.ZeroFilter;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

public class FilterFactoryTest {

    @Test
    public void testSimple() throws Exception {
        final Properties p = new Properties();
        p.setProperty("type", "Zero");
        final Properties[] configs = new Properties[]{p};
        final FilterFactory ff = new FilterFactory(configs);
        final Filter[] filters = ff.getInstances(new MockConnectionProcessor());
        Assert.assertEquals(1, filters.length);
        Assert.assertEquals(ZeroFilter.class.getName(), filters[0].getClass().getName());
    }

    @Test
    public void testMultipleFilters() throws Exception {
        final Properties[] configs = new Properties[] { new Properties(), new Properties() };
        configs[0].setProperty("type", "Zero");
        configs[1].setProperty("type", "Wan");
        configs[1].setProperty("median_latency", "250");
        final FilterFactory ff = new FilterFactory(configs);
        final Filter[] filters = ff.getInstances(new MockConnectionProcessor());
        Assert.assertEquals(configs.length, filters.length);
        Assert.assertEquals(filters[0].getClass().getName(), ZeroFilter.class.getName());
        configs[1].setProperty("median_latency", "200");
        Assert.assertEquals(filters[1].getClass().getName(), WanFilter.class.getName());
    }

    @Test
    public void testMissingType() throws Exception {
        final Properties p = new Properties();
        p.setProperty("type", "BOGUS");
        final Properties[] configs = new Properties[]{p};
        final FilterFactory ff = new FilterFactory(configs);
        try {
            final Filter[] filters = ff.getInstances(new MockConnectionProcessor());
        } catch (Throwable t) {
            Assert.assertEquals("failed to load class for Filter type 'BOGUS'", t.getMessage());
        }
    }

    @Test
    public void testInvalidType() throws Exception {
        final Properties p = new Properties();
        p.setProperty("type", "java.lang.String");
        final Properties[] configs = new Properties[]{p};
        final FilterFactory ff = new FilterFactory(configs);
        try {
            final Filter[] filters = ff.getInstances(new MockConnectionProcessor());
        } catch (Throwable t) {
            Assert.assertTrue(t.getMessage().indexOf("trouble creating an instance of'java.lang.String'") != -1);
        }
    }

}
