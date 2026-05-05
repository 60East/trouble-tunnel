package com.crankuptheamps.ttunnel;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Properties;

public class ConfigParserTest {

    @Test
    public void testParse() throws Exception {
        ConfigParser.parse(new File("tests/sample-config.json"), new ConfigHandler() {

            public void config(String name, EndpointSpec listen, EndpointSpec remote, String log_dir, Properties[] filter_configs) {
                if (name.equals("AB")) {
                    Assert.assertEquals(EndpointSpec.Type.TCP, remote.getType());
                    Assert.assertEquals("www.google.com", remote.getHost());
                    Assert.assertEquals(80, remote.getPort());
                    Assert.assertEquals(".", log_dir);
                    Assert.assertEquals(EndpointSpec.Type.TCP, listen.getType());
                    Assert.assertEquals(8080, listen.getPort());
                    Assert.assertEquals(0, filter_configs.length);
                } else if (name.equals("AC")) {
                    Assert.assertEquals(EndpointSpec.Type.TCP, remote.getType());
                    Assert.assertEquals("C", remote.getHost());
                    Assert.assertEquals(9004, remote.getPort());
                    Assert.assertEquals(".", log_dir);
                    Assert.assertEquals(EndpointSpec.Type.TCP, listen.getType());
                    Assert.assertEquals(9005, listen.getPort());
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

    @Test
    public void testParseTCPToTCP() throws Exception {
        ConfigParser.parse(new File("tests/sample-configs-1.1.0/tcp-to-tcp.json"), new ConfigHandler() {
            public void config(String name, EndpointSpec listen, EndpointSpec remote, String log_dir,
                    Properties[] filter_configs) {
                Assert.assertEquals(EndpointSpec.Type.TCP, listen.getType());
                Assert.assertEquals(9004, listen.getPort());
                Assert.assertEquals(EndpointSpec.Type.TCP, remote.getType());
                Assert.assertEquals(9004, remote.getPort());
                Assert.assertEquals("B", remote.getHost());
                Assert.assertEquals(".", log_dir);
            }
        });
    }

    @Test
    public void testParseTPCToUDS() throws Exception {
        ConfigParser.parse(new File("tests/sample-configs-1.1.0/tcp-to-uds.json"), new ConfigHandler() {
            public void config(String name, EndpointSpec listen, EndpointSpec remote, String log_dir,
                    Properties[] filter_configs) {
                Assert.assertEquals(EndpointSpec.Type.TCP, listen.getType());
                Assert.assertEquals(9004, listen.getPort());
                Assert.assertEquals(EndpointSpec.Type.UNIX, remote.getType());
                Assert.assertEquals("./uds-remote.sock", remote.getPath().toString());
                Assert.assertTrue(remote.isUnlinkExisting());
                Assert.assertEquals(".", log_dir);
            }
        });
    }
    
    @Test
    public void testParseUDSToTCP() throws Exception {
        ConfigParser.parse(new File("tests/sample-configs-1.1.0/uds-to-tcp.json"), new ConfigHandler() {
            public void config(String name, EndpointSpec listen, EndpointSpec remote, String log_dir,
                    Properties[] filter_configs) {
                Assert.assertEquals(EndpointSpec.Type.UNIX, listen.getType());
                Assert.assertEquals("./uds-listen.sock", listen.getPath().toString());
                Assert.assertTrue(listen.isUnlinkExisting());
                Assert.assertEquals(EndpointSpec.Type.TCP, remote.getType());
                Assert.assertEquals(9004, remote.getPort());
                Assert.assertEquals("B", remote.getHost());
                Assert.assertEquals(".", log_dir);
            }
        });
    }
    
    @Test
    public void testParseUDSToUDS() throws Exception {
        ConfigParser.parse(new File("tests/sample-configs-1.1.0/uds-to-uds.json"), new ConfigHandler() {
            public void config(String name, EndpointSpec listen, EndpointSpec remote, String log_dir,
                    Properties[] filter_configs) {
                Assert.assertEquals(EndpointSpec.Type.UNIX, listen.getType());
                Assert.assertEquals("./uds-listen.sock", listen.getPath().toString());
                Assert.assertTrue(listen.isUnlinkExisting());
                Assert.assertEquals(EndpointSpec.Type.UNIX, remote.getType());
                Assert.assertEquals("./uds-remote.sock", remote.getPath().toString());
                Assert.assertTrue(remote.isUnlinkExisting());
                Assert.assertEquals(".", log_dir);
            }
        });
    }
    
    @Test
    public void testParseUDSToUDSFalseAndMissingUnlinkExisting() throws Exception {
        ConfigParser.parse(new File("tests/sample-configs-1.1.0/uds-to-uds-unlink-existing.json"), new ConfigHandler() {
            public void config(String name, EndpointSpec listen, EndpointSpec remote, String log_dir,
                    Properties[] filter_configs) {
                Assert.assertEquals(EndpointSpec.Type.UNIX, listen.getType());
                Assert.assertEquals("./uds-listen.sock", listen.getPath().toString());
                Assert.assertFalse(listen.isUnlinkExisting());
                Assert.assertEquals(EndpointSpec.Type.UNIX, remote.getType());
                Assert.assertEquals("./uds-remote.sock", remote.getPath().toString());
                Assert.assertFalse(remote.isUnlinkExisting());
                Assert.assertEquals(".", log_dir);
            }
        });
    }

    @Test
    public void testParseLogDirAndFilter() throws Exception {
        ConfigParser.parse(new File("tests/sample-configs-1.1.0/uds-and-filter.json"), new ConfigHandler() {
            public void config(String name, EndpointSpec listen, EndpointSpec remote, String log_dir,
                    Properties[] filter_configs) {
                Assert.assertEquals(EndpointSpec.Type.UNIX, listen.getType());
                Assert.assertEquals("./uds-listen.sock", listen.getPath().toString());
                Assert.assertTrue(listen.isUnlinkExisting());
                Assert.assertEquals(EndpointSpec.Type.UNIX, remote.getType());
                Assert.assertEquals("./uds-remote.sock", remote.getPath().toString());
                Assert.assertTrue(remote.isUnlinkExisting());
                Assert.assertEquals(".", log_dir);
                Assert.assertEquals(1, filter_configs.length);
                Assert.assertEquals("Wan", filter_configs[0].getProperty("type"));
                Assert.assertEquals("WAN UDS", filter_configs[0].getProperty("description"));
                Assert.assertEquals("1000", filter_configs[0].getProperty("median_latency"));
            }
        });
    }
}
