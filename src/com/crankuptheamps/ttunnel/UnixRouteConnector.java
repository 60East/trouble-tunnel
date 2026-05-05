package com.crankuptheamps.ttunnel;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.SocketChannel;

public class UnixRouteConnector implements RouteConnector {

    private final EndpointSpec endpoint;

    public UnixRouteConnector(final EndpointSpec endpoint) {
        this.endpoint = endpoint;
    }

    public RouteConnection connect() throws IOException {
        final SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX);
        channel.connect(UnixDomainSocketAddress.of(endpoint.getPath()));
        return new UnixRouteConnection(channel);
    }
}
