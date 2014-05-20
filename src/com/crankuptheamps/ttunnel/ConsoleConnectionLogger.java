package com.crankuptheamps.ttunnel;

import java.util.logging.*;

public class ConsoleConnectionLogger implements ConnectionLogger {

	private boolean disabled = false;
	private final String route_name;
	private final int connection_id;
	final Logger logger = Logger.getLogger(getClass().getName().substring(getClass().getName().lastIndexOf(".") + 1));


	public ConsoleConnectionLogger(final String route_name, final Integer connection_id) {
		this.route_name = route_name;
		this.connection_id = connection_id;
		final ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(new SimpleFormatter());
		logger.addHandler(handler);
		logger.setUseParentHandlers(false);
	}

	public void enable() {
		disabled = false;
	}

	public void disable() {
		disabled = true;
	}

	public void debug(final String msg) {
		if(disabled) return;
		logger.fine("" + route_name + "-" + connection_id + "[debug]: " + msg);
	}

	public void info(final String msg) {
		if(disabled) return;
		else logger.info("" + route_name + "-" + connection_id + "[info]: " + msg);
	}

	public void warn(final String msg) {
		if(disabled) return;
		else logger.warning("" + route_name + "-" + connection_id + "[warn]: " + msg);
	}

	
	public void entering(String name, Object o, String... args)
	{
		info(StringUtils.shortName(o.getClass()) + "-" + o.hashCode() + " entering " + name + "(" + StringUtils.join(args, ", ") + ")");
	}

	
	public void leaving(String name, Object o)
	{
		info(StringUtils.shortName(o.getClass()) + "-" + o.hashCode() +  " leaving " + name + "()");
	}

}
