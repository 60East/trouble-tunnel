package com.crankuptheamps.ttunnel;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Properties;

public class ConfigParserTest {

    public ConfigParserTest() {

    }

    @Test
    public void testParse() throws Exception {
        final ConfigParser cp = new ConfigParser();
        cp.parse(new File("tests/sample-config.json"), new ConfigHandler() {

            public void config(String name, String remote_addr, int listen_on, String log_dir, Properties[] filter_configs) {
                if (name.equals("AB")) {
                    Assert.assertEquals("www.google.com:80", remote_addr);
                    Assert.assertEquals(".", log_dir);
                    Assert.assertEquals(8080, listen_on);
                    Assert.assertEquals(0, filter_configs.length);
                } else if (name.equals("AC")) {
                    Assert.assertEquals("C:9004", remote_addr);
                    Assert.assertEquals(".", log_dir);
                    Assert.assertEquals(9005, listen_on);
                    Assert.assertEquals(2, filter_configs.length);
                    Assert.assertEquals("Zero", filter_configs[0].getProperty("type"));
                    Assert.assertEquals("Wan", filter_configs[1].getProperty("type"));
                    Assert.assertEquals("zeroing switch", filter_configs[0].getProperty("description"));
                    Assert.assertEquals("transatlantic cable", filter_configs[1].getProperty("description"));
                    Assert.assertEquals("1", filter_configs[1].getProperty("median_latency"));
                } else {

                }
            }
        });
    }

}

