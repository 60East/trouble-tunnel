package com.crankuptheamps.ttunnel;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class UnixRouteTransportTest {

    private Path testDirectory;

    @BeforeClass
    public static void checkUnixDomainSocketsSupported() throws Exception {
        Path directory = null;
        Path socketPath = null;
        
        try {
            directory = Files.createTempDirectory("ttunnel-uds-probe");
            socketPath = directory.resolve("probe.sock");
            
            try (ServerSocketChannel channel = ServerSocketChannel.open(StandardProtocolFamily.UNIX);) {
                channel.bind(UnixDomainSocketAddress.of(socketPath));
            } catch (Throwable t) {
                Assume.assumeNoException(t);
            }
        } finally {
            if (socketPath != null) {
                try {
                    Files.deleteIfExists(socketPath);
                } catch (IOException ignored) {}
            }
            if (directory != null) {
                try {
                    Files.deleteIfExists(directory);
                } catch (IOException ignored) {}
            }
        }
    }

    @After
    public void cleanUp() throws Exception {
        if (testDirectory != null) {
            deleteRecursively(testDirectory);
        }
    }

    @Test
    public void createsParentDirectoriesForSocketPath() throws Exception {
        final Path socketPath = socketPath("nested/listener.sock");
        
        try (UnixRouteListener listener = new UnixRouteListener(EndpointSpec.unix(socketPath, false));) {
            Assert.assertTrue(Files.isDirectory(socketPath.getParent()));
            Assert.assertTrue(Files.exists(socketPath));
        }
    }

    @Test(expected = IOException.class)
    public void failsWhenSocketPathExistsAndUnlinkExistingIsFalse() throws Exception {
        final Path socketPath = socketPath("existing.sock");
        Files.createFile(socketPath);

        try (UnixRouteListener listener = new UnixRouteListener(EndpointSpec.unix(socketPath, false));) {}
    }
    
    @Test(expected = IOException.class)
    public void failsWhenSocketPathIsRegularFileAndUnlinkExistingIsTrue() throws Exception {
        final Path socketPath = socketPath("regularFile.txt");
        Files.createFile(socketPath);

        try (UnixRouteListener listener = new UnixRouteListener(EndpointSpec.unix(socketPath, true));) {}
    }
    
    @Test(expected = IOException.class)
    public void failsWhenSocketPathIsDirectoryAndUnlinkExistingIsTrue() throws Exception {
        socketPath("regularFile.txt");

        try (UnixRouteListener listener = new UnixRouteListener(EndpointSpec.unix(testDirectory, true));) {}
    }
    
    @Test(expected = IOException.class)
    public void failsWhenSocketPathIsSymbolicLinkAndUnlinkExistingIsTrue() throws Exception {
        final Path socketPath = socketPath("replace.sock");

        UnixDomainSocketAddress address = UnixDomainSocketAddress.of(socketPath);

        try (ServerSocketChannel serverChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX)) {
            serverChannel.bind(address);
            final Path symbolicLinkPath = testDirectory.resolve("link.sock");
            Files.createSymbolicLink(symbolicLinkPath, socketPath);
            try (UnixRouteListener listener = new UnixRouteListener(EndpointSpec.unix(symbolicLinkPath, true));) {}
        }
    }

    @Test(expected = IOException.class)
    public void failsWhenSocketParentCanBeModifiedByOtherUsers() throws Exception {
        final Path unsafeParent = socketPath("unsafe-parent");
        Files.createDirectory(unsafeParent);
        setPermissionsOrSkip(unsafeParent, EnumSet.of(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE,
            PosixFilePermission.OWNER_EXECUTE,
            PosixFilePermission.GROUP_READ,
            PosixFilePermission.GROUP_WRITE,
            PosixFilePermission.GROUP_EXECUTE,
            PosixFilePermission.OTHERS_READ,
            PosixFilePermission.OTHERS_WRITE,
            PosixFilePermission.OTHERS_EXECUTE));

        try (UnixRouteListener listener = new UnixRouteListener(EndpointSpec.unix(unsafeParent.resolve("listener.sock"), false));) {}
    }

    @Test
    public void replacesExistingSocketPathWhenUnlinkExistingIsTrue() throws Exception {
        final Path socketPath = socketPath("replace.sock");

        UnixDomainSocketAddress address = UnixDomainSocketAddress.of(socketPath);

        try (ServerSocketChannel serverChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX)) {
            serverChannel.bind(address);
            try (UnixRouteListener listener = new UnixRouteListener(EndpointSpec.unix(socketPath, true));) {
                Assert.assertTrue(Files.exists(socketPath));
            }
        }
    }


    @Test
    public void deletesSocketPathOnClose() throws Exception {
        final Path socketPath = socketPath("close.sock");
        
        try (UnixRouteListener listener = new UnixRouteListener(EndpointSpec.unix(socketPath, false));) {
            Assert.assertTrue(Files.exists(socketPath));
            listener.close();
            Assert.assertFalse(Files.exists(socketPath));
        }
    }

    @Test
    public void connectorConnectsToUnixListener() throws Exception {
        final Path socketPath = socketPath("connect.sock");
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        
        try (UnixRouteListener listener = new UnixRouteListener(EndpointSpec.unix(socketPath, false));) {
            final Future<RouteConnection> accepted = acceptAsync(executor, listener);
            
            try (RouteConnection clientConnection = new UnixRouteConnector(EndpointSpec.unix(socketPath, false)).connect();
                    RouteConnection serverConnection = accepted.get(5, TimeUnit.SECONDS);) {
                Assert.assertNotNull(clientConnection);
                Assert.assertNotNull(serverConnection);
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void connectionCanSendBytesClientToServer() throws Exception {
        final Path socketPath = socketPath("client-to-server.sock");
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        
        try (UnixRouteListener listener = new UnixRouteListener(EndpointSpec.unix(socketPath, false));) {
            final Future<RouteConnection> accepted = acceptAsync(executor, listener);
            
            try (RouteConnection clientConnection = new UnixRouteConnector(EndpointSpec.unix(socketPath, false)).connect();
                    RouteConnection serverConnection = accepted.get(5, TimeUnit.SECONDS);) {
                final byte[] sent = new byte[] { 1, 2, 3, 4, 5 };
                final byte[] received = new byte[sent.length];
                clientConnection.output().write(sent);

                Assert.assertEquals(sent.length, serverConnection.input().read(received));
                Assert.assertArrayEquals(sent, received);
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void connectionCanSendBytesServerToClient() throws Exception {
        final Path socketPath = socketPath("server-to-client.sock");
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        
        try (UnixRouteListener listener = new UnixRouteListener(EndpointSpec.unix(socketPath, false));) {
            final Future<RouteConnection> accepted = acceptAsync(executor, listener);
            
            try (RouteConnection clientConnection = new UnixRouteConnector(EndpointSpec.unix(socketPath, false)).connect();
                    RouteConnection serverConnection = accepted.get(5, TimeUnit.SECONDS);) {
                final byte[] sent = new byte[] { 6, 7, 8, 9 };
                final byte[] received = new byte[sent.length];
                serverConnection.output().write(sent);

                Assert.assertEquals(sent.length, clientConnection.input().read(received));
                Assert.assertArrayEquals(sent, received);
            }
        } finally {
            executor.shutdownNow();
        }
    }
    
    @Test
    public void socketHasOwnerPermissions() throws Exception {
        final Path socketPath = socketPath("owner-permissions.sock");
        
        try (UnixRouteListener listener = new UnixRouteListener(EndpointSpec.unix(socketPath, false));) {
            Set<PosixFilePermission> perms = Files.getPosixFilePermissions(socketPath);

            Assert.assertEquals("Should have only 2 permissions set.", 2, perms.size());
            Assert.assertTrue("Should have owner write.", perms.contains(PosixFilePermission.OWNER_WRITE));
            Assert.assertTrue("Should have owner read.", perms.contains(PosixFilePermission.OWNER_READ));
        }
    }

    @Test
    public void connectionCloseIsIdempotent() throws Exception {
        final Path socketPath = socketPath("idempotent-close.sock");
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        
        try (UnixRouteListener listener = new UnixRouteListener(EndpointSpec.unix(socketPath, false));) {
            final Future<RouteConnection> accepted = acceptAsync(executor, listener);
            
            try (RouteConnection clientConnection = new UnixRouteConnector(EndpointSpec.unix(socketPath, false)).connect();
                    RouteConnection serverConnection = accepted.get(5, TimeUnit.SECONDS);) {
                clientConnection.close();
                clientConnection.close();
                serverConnection.close();
                serverConnection.close();
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void processorDisconnectClosesUnixRouteConnections() throws Exception {
        final Path socketPath = socketPath("processor-disconnect.sock");
        final ExecutorService executor = Executors.newSingleThreadExecutor();

        try (UnixRouteListener listener = new UnixRouteListener(EndpointSpec.unix(socketPath, false));) {
            final Future<RouteConnection> accepted = acceptAsync(executor, listener);

            final RouteConnection clientConnection = new UnixRouteConnector(EndpointSpec.unix(socketPath, false)).connect();
            final RouteConnection serverConnection = accepted.get(5, TimeUnit.SECONDS);

            try {
                final ConnectionProcessorImpl processor = new ConnectionProcessorImpl(
                    clientConnection,
                    serverConnection,
                    "processor-disconnect",
                    null,
                    new Properties[] {});

                processor.disconnect();

                try {
                    clientConnection.output().write(new byte[] { 1 });
                    Assert.fail("expected write to fail after connection close");
                } catch (IOException expected) {}

                try {
                    serverConnection.output().write(new byte[] { 1 });
                    Assert.fail("expected write to fail after connection close");
                } catch (IOException expected) {}
            } finally {
                clientConnection.close();
                serverConnection.close();
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void routeStopClosesActiveUnixRouteConnections() throws Exception {
        final Path listenPath = socketPath("route-stop-listen.sock");
        final Path remotePath = socketPath("route-stop-remote.sock");
        final ExecutorService executor = Executors.newCachedThreadPool();
        final Route route = new Route(
            "route-stop",
            EndpointSpec.unix(listenPath, false),
            EndpointSpec.unix(remotePath, false),
            testDirectory.toFile(),
            new Properties[] {});
        final Thread routeThread = new Thread(route);

        try (UnixRouteListener remoteListener = new UnixRouteListener(EndpointSpec.unix(remotePath, false));) {
            routeThread.start();
            
            final long deadline = System.currentTimeMillis() + 5000;

            while (!Files.exists(listenPath) && System.currentTimeMillis() < deadline) {
                Thread.sleep(10);
            }

            Assert.assertTrue("timed out waiting for " + listenPath, Files.exists(listenPath));

            final Future<RouteConnection> acceptedRemote = acceptAsync(executor, remoteListener);
            final RouteConnection clientConnection = new UnixRouteConnector(EndpointSpec.unix(listenPath, false)).connect();
            final RouteConnection serverConnection = acceptedRemote.get(5, TimeUnit.SECONDS);

            try {
                clientConnection.output().write(new byte[] { 42 });
                Assert.assertEquals(42, serverConnection.input().read());

                route.stop();
                routeThread.join(5000);

                Assert.assertFalse(routeThread.isAlive());
                Assert.assertEquals(-1, readByteAsync(executor, clientConnection).get(5, TimeUnit.SECONDS).intValue());
                Assert.assertEquals(-1, readByteAsync(executor, serverConnection).get(5, TimeUnit.SECONDS).intValue());
            } finally {
                route.stop();
                clientConnection.close();
                serverConnection.close();
            }
        } finally {
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

    private Future<Integer> readByteAsync(final ExecutorService executor, final RouteConnection connection) {
        return executor.submit(new Callable<Integer>() {
            public Integer call() throws Exception {
                return connection.input().read();
            }
        });
    }

    private Path socketPath(final String name) throws IOException {
        if (testDirectory == null) {
            testDirectory = Files.createTempDirectory("ttunnel-uds-test");
        }
        return testDirectory.resolve(name);
    }

    private static void setPermissionsOrSkip(final Path path, final Set<PosixFilePermission> permissions) throws IOException {
        try {
            Files.setPosixFilePermissions(path, permissions);
        } catch (UnsupportedOperationException e) {
            Assume.assumeNoException(e);
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
