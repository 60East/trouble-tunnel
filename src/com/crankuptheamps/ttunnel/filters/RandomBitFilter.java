package com.crankuptheamps.ttunnel.filters;


import com.crankuptheamps.ttunnel.ConnectionProcessor;

import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

public class RandomBitFilter extends Filter {

    private final Random r = new Random(System.currentTimeMillis());
    private final float probability;
    private final int[][] table = new int[256][8];
    public static final String probability_key = "probability";

    public RandomBitFilter(final ConnectionProcessor proc, final Properties props) {
        super(proc, props);
        requireConfigKey(probability_key);
        probability = Float.parseFloat(props.getProperty(probability_key));
        for (int i = 0 ; i < table.length ; ++i) {
            for (int j = 24 ; j < 32 ; ++j) {
                table[i][j-24] = flip_bit_at(i, j);
            }
        }
    }

    public int filter(final int datum) {
        int ret = datum;
        if (probability > r.nextFloat()) {
            try {
                ret = table[datum][r.nextInt(8)];
            } catch (ArrayIndexOutOfBoundsException e) {
                throw e;
            }
        }
        return ret;
    }

    public int filter(final byte[] b, final int off, final int len) {
        for (int i = 0 ; i < len ; ++i) {
            b[off + i] =  (byte)filter(b[off + i]);
        }
        return len;
    }

    int[][] get_table_copy_for_unit_testing() {
         final int[][] t = new int[256][8];
         for (int i = 0 ; i < t.length ; ++i) {
		 					for (int j = 0 ; j < t[i].length ; ++j) {
							 	t[i][j] = table[i][j];
							}
         }
         return t;
    }

    public static int flip_bit_at(final int val, final int at_index) {
        assert(at_index >= 24); // we're only flipping bits in least sig byte of val
        final char[] chars = getZeroPaddedBinaryString(val).toCharArray();
        chars[at_index] = chars[at_index] == '1' ? '0' : '1';
        final int i = Integer.parseInt(new String(chars), 2);
        return i;
    }

    static String getZeroPaddedBinaryString(final int i) {
        String s = Integer.toBinaryString(i);
        while (s.length() < 32) {
            s = "0" + s;
        }
        return s;
    }
}
