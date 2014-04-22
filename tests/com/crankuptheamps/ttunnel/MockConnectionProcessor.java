package com.crankuptheamps.ttunnel;


import java.io.IOException;
import java.util.Map;
import java.util.Properties;


class MockConnectionProcessor implements ConnectionProcessor {

    public void disconnect() throws IOException {
        throw new RuntimeException("not implemented");
    }

    public void pause() {
        throw new RuntimeException("not implemented");
    }

    public void pause_egress() {
        throw new RuntimeException("not implemented");
    }

    public void pause_ingress() {
        throw new RuntimeException("not implemented");
    }

    public void start_logging() {
        throw new RuntimeException("not implemented");
    }

    public void stop_logging() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public IOException getException() {
        throw new RuntimeException("not implemented");    }

    @Override
    public Map<String, Long> getStatistics() {
        throw new RuntimeException("not implemented");    }


}