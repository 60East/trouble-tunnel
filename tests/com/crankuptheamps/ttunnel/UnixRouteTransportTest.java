package com.crankuptheamps.ttunnel;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class UnixRouteTransportTest {

    private Path testDirectory;

    @After
    public void cleanUp() throws Exception {
        if (testDirectory != null) {
            deleteRecursively(testDirectory);
        }
    }

    @Test
    public void createsParentDirectoriesForSocketPath() throws Exception {
        assumeUnixDomainSocketsSupported();
        final Path socketPath = socketPath("nested/listener.sock");
        UnixRouteListener listener = null;
        try {
            listener = new UnixRouteListener(EndpointSpec.unix(socketPath, false));
            Assert.assertTrue(Files.isDirectory(socketPath.getParent()));
            Assert.assertTrue(Files.exists(socketPath));
        } finally {
            close(listener);
        }
    }

    @Test(expected = IOException.class)
    public void failsWhenSocketPathExistsAndUnlinkExistingIsFalse() throws Exception {
        assumeUnixDomainSocketsSupported();
        final Path socketPath = socketPath("existing.sock");
        Files.createFile(socketPath);

        new UnixRouteListener(EndpointSpec.unix(socketPath, false));
    }

    @Test
    public void replacesExistingSocketPathWhenUnlinkExistingIsTrue() throws Exception {
        assumeUnixDomainSocketsSupported();
        final Path socketPath = socketPath("replace.sock");
        Files.createFile(socketPath);

        UnixRouteListener listener = null;
        try {
            listener = new UnixRouteListener(EndpointSpec.unix(socketPath, true));
            Assert.assertTrue(Files.exists(socketPath));
        } finally {
            close(listener);
        }
    }

    @Test
    public void deletesSocketPathOnClose() throws Exception {
        assumeUnixDomainSocketsSupported();
        final Path socketPath = socketPath("close.sock");
        final UnixRouteListener listener = new UnixRouteListener(EndpointSpec.unix(socketPath, false));

        Assert.assertTrue(Files.exists(socketPath));
        listener.close();
        Assert.assertFalse(Files.exists(socketPath));
    }

    @Test
    public void connectorConnectsToUnixListener() throws Exception {
        assumeUnixDomainSocketsSupported();
        final Path socketPath = socketPath("connect.sock");
        final UnixRouteListener listener = new UnixRouteListener(EndpointSpec.unix(socketPath, false));
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        RouteConnection serverConnection = null;
        RouteConnection clientConnection = null;
        try {
            final Future<RouteConnection> accepted = acceptAsync(executor, listener);
            clientConnection = new UnixRouteConnector(EndpointSpec.unix(socketPath, false)).connect();
            serverConnection = accepted.get(5, TimeUnit.SECONDS);

            Assert.assertNotNull(clientConnection);
            Assert.assertNotNull(serverConnection);
        } finally {
            close(clientConnection);
            close(serverConnection);
            close(listener);
            executor.shutdownNow();
        }
    }

    @Test
    public void connectionCanSendBytesClientToServer() throws Exception {
        assumeUnixDomainSocketsSupported();
        final Path socketPath = socketPath("client-to-server.sock");
        final UnixRouteListener listener = new UnixRouteListener(EndpointSpec.unix(socketPath, false));
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        RouteConnection serverConnection = null;
        RouteConnection clientConnection = null;
        try {
            final Future<RouteConnection> accepted = acceptAsync(executor, listener);
            clientConnection = new UnixRouteConnector(EndpointSpec.unix(socketPath, false)).connect();
            serverConnection = accepted.get(5, TimeUnit.SECONDS);

            final byte[] sent = new byte[] { 1, 2, 3, 4, 5 };
            final byte[] received = new byte[sent.length];
            clientConnection.output().write(sent);

            Assert.assertEquals(sent.length, serverConnection.input().read(received));
            Assert.assertArrayEquals(sent, received);
        } finally {
            close(clientConnection);
            close(serverConnection);
            close(listener);
            executor.shutdownNow();
        }
    }

    @Test
    public void connectionCanSendBytesServerToClient() throws Exception {
        assumeUnixDomainSocketsSupported();
        final Path socketPath = socketPath("server-to-client.sock");
        final UnixRouteListener listener = new UnixRouteListener(EndpointSpec.unix(socketPath, false));
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        RouteConnection serverConnection = null;
        RouteConnection clientConnection = null;
        try {
            final Future<RouteConnection> accepted = acceptAsync(executor, listener);
            clientConnection = new UnixRouteConnector(EndpointSpec.unix(socketPath, false)).connect();
            serverConnection = accepted.get(5, TimeUnit.SECONDS);

            final byte[] sent = new byte[] { 6, 7, 8, 9 };
            final byte[] received = new byte[sent.length];
            serverConnection.output().write(sent);

            Assert.assertEquals(sent.length, clientConnection.input().read(received));
            Assert.assertArrayEquals(sent, received);
        } finally {
            close(clientConnection);
            close(serverConnection);
            close(listener);
            executor.shutdownNow();
        }
    }

    @Test
    public void connectionCloseIsIdempotent() throws Exception {
        assumeUnixDomainSocketsSupported();
        final Path socketPath = socketPath("idempotent-close.sock");
        final UnixRouteListener listener = new UnixRouteListener(EndpointSpec.unix(socketPath, false));
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        RouteConnection serverConnection = null;
        RouteConnection clientConnection = null;
        try {
            final Future<RouteConnection> accepted = acceptAsync(executor, listener);
            clientConnection = new UnixRouteConnector(EndpointSpec.unix(socketPath, false)).connect();
            serverConnection = accepted.get(5, TimeUnit.SECONDS);

            clientConnection.close();
            clientConnection.close();
            serverConnection.close();
            serverConnection.close();
        } finally {
            close(clientConnection);
            close(serverConnection);
            close(listener);
            executor.shutdownNow();
        }
    }

    private Future<RouteConnection> acceptAsync(final ExecutorService executor, final RouteListener listener) {
        return executor.submit(new Callable<RouteConnection>() {
            public RouteConnection call() throws Exception {
                return listener.accept();
            }
        });
    }

    private Path socketPath(final String name) throws IOException {
        if (testDirectory == null) {
            testDirectory = Files.createTempDirectory("ttunnel-uds-test");
        }
        return testDirectory.resolve(name);
    }

    private void assumeUnixDomainSocketsSupported() {
        Path directory = null;
        Path socketPath = null;
        ServerSocketChannel channel = null;
        try {
            directory = Files.createTempDirectory("ttunnel-uds-probe");
            socketPath = directory.resolve("probe.sock");
            channel = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
            channel.bind(UnixDomainSocketAddress.of(socketPath));
        } catch (Throwable t) {
            Assume.assumeNoException(t);
        } finally {
            close(channel);
            if (socketPath != null) {
                try {
                    Files.deleteIfExists(socketPath);
                } catch (IOException ignored) {
                }
            }
            if (directory != null) {
                try {
                    Files.deleteIfExists(directory);
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static void close(final AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignored) {
        }
    }

    private static void deleteRecursively(final Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        if (Files.isDirectory(path)) {
            final DirectoryStream<Path> entries = Files.newDirectoryStream(path);
            try {
                for (Path entry : entries) {
                    deleteRecursively(entry);
                }
            } finally {
                entries.close();
            }
        }
        Files.deleteIfExists(path);
    }
}
