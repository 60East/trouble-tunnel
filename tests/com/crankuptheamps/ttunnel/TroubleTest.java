package com.crankuptheamps.ttunnel;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Iterator;
import java.util.Properties;
import java.util.Scanner;

public abstract class TroubleTest {

 protected final File working_directory;
 private final File template_file;
 private final File config_file;
 private final File props_file;
 private final Properties props = new Properties();
 private Main tunnel_main;

 public TroubleTest(final File workingDirectory)
 {
	final String name = getClass().getName().substring(getClass().getName().lastIndexOf(".") + 1);

	this.working_directory = workingDirectory;
	if (!working_directory.isDirectory())
	{
	 Assert.fail("working directory not found: " + working_directory.getAbsolutePath());
	}

	this.template_file = new File(working_directory, name + "-template.json");
	if (!template_file.exists())
	{
	 Assert.fail("template file not found: " + template_file.getAbsolutePath());
	} else if (template_file.length() == 0) {
	 Assert.fail("template file exists but is empty: " + template_file.getAbsolutePath());
	}

	this.config_file = new File(workingDirectory, name + ".json");

	this.props_file = new File(workingDirectory, name + ".properties");
	if (!props_file.exists())
	{
	 Assert.fail("config file not found: " + props_file.getAbsolutePath());
	}

 }

 protected String getProperty(final String key) {
	return props.getProperty(key);
 }

 protected long getLong(final String key) {
	if (getProperty(key) == null) throw new RuntimeException("long key not found in configuration: " + key);
	return Long.parseLong(getProperty(key));
 }

 protected int getInt(final String key) {
	if (getProperty(key) == null) throw new RuntimeException("int key not found in configuration: " + key);
	return Integer.parseInt(getProperty(key));
 }

 protected File getFile(final String key) {
	return getProperty(key) == null ? null : new File(working_directory, getProperty(key));
 }

 protected Iterator configKeys() {
	return props.keySet().iterator();
 }

 @Before
 public void start_tunnel() throws Exception
 {

	props.load(new FileReader(props_file));

	try
	{
	 if (tunnel_main != null) return;
	 write_tunnel_config();
	 tunnel_main = new Main(working_directory,  new String[]{config_file.getAbsolutePath()});
	 tunnel_main.start();
	 Thread.sleep(2000);
	} catch (org.json.simple.parser.ParseException t) {
	 System.err.println(t.getClass().getName());
	 t.printStackTrace(System.err);
	 System.err.flush();
	 Assert.fail("Failed to start TroubleTunnel with " + "config file: '" + config_file.getAbsolutePath() + "', which was generated from template: '" + template_file.getAbsolutePath() + "");
	}
 }


 @After
 public void stop_tunnel() throws Exception
 {
	if (tunnel_main == null) return;
	tunnel_main.stop();
	Thread.sleep(2000);
	tunnel_main = null;
 }


 private void write_tunnel_config() throws Exception
 {
	System.out.println("write_tunnel_config");


	String config = read_file(template_file);
	for (Iterator keys = props.keySet().iterator(); keys.hasNext(); )
	{
	 final String key = (String) keys.next();
	 final String val = props.getProperty(key);
	 config = replace_all(config, "${" + key + "}", val);
	}
	write_file(config_file, config);
 }


 private String read_file(final File file) throws Exception
 {
	BufferedReader r = null;
	final StringBuffer sb = new StringBuffer();
	try
	{
	 r = new BufferedReader(new FileReader(file));
	 String line = null;
	 while ((line = r.readLine()) != null) sb.append(line).append("\n");
	}
	finally
	{
	 if (r != null) r.close();
	}
	return sb.toString();
 }


 private void write_file(final File file, final String content) throws IOException
 {
	if (file.exists()) file.delete();
	BufferedWriter w = null;
	try
	{
	 w = new BufferedWriter(new FileWriter(file));
	 w.write(content);
	}
	finally
	{
	 try
	 {
		if (w != null) w.flush();
	 }
	 finally
	 {
		if (w != null) w.close();
	 }
	}
 }


 private String replace_all(String s, final String from, final String to)
 {
	int index = 0;
	while ((index = s.indexOf(from)) != -1)
	{
	 s = s.substring(0, index) + to + s.substring(index + from.length());
	}
	return s;
 }

}