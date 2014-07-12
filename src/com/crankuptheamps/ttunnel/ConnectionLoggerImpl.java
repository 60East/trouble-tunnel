package com.crankuptheamps.ttunnel;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ConnectionLoggerImpl implements ConnectionLogger {

    private boolean disabled = false;
    private final String route_name;
    private final int connection_id;
    final Logger logger = Logger.getLogger(ConnectionLoggerImpl.class.getName());

    ConnectionLoggerImpl(final File log_dir, final String route_name, final Integer connection_id) {
        // System.out.debug("ConnectionLoggerImpl.<init>(" + log_dir + ", " + route_name + ", " + connection_id + ")");

		 if (!log_dir.exists())
		 {
			log_dir.mkdirs();
		 }
		 if (!log_dir.exists())
		 {
			throw new Error("log dir does not exist and could not be created: " + log_dir.getAbsolutePath());
		 }

        this.route_name = route_name;
        this.connection_id = connection_id;

        String log_filename = route_name + "-cxn" + connection_id + ".log";
        if (connection_id == null) {
            log_filename = route_name + "-route.log";
        }
        final File logfile = new File(log_dir, log_filename);
        final int log_size_limit_bytes = 10 * 1024 * 1024; // 10MB
        final int rollover_count_limit = 5;
        final boolean append = true;
        try {
            final FileHandler handler = new FileHandler(logfile.getAbsolutePath(),
                                                        log_size_limit_bytes,
                                                        rollover_count_limit,
                                                        append);
            handler.setFormatter(new SimpleFormatter());
            logger.addHandler(handler);
            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            throw new Error("Failed to initialize logger handler for route: " + route_name + ":" + e.getMessage());
        }

    }

    public void enable() {
        disabled = false;
    }

    public void disable() {
        disabled = true;
    }

    public void debug(final String msg) {
        if(disabled) return;
        else logger.log(Level.FINE, msg);
        // System.out.debug(route_name + "-" + connection_id + "[debug]: " + msg);
    }

    public void info(final String msg) {
        if(disabled) return;
        else logger.info(msg);
        // System.out.debug(route_name + "-" + connection_id + "[info]: " + msg);
    }

    public void warn(final String msg) {
        if(disabled) return;
        else logger.warning(msg);
        // System.out.debug(route_name + "-" + connection_id + "[warn]: " + msg);
    }

    public void entering(final String name, final Object o, String ... args) {
        info(StringUtils.shortName(o.getClass()) + "-" + o.hashCode() + " entering " + name + "(" + StringUtils.join(args, ", ") + ")");
    }

    public void leaving(final String name, final Object o) {
        info(StringUtils.shortName(o.getClass()) + "-" + o.hashCode() +  " leaving " + name + "()");
    }


}
