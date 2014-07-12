package com.crankuptheamps.ttunnel;


import com.crankuptheamps.ttunnel.filters.Filter;
import com.crankuptheamps.ttunnel.filters.WanFilter;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;


public class Main {

 public static void main(String[] args) throws Exception
 {
	new Main(new File("."), args).start();
 }

 private final String[] args;
 private final List<Route> routes = new LinkedList<Route>();
 private final File working_directory;

 public Main(final File working_directory, final String[] args)
 {
	this.working_directory = working_directory;
	this.args = args;
 }

 public void start() throws Exception
 {
	final String usage = "trouble-tunnel  <config file> [ admin port ]";
	if (args.length == 0)
	{
	 error("no cmdline args found");
	 throw new Error(usage);
	}
	ConfigParser.parse(new File(args[0]), new MainConfigHandler(working_directory, routes));
 }

 public void stop()
 {
	synchronized (routes)
	{
	 for (Route route : routes)
	 {
		route.stop();
	 }
	}
 }

 private static void log(final String lvl, final String msg)
 {
	System.out.println("[Main] " + lvl + ": " + msg);
 }

 private static void error(final String msg)
 {
	log("ERROR", msg);
 }

}

class MainConfigHandler implements ConfigHandler {

 private final List<Route> routes;
 private final File working_directory;

 public MainConfigHandler(final File working_directory, final List<Route> routes)
 {
	this.working_directory = working_directory;
	this.routes = routes;
 }

 public void config(String name, String remote_addr, int listen_on, String log_dir, Properties[] filter_configs)
 {
	try
	{
	 final Route route = new Route(name, listen_on, remote_addr, new File(working_directory, log_dir), filter_configs);
	 synchronized (routes)
	 {
		routes.add(route);
	 }
	 new Thread(route, name).start();
	 info(name + " route started.");
	}
	catch (IOException e)
	{
	 error("route creation failed for " + name + e.getMessage());
	 System.exit(1);
	}
 }

 private void log(final String lvl, final String msg)
 {
	System.out.println("[Main] " + lvl + ": " + msg);
 }

 private void info(final String msg)
 {
	log("INFO", msg);
 }

 private void error(final String msg)
 {
	log("ERROR", msg);
 }

}
