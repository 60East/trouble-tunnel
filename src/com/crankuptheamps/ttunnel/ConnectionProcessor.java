package com.crankuptheamps.ttunnel;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public interface ConnectionProcessor {

    public void disconnect();

    public void pause();

    public void pause_egress();

    public void pause_ingress();

    public void start_logging();

    public void stop_logging();

    public Exception  getException();

    public Map<String, Long> getStatistics();

}
