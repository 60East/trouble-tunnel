package com.crankuptheamps.ttunnel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConnectionProcessorPipe implements ConnectionProcessor, Runnable {

    private final InputStream instream;
    private final OutputStream outstream;
    private final ConnectionLogger logger;
    private final int bufferSize;

    public ConnectionProcessorPipe(final InputStream instream, final FilterFactory filterFactory, OutputStream outstream, final ConnectionLogger logger,  final Integer bufferSize) {
        this.instream =  new TroubleInputStream(instream, filterFactory.getInstances(this));
        this.outstream = outstream;
        this.bufferSize = bufferSize == null ? 1024 * 1024 : bufferSize;
        this.logger = logger;
    }

    @Override
    public void disconnect(){

    }

    public void pause() {
        pause_egress();
        pause_ingress();
    }

    private Boolean egress_paused = false;
    @Override
    public void pause_egress() {
        synchronized(egress_paused) {
            egress_paused = true;
        }
    }

    private boolean egress_paused() {
        synchronized(egress_paused) {
            return egress_paused;
        }
    }

    private Boolean ingress_paused = false;
    @Override
    public void pause_ingress() {
        synchronized (ingress_paused) {
            ingress_paused = true;
        }
    }
    private boolean ingress_paused() {
        synchronized (ingress_paused) {
            return ingress_paused;
        }
    }

    private Boolean logging_enabled = false;
    @Override
    public void start_logging() {
        synchronized(logging_enabled) {
            logging_enabled = true;
        }

    }

    @Override
    public void stop_logging() {
        synchronized (logging_enabled) {
            logging_enabled = false;
        }
    }

    private boolean logging_enabled() {
        synchronized (logging_enabled) {
            return logging_enabled;
        }
    }

    private long bytes_in, bytes_out, read_ms, write_ms, read_count,  write_count, began_at, ended_at, exception_at;
    @Override
    public Map<String, Long> getStatistics() {
        final Map<String, Long> stat = new HashMap<String, Long>(6);
        stat.put("bytes_in",   bytes_in);
        stat.put("bytes_out",  bytes_out);
        stat.put("read_ms",    read_ms);
        stat.put("write_ms",   write_ms);
        stat.put("read_count", read_count);
        stat.put("write_count", write_count);
        stat.put("began_at", began_at);
        stat.put("ended_at", ended_at);
        stat.put("exception_at", exception_at);

        return stat;
    }

    @Override
    public void run() {
        logger.entering("run", this);
        began_at = System.currentTimeMillis();
        final byte[] buffer = new byte[bufferSize];
        int cursor = 0;
        boolean input_ended = false;
        final Timer in_timer = new Timer(), out_timer = new Timer();
        try {
            while (exception == null && !input_ended) {
                final int space_remaining = buffer.length - cursor;
                    if (!ingress_paused() && !input_ended && space_remaining > 0) {
                        in_timer.begin();
                        try {
                            final int bytesRead = instream.read(buffer, cursor, space_remaining);
                            if (bytesRead == -1) {
                                input_ended = true;
                            } else {
                                cursor += bytesRead;
                                bytes_in += bytesRead;
                            }
                        } finally {
                            read_ms += in_timer.end();
                            ++read_count;
                        }
                    }
                    if (!egress_paused() && cursor > 0) {
                        out_timer.begin();
                        outstream.write(buffer, 0, cursor);
                        outstream.flush();
                        write_ms += out_timer.end();
                        bytes_out += cursor;
                        cursor = 0;
                        ++write_count;
                    }
            }
        } catch (IOException e) {
            onException(e);
        } finally {
            ended_at = System.currentTimeMillis();
        }
        logger.leaving("run", this);
    }


    private Exception exception;
    public Exception getException() {
        return exception;
    }
    private void onException(Exception e) {
        exception_at = System.currentTimeMillis();
        this.exception = e;
    }
}

class Timer {
    private long started;
    public void begin() {
        started = System.currentTimeMillis();
    }
    public long end() {
        if (started == 0) {
            throw new Error("trying to end but never started.");
        }
        final long duration = System.currentTimeMillis() - started;
        started = 0;
        return duration;
    }
}
