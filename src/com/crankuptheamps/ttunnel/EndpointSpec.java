package com.crankuptheamps.ttunnel;

import java.nio.file.Path;

public class EndpointSpec {

    public enum Type {
        TCP,
        UNIX
    }

    private final Type type;
    private final String host;
    private final int port;
    private final Path path;
    private final boolean unlinkExisting;

    private EndpointSpec(final Type type, final String host, final int port, final Path path, final boolean unlinkExisting) {
        this.type = type;
        this.host = host;
        this.port = port;
        this.path = path;
        this.unlinkExisting = unlinkExisting;
    }

    public static EndpointSpec tcp(final String host, final int port) {
        return new EndpointSpec(Type.TCP, host, port, null, false);
    }

    public static EndpointSpec unix(final Path path, final boolean unlinkExisting) {
        return new EndpointSpec(Type.UNIX, null, -1, path, unlinkExisting);
    }

    public Type getType() {
        return type;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Path getPath() {
        return path;
    }

    public boolean isUnlinkExisting() {
        return unlinkExisting;
    }

    public String toString() {
        if (type == Type.TCP) {
            return host == null ? "tcp:" + port : "tcp:" + host + ":" + port;
        }
        return "unix:" + path;
    }
}
