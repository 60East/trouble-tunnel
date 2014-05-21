package com.crankuptheamps.ttunnel;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class Route implements Runnable {

    private final int local_port;
    private final int remote_port;
    public final String name;
    private final String remote_hostname;
    private  FilterFactory filterFactory;
    private final Properties[] filterConfigs;
    private final File log_dir;
    private final ConnectionLoggerImpl logger;


    /**
     *
     * @param name
     * @param local_port
     * @param remote_address
     * @param filterConfigs
     * @throws IOException - if there's trouble initializing logging system
     */
    public Route(final String name, final int local_port,
                 final String remote_address,final File log_dir,
                 final Properties[] filterConfigs) throws IOException {

        this.name = name;
        if (remote_address.indexOf(":") == -1) {
            throw new Error("port missing in remote address, expected <hostname>:<port>: " + remote_address);
        }
        this.remote_hostname= remote_address.substring(0, remote_address.indexOf(":"));
        this.remote_port= Integer.parseInt(remote_address.substring(remote_address.indexOf(":")+1, remote_address.length()));
        this.local_port = local_port;
        this.filterConfigs = filterConfigs;
        this.log_dir = log_dir;
        logger = new ConnectionLoggerImpl(log_dir, name, 0);
        logger.enable();
    }

    private boolean stopped = false;
    private ServerSocket serverSocket;
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

	if (serverSocket != null) {
	    try
		{
		    serverSocket.close();
		    info("Route-" + hashCode() + " serverSocket closed");
		    serverSocket = null;
		    info("Route-" + hashCode() + " serverSocket: " + serverSocket);
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
		    if (serverSocket == null) {
			info("Thread-" + Thread.currentThread().hashCode() + " Route-" + hashCode() + " constructing new ServerSocket on port " + local_port);
			serverSocket = new ServerSocket(local_port);
		    }
                    Socket localSocket, remoteSocket;
                    try {
                        info("waiting for connection to serversocket on port " + local_port);
			info("Thread-" + Thread.currentThread().hashCode() + " Route-" + hashCode() + " calling accept()");
                        localSocket = serverSocket.accept();
                        info("connection received on port " + local_port);
                        info("connecting to remote socket: host: " + remote_hostname + ", port: " + remote_port);
                        remoteSocket = new Socket(remote_hostname, remote_port);
                        info("remote socket connected, constructing connection processor ...");
                        final ConnectionProcessorImpl proc = new ConnectionProcessorImpl(localSocket.getInputStream(),
											 localSocket.getOutputStream(),
											 remoteSocket.getInputStream(),
											 remoteSocket.getOutputStream(),
											 this.name,
											 this.log_dir,
											 filterConfigs);
			synchronized(processors) { processors.add(proc); }
                        info("starting connection processor impl thread for " + proc.hashCode() +  " ...");
                        new Thread(proc).start();
                        info("connection processor impl thread started.");
			//proc.run();
                    } catch (java.net.SocketException ignored) {
			// happens if the serverSocket is closed while we're blocking on accept()
                    }
                } catch (IOException e) {
		    // thrown by ServerSocket construction
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

