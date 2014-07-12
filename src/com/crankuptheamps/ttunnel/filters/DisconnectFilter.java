package com.crankuptheamps.ttunnel.filters;

import com.crankuptheamps.ttunnel.ConnectionLogger;
import com.crankuptheamps.ttunnel.ConnectionProcessor;
import com.crankuptheamps.ttunnel.ConsoleConnectionLogger;

import java.util.*;

public class DisconnectFilter extends Filter {

 private final String min_uptime_key = "min_uptime";
 private final String max_uptime_key = "max_uptime";
 private final ConnectionLogger logger;

 public DisconnectFilter(final ConnectionProcessor proc, final Properties props)
 {
	super(proc, props);
	requireConfigKey(min_uptime_key);
	requireConfigKey(max_uptime_key);
	final long min_uptime = Long.parseLong(props.getProperty(min_uptime_key));
	final long max_uptime = Long.parseLong(props.getProperty(max_uptime_key));
	if (min_uptime > max_uptime)
	{
	 throw new RuntimeException("min_uptime(" + min_uptime + ")" +
		"must be less than max_uptime(" + max_uptime + ")");
	}
	final Random random = new Random();
	final long uptime = max_uptime > 0 ? min_uptime + random.nextInt((int) (max_uptime - min_uptime)) : 0;
	final String clname = getClass().getName().substring(getClass().getName().lastIndexOf('.') + 1);
	logger = new ConsoleConnectionLogger(clname, hashCode());
	debug("<init>() uptime=" + uptime + "ms");
	new Thread(new Runnable() {
	 public void run()
	 {
		try
		{
		 String pname = getConnectionProcessor().getClass().getName().substring(getConnectionProcessor().getClass().getName().lastIndexOf(".") + 1);
		 pname = pname + "-" + getConnectionProcessor().hashCode();
		 debug("sleeping " + uptime + "ms on " + pname);
		 Thread.sleep(uptime);
		 debug("disconnecting processor + " + pname + " after " + uptime + "ms");
		 getConnectionProcessor().disconnect();
		}
		catch (InterruptedException e)
		{
		}
	 }
	}).start();
 }


 public int filter(int datum)
 {
//	debug("filter(" + datum + ")");
	return datum;
 }

 public int filter(byte[] b, int off, int len)
 {
//	debug("filter(" + new String(b, off, len) + ")");
	return len;
 }

 private void debug(final String msg)
 {
	final String clname = getClass().getName().substring(getClass().getName().lastIndexOf('.') + 1);
    getConnectionProcessor().get_logger().debug(clname + "-" + hashCode() + msg);
 }

}
