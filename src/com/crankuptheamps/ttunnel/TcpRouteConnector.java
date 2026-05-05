package com.crankuptheamps.ttunnel;

import java.io.IOException;
import java.net.Socket;

public class TcpRouteConnector implements RouteConnector {

    private final EndpointSpec endpoint;

    public TcpRouteConnector(final EndpointSpec endpoint) {
        this.endpoint = endpoint;
    }

    public RouteConnection connect() throws IOException {
        return new TcpRouteConnection(new Socket(endpoint.getHost(), endpoint.getPort()));
    }
}
