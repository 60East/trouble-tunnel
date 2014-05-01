package com.crankuptheamps.ttunnel;

import com.crankuptheamps.ttunnel.filters.ChaoticFilter;
import com.crankuptheamps.ttunnel.filters.Filter;
import com.crankuptheamps.ttunnel.filters.WanFilter;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Properties;

public class ChaoticFilterTest {

    @Test
    public void testSimple() {
        int target_duration = 2000;
        final long started = System.currentTimeMillis();
        int sum = 0;
        int count;
        final Properties[] configs = new Properties[] { new Properties() };
        configs[0].setProperty("type", "Chaotic");
        final FilterFactory factory = new FilterFactory(configs);
        final Filter[] filters = factory.getInstances(new MockConnectionProcessor());
        Assert.assertEquals(configs.length, filters.length);
;
        for (count = 0 ; (System.currentTimeMillis() - started) < target_duration ; ++count) {
            final long before = System.currentTimeMillis();
            Assert.assertEquals(count, filters[0].filter(count));
            final long after = System.currentTimeMillis();
            sum += (int)(after - before);
        }
        final int actual_duration = (int)(System.currentTimeMillis() - started);
        final int avg = sum/count;
        System.out.println("sum: " + sum);
        System.out.println("count: " + count);
        System.out.println("target_duration: " + target_duration);
        System.out.println("actual_duration: " + actual_duration);
        System.out.println("avg: " + avg);
        Assert.assertTrue(avg <= 1000 && avg >= 0);
    }

}
