package com.crankuptheamps.ttunnel;

import com.crankuptheamps.ttunnel.filters.Filter;
import com.crankuptheamps.ttunnel.filters.WanFilter;
import com.crankuptheamps.ttunnel.filters.ZeroFilter;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

public class TroubleInputStreamTest {

    final int BYTES_PER_KB = 1024;
    final int BYTES_PER_MB = BYTES_PER_KB * 1024;
    final int BYTES_PER_GB = 1024 * BYTES_PER_MB;

    @Test
    public void testBytelArrayRead() throws Exception{
        final byte[] input = new byte[10 * BYTES_PER_MB];
        final byte[] output = new byte[input.length];
        final byte[] expected = new byte[input.length];
        Arrays.fill(expected, (byte)0);
        new Random().nextBytes(input);
        final ConnectionProcessor cp = new MockConnectionProcessor();
        final Filter[] filters = new Filter[]{ new ZeroFilter(cp, new Properties())};
        new TroubleInputStream(new ByteArrayInputStream(input), filters).read(output);
        Assert.assertArrayEquals(expected, output);
    }



}
