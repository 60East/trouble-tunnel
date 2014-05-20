package com.crankuptheamps.ttunnel;

import com.crankuptheamps.ttunnel.filters.Filter;

import java.io.*;
import java.util.Map;
import java.util.Properties;

public class ConnectionProcessorImpl implements Runnable, ConnectionProcessor {

    private final int id;
    private final ConnectionProcessorPipe remote_to_local_pipe;
    private final ConnectionProcessorPipe local_to_remote_pipe;
    final ConnectionProcessorPipe[] pipes;
    final Thread[] threads;
		final ConnectionLogger logger;

    public ConnectionProcessorImpl(final InputStream localIn,
                                   final OutputStream localOut,
                                   final InputStream remoteIn,
                                   final OutputStream remoteOut,
                                   final String route_name,
                                   final File log_dir,
                                   final Properties[] filterConfigs) throws IOException {
        this.id = nextId();
        final FilterFactory filterFactory = new FilterFactory(filterConfigs);
		    logger = route_name != null && log_dir != null ? new ConnectionLoggerImpl(log_dir, route_name, this.id) : new ConsoleConnectionLogger(route_name, this.id);
        this.remote_to_local_pipe = new ConnectionProcessorPipe(localIn,  filterFactory.getInstances(this), remoteOut, logger, null); // null buffer sisze => default
        this.local_to_remote_pipe = new ConnectionProcessorPipe(remoteIn, filterFactory.getInstances(this), localOut,  logger, null); // null buffer sisze => default
        pipes = new ConnectionProcessorPipe[] {
                remote_to_local_pipe,
                local_to_remote_pipe,
        };
        threads = new Thread[pipes.length];
        start_logging();
    }

    private static int next_id = 0;

    private synchronized int nextId() {
        return ++next_id;
    }

    public void run() {
        for (int i = 0 ; i < pipes.length ; ++i) {
            threads[i] = new Thread(pipes[i]);
            threads[i].start();
        }
        join_pipes();
    }

    public void join_pipes() {
        try {
            for (Thread t : threads) {
                t.join();
            }
        } catch (InterruptedException e) {
            setException(e);
        }
    }

    public void disconnect() {
        for (ConnectionProcessor pipe : pipes) {
            pipe.disconnect();
        }
    }

    public void pause() {
        for (ConnectionProcessor pipe : pipes) {
            pipe.pause();
        }
    }

    public void pause_egress() {
        for (ConnectionProcessor pipe : pipes) {
            pipe.pause_egress();
        }
    }

    public void pause_ingress() {
        for (ConnectionProcessor pipe : pipes) {
            pipe.pause_ingress();
        }
    }

    public void start_logging() {
        for (ConnectionProcessor pipe : pipes) {
            pipe.start_logging();
        }
    }

    public void stop_logging() {
        for (ConnectionProcessor pipe : pipes) {
            pipe.stop_logging();
        }
    }

	public ConnectionLogger get_logger()
	{
		return logger;
	}

	public Map<String, Long> getStatistics() {
        throw new RuntimeException("not implemented");
    }


    private Exception exception;
    private synchronized void setException(final Exception e) {
        exception = e;
    }

    public synchronized Exception getException() {
        Exception e = exception;
        for (ConnectionProcessor pipe : pipes) {
            if (e == null) {
                e = pipe.getException();
            }
            if (e != null) return e;
        }
        return e;
    }

}
