package com.crankuptheamps.ttunnel;

import junit.framework.Assert;
import org.junit.Test;

import java.io.File;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;

public class ExampleTest extends TroubleTest {

 public ExampleTest()
 {
	super(new File(new File("tests"), "work"));
 }

 @Test
 public void test_local_port() throws Exception
 {
	final Socket s = new Socket("localhost", getInt("b_local_port"));
	s.getInputStream();
	s.getOutputStream();
	Assert.assertEquals("expected latency on this connection in ms", getLong("b_latency"), 1000);
	Assert.assertTrue(s.isConnected());
	s.close();
 }

 @Test
 public void test_get_property()
 {
	Assert.assertEquals("www.google.com", getProperty("b_remote_host"));
 }

 @Test
 public void test_get_long()
 {
	Assert.assertEquals(8989l, getLong("b_local_port"));
 }

 @Test(expected = java.lang.RuntimeException.class)
 public void test_get_long_missing()
 {
	Assert.assertEquals(8989l, getLong("bogus_key!"));
 }

 @Test
 public void test_get_int()
 {
	Assert.assertEquals(8989, getInt("b_local_port"));
 }

 @Test(expected = java.lang.RuntimeException.class)
 public void test_get_int_missing()
 {
	Assert.assertEquals(8989, getInt("bogus_key!"));
 }

 @Test
 public void test_get_file()
 {
	Assert.assertTrue(getFile("b_remote_host").getAbsolutePath().startsWith(working_directory.getAbsolutePath()));
 }

 @Test
 public void test_get_file_missing()
 {
	Assert.assertNull(getFile("bogus_key!"));
 }

 @Test
 public void test_config_keys()
 {
	HashSet<String> set = new HashSet<>();
    for (Iterator i = configKeys(); i.hasNext(); )
	{
        set.add(i.next().toString());
	}
    // Check the placeholders in ExampleTest-template.json
	Assert.assertTrue(set.contains("b_remote_host"));
	Assert.assertTrue(set.contains("c_latency"));
	Assert.assertTrue(set.contains("b_latency"));
	Assert.assertTrue(set.contains("b_local_port"));
	Assert.assertTrue(set.contains("b_remote_port"));
    Assert.assertEquals(5, set.size());
 }

}
