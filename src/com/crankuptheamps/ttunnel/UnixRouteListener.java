package com.crankuptheamps.ttunnel;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

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
                try {
                    BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
                    // If the file is an existing Unix domain socket file, delete it
                    if (attrs.isOther() && !Files.isSymbolicLink(path)) {
                        Files.delete(path);
                    } else {
                        throw new IOException("Cannot delete file since is not a Unix domain socket file.");
                    }
                } catch (Exception e) {
                    throw new IOException("Cannot delete file since it is not a Unix domain socket file.", e);
                }
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

