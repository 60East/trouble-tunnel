package com.crankuptheamps.ttunnel;

import junit.framework.Assert;
import org.junit.Test;

import java.io.File;
import java.net.Socket;
import java.util.Iterator;

public class ExampleTest extends TroubleTest {

 public ExampleTest()
 {
	super(new File("./work-dir"));
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
	final StringBuffer s = new StringBuffer();
	for (Iterator i = configKeys(); i.hasNext(); )
	{
	 s.append("<" + i.next().toString() + ">");
	}
	Assert.assertEquals(s.toString(), "<b_remote_host><c_latency><b_latency><b_local_port><b_remote_port>");
 }

}
