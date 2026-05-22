package com.crankuptheamps.ttunnel;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class Route implements Runnable {

    private final EndpointSpec listenEndpoint;
    private final EndpointSpec remoteEndpoint;
    public final String name;
    private final Properties[] filterConfigs;
    private final File log_dir;
    private final ConnectionLoggerImpl logger;
    private final RouteTransportFactory transportFactory = new RouteTransportFactory();

    /**
     *
     * @param name
     * @param listenEndpoint
     * @param remoteEndpoint
     * @param filterConfigs
     * @throws IOException - if there's trouble initializing logging system
     */
    public Route(final String name, final EndpointSpec listenEndpoint,
                 final EndpointSpec remoteEndpoint, final File log_dir,
                 final Properties[] filterConfigs) throws IOException {
        this.name = name;
        this.listenEndpoint = listenEndpoint;
        this.remoteEndpoint = remoteEndpoint;
        this.filterConfigs = filterConfigs;
        this.log_dir = log_dir;
        logger = new ConnectionLoggerImpl(log_dir, name, 0);
        logger.enable();
    }

    private boolean stopped = false;
    private RouteListener listener;
    private final List<ConnectionProcessor> processors = new LinkedList<ConnectionProcessor>();

    public synchronized void stop() {
        debug("Route.stop() Route-" + hashCode());

        if (stopped) {
            warn("Route.stop() called but already stopped");
        }

        stopped = true;

        synchronized(processors) {
            for (ConnectionProcessor proc : processors) {
                proc.disconnect();
            }
        }

        if (listener != null) {
            try {
                listener.close();
                info("Route-" + hashCode() + " listener closed");
                listener = null;
                info("Route-" + hashCode() + " listener: " + listener);
            } catch (IOException e) {
                warn("in Route.stop(): " + e.getMessage());
            }
        }
    }

    public synchronized boolean stopped() {
        debug("Route.stopped() returning " + stopped);
        return stopped;
    }

    public void run() {
        info("Route-" + hashCode() + " entering run()");

        try {
            while (!stopped()) {
                try {
                    if (listener == null) {
                        info("Thread-" + Thread.currentThread().hashCode() + " Route-" + hashCode() + " constructing new listener on " + listenEndpoint);
                        listener = transportFactory.listener(listenEndpoint);
                    }

                    RouteConnection localConnection, remoteConnection;

                    try {
                        info("waiting for connection on " + listenEndpoint);
                        info("Thread-" + Thread.currentThread().hashCode() + " Route-" + hashCode() + " calling accept()");
                        localConnection = listener.accept();
                        info("connection received on " + listenEndpoint);
                        info("connecting to remote endpoint: " + remoteEndpoint);
                        remoteConnection = transportFactory.connector(remoteEndpoint).connect();
                        info("remote endpoint connected, constructing connection processor ...");
                        final ConnectionProcessorImpl proc = new ConnectionProcessorImpl(localConnection.input(),
                            localConnection.output(),
                            remoteConnection.input(),
                            remoteConnection.output(),
                            this.name,
                            this.log_dir,
                            filterConfigs);

                        synchronized(processors) {
                            processors.add(proc);
                        }

                        info("starting connection processor impl thread for " + proc.hashCode() +  " ...");
                        new Thread(proc).start();
                        info("connection processor impl thread started.");
                        //proc.run();
                    } catch (java.net.SocketException ignored) {
                        // happens if the listener is closed while we're blocking on accept()
                    }
                } catch (IOException e) {
                    // thrown by listener construction
                    warn("Route-" + hashCode() + " run() catch #2: " + e.getMessage());
                }
            }
        } finally {
            info("Route-" + hashCode() + " leaving run()");
        }
    }

    private void info(final String msg) {
        logger.info(msg);
    }

    private void debug(final String msg) {
        logger.debug(msg);
    }

    private void warn(final String msg) {
        logger.warn(msg);
    }

}
