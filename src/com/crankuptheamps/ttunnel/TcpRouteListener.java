package com.crankuptheamps.ttunnel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class TcpRouteListener implements RouteListener {

    private final ServerSocket serverSocket;

    public TcpRouteListener(final EndpointSpec endpoint) throws IOException {
        serverSocket = new ServerSocket();

        if (endpoint.getHost() == null) {
            serverSocket.bind(new InetSocketAddress(endpoint.getPort()));
        } else {
            serverSocket.bind(new InetSocketAddress(endpoint.getHost(), endpoint.getPort()));
        }
    }

    public RouteConnection accept() throws IOException {
        return new TcpRouteConnection(serverSocket.accept());
    }

    public void close() throws IOException {
        serverSocket.close();
    }
}
