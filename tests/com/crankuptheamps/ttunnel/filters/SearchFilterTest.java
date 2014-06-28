package com.crankuptheamps.ttunnel.filters;

import com.crankuptheamps.ttunnel.StringUtils;
import com.crankuptheamps.ttunnel.TroubleTest;
import junit.framework.Assert;
import org.junit.Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;
import com.crankuptheamps.ttunnel.StringTrafficker;

public class SearchFilterTest extends TroubleTest {

    public SearchFilterTest() {
        super(new File(new File("tests"), "work"));
    }


    @Test
    public void test_static_replacer() throws Exception {
        stop_tunnel_in(1000);
        final String lipsum = StringUtils.lipsum(20);
        final long start = System.currentTimeMillis();
        new StringTrafficker(getInt("static_replacer_local_port"),
                                    getInt("static_replacer_remote_port"),
                                    new String[][] { {"foo", "foo bar baz " + getProperty("static_replacer_search_term") + " foo bar baz"}},
                                    new String[][] { {"foo", "foo bar baz " + getProperty("static_replacer_replacement") + " foo bar baz"}}).run();
        final long duration = System.currentTimeMillis() - start;
        System.err.println("test_static_replacer duration: " + duration);
        Assert.assertTrue(duration < 5000);
    }

    @Test
    public void test_pauser() throws Exception {
        stop_tunnel_in(1000);
        final String lipsum = StringUtils.lipsum(20);
        final long start = System.currentTimeMillis();
        new StringTrafficker(getInt("pauser_local_port"),
                getInt("pauser_remote_port"),
                new String[][] { {"foo", "foo bar baz " + getProperty("pauser_search_term") + " foo bar baz"}},
                new String[][] { {"foo", "foo bar baz " + getProperty("pauser_search_term") + " foo bar baz"}}).run();
        final long duration = System.currentTimeMillis() - start;
        System.err.println("test_pauser duration: " + duration);
        Assert.assertTrue(duration > getLong("pauser_pause_duration"));
    }

}

