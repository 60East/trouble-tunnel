package com.crankuptheamps.ttunnel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TcpRouteConnection implements RouteConnection {

    private final Socket socket;

    public TcpRouteConnection(final Socket socket) {
        this.socket = socket;
    }

    public InputStream input() throws IOException {
        return socket.getInputStream();
    }

    public OutputStream output() throws IOException {
        return socket.getOutputStream();
    }

    public void close() throws IOException {
        socket.close();
    }
}
