package com.crankuptheamps.ttunnel;

import java.io.IOException;

public class RouteTransportFactory {

    public RouteListener listener(final EndpointSpec endpoint) throws IOException {
        if (endpoint.getType() == EndpointSpec.Type.UNIX) {
            return new UnixRouteListener(endpoint);
        }

        return new TcpRouteListener(endpoint);
    }

    public RouteConnector connector(final EndpointSpec endpoint) {
        if (endpoint.getType() == EndpointSpec.Type.UNIX) {
            return new UnixRouteConnector(endpoint);
        }

        return new TcpRouteConnector(endpoint);
    }
}
