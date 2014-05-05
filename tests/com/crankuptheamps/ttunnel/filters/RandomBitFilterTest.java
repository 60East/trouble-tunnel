package com.crankuptheamps.ttunnel.filters;

import com.crankuptheamps.ttunnel.FilterFactory;
import com.crankuptheamps.ttunnel.MockConnectionProcessor;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;
import java.util.Random;

public class RandomBitFilterTest {

     RandomBitFilter get_filter(final float probability) {
        final Properties[] configs = new Properties[] { new Properties() };
        configs[0].setProperty("type", "RandomBit");
        configs[0].setProperty(RandomBitFilter.probability_key, String.valueOf(probability));
        final FilterFactory factory = new FilterFactory(configs);
        final Filter[] filters = factory.getInstances(new MockConnectionProcessor());
        Assert.assertEquals(configs.length, filters.length);
        return (RandomBitFilter)filters[0];
    }

    @Test
    public void print_speed() {
        final Filter filter = get_filter(1.00f);

        Random r = new Random();

        int sum = 0;
        int[] inputs = new int[10000000];
        for (int i = 0 ; i < inputs.length ; ++i) {
            inputs[i] = r.nextInt(255);
            sum += inputs[i];
        }
        int filtered_sum = 0;
        final long start = System.currentTimeMillis();
        for (int i = 0 ; i < inputs.length ; ++i) {
            filtered_sum += filter.filter(inputs[i]);
        }
        final long duration = System.currentTimeMillis() - start;
        float favg = ((float)filtered_sum) / ((float)(inputs.length));
        float avg = ((float)sum) / ((float)(inputs.length));
        System.out.println("duration: " + duration + "ms");
        System.out.println("inputs.length: " + inputs.length + " filter calls");
        System.out.println("filters/sec: " + (inputs.length * 1000) / duration);
    }

    @Test
    public void test_filter() throws Exception {
        final float target_prob = 0.90f;
        final float epsilon = 0.15f;
        final Filter f = get_filter(target_prob);
        int filter_count = 0, flipped_count = 0;
        for (int j = 0 ; j < 1000 ; ++j) {
            for (int i = 0; i < 255; ++i) {
                final int filtered = f.filter(i);
                ++filter_count;
                if (count_flipped_bits(i, filtered) > 0) {
                    Assert.assertEquals(1, count_flipped_bits(i, filtered));
                    ++flipped_count;
                }
            }
        }
        final float ratio = ((float)flipped_count) / ((float)filter_count);
        System.out.println("ratio: " + ratio);

        Assert.assertEquals(target_prob, ratio, epsilon); // Math.abs(expected - actual) < epsilon
    }

    int count_flipped_bits(final int first, final int second) {
        String s1 = RandomBitFilter.getZeroPaddedBinaryString(first);
        String s2 = RandomBitFilter.getZeroPaddedBinaryString(second);

        Assert.assertEquals(s1.length(), s2.length());

        int flips = 0;
        for (int i = 0 ; i < s1.length() ; ++i) {
            if (s1.charAt(i) != s2.charAt(i)) {
                ++flips;
            }
        }
        return flips;
    }


    @Test
    public void testTable() throws Exception {
        final RandomBitFilter f = get_filter(1.00f);
        final int[][] t = f.get_table_copy_for_unit_testing();
        for (int i = 0 ; i < t.length ; ++i) {
            for (int j = 0 ; j < t[i].length ; ++j) {
                Assert.assertTrue(t[i][j] >= 0 && t[i][j] <= 255);
                Assert.assertEquals(count_flipped_bits(i, t[i][j]), 1);
            }
        }
    }

}
