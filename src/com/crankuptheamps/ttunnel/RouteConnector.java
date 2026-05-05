package com.crankuptheamps.ttunnel;

import java.io.IOException;

public interface RouteConnector {
    RouteConnection connect() throws IOException;
}
