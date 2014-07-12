package com.crankuptheamps.ttunnel;


import java.io.IOException;
import java.util.Map;
import java.util.Properties;


public class MockConnectionProcessor implements ConnectionProcessor {

    public int disconnect_count, pause_count, pause_egress_count, resume_count, resume_egress_count;
    public int resume_ingress_count, pause_ingress_count, start_logging_count, stop_logging_count;

    public void disconnect() {
        ++disconnect_count;
    }

    public void pause() {
        ++pause_count;
    }

    public void resume() {
        ++resume_count;
    }

    public void resume_egress() {
        ++resume_egress_count;
    }

    public void pause_egress() {
        ++pause_egress_count;
    }

    public void pause_ingress() {
        ++pause_ingress_count;
    }

    public void resume_ingress() {
        ++resume_ingress_count;
    }

    public void start_logging() {
        ++start_logging_count;
    }

    public void stop_logging() {
        ++stop_logging_count;
    }


    public IOException getException() {
        throw new RuntimeException("not implemented");
    }


    public Map<String, Long> getStatistics() {
        throw new RuntimeException("not implemented");
    }

    public ConnectionLogger get_logger() {
        return new ConsoleConnectionLogger("bogus_route", 0);
        //throw new RuntimeException("not implemented");
    }

}