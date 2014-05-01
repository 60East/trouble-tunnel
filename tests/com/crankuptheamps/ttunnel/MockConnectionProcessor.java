package com.crankuptheamps.ttunnel;


import java.io.IOException;
import java.util.Map;
import java.util.Properties;


class MockConnectionProcessor implements ConnectionProcessor {

    public int disconnect_count, pause_count, pause_egress_count;
    public int pause_ingress_count, start_logging_count, stop_logging_count;

    public void disconnect() {
        ++disconnect_count;
    }

    public void pause() {
        ++pause_count;
    }

    public void pause_egress() {
        ++pause_egress_count;
    }

    public void pause_ingress() {
        ++pause_ingress_count;
    }

    public void start_logging() {
        ++start_logging_count;
    }

    public void stop_logging() {
        ++stop_logging_count;
    }

    @Override
    public IOException getException() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Map<String, Long> getStatistics() {
        throw new RuntimeException("not implemented");
    }


}