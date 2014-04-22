package com.crankuptheamps.ttunnel;

import com.crankuptheamps.ttunnel.filters.Filter;
import com.crankuptheamps.ttunnel.filters.WanFilter;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Properties;

public class WanFilterTest {

    @Test
    public void testSimple() {
        final long[] times = new long[51];
        final long target_median = 90;
        final long margin_of_error = 10;
        final Properties config = new Properties();
        config.setProperty("median_latency", "" + target_median);
        final Filter f = new WanFilter(new MockConnectionProcessor(), config);
        for (int i = 0 ; i < times.length; ++i) {
            final long before = System.currentTimeMillis();
            f.filter(i);
            final long after = System.currentTimeMillis();
            times[i] = after - before;
        }
        Arrays.sort(times);
        final long median = times[(times.length - 1) / 2];
        Assert.assertTrue(Math.abs(target_median-median) < margin_of_error);
    }

    @Test
    public void testSimplex() {
        Properties first = new Properties();
        first.setProperty("name", "first");
        Properties second = new Properties(first);
        Assert.assertEquals(second.getProperty("name"), "first");
    }

}
