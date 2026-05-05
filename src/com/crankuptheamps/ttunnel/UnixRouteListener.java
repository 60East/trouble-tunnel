package com.crankuptheamps.ttunnel;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public class UnixRouteListener implements RouteListener {

    private final Path path;
    private final ServerSocketChannel channel;

    public UnixRouteListener(final EndpointSpec endpoint) throws IOException {
        path = endpoint.getPath();
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        if (Files.exists(path)) {
            if (endpoint.isUnlinkExisting()) {
                Files.delete(path);
            } else {
                throw new IOException("Unix domain socket path already exists: " + path);
            }
        }
        channel = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
        channel.bind(UnixDomainSocketAddress.of(path));
    }

    public RouteConnection accept() throws IOException {
        return new UnixRouteConnection(channel.accept());
    }

    public void close() throws IOException {
        try {
            channel.close();
        } finally {
            Files.deleteIfExists(path);
        }
    }
}
