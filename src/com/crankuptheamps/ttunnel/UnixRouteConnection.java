package com.crankuptheamps.ttunnel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class UnixRouteConnection implements RouteConnection {

    private final SocketChannel channel;
    private final SelectableChannelInputStream input;
    private final SelectableChannelOutputStream output;
    private boolean closed;

    public UnixRouteConnection(final SocketChannel channel) throws IOException {
        this.channel = channel;
        this.channel.configureBlocking(false);
        this.input = new SelectableChannelInputStream(this.channel);
        this.output = new SelectableChannelOutputStream(this.channel);
    }

    public InputStream input() throws IOException {
        return input;
    }

    public OutputStream output() throws IOException {
        return output;
    }

    public synchronized void close() throws IOException {
        if (closed) {
            return;
        }

        closed = true;
        IOException thrown = null;

        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException e) {
            thrown = e;
        }

        try {
            if (output != null) {
                output.close();
            }
        } catch (IOException e) {
            if (thrown == null) {
                thrown = e;
            }
        }

        try {
            channel.close();
        } catch (IOException e) {
            if (thrown == null) {
                thrown = e;
            }
        }

        if (thrown != null) {
            throw thrown;
        }
    }

    private static final class SelectableChannelInputStream extends InputStream {

        private final SocketChannel channel;
        private final Selector selector;

        SelectableChannelInputStream(final SocketChannel channel) throws IOException {
            this.channel = channel;
            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_READ);
        }

        public int read() throws IOException {
            final byte[] b = new byte[1];
            final int read = read(b, 0, 1);
            return read == -1 ? -1 : b[0] & 0xff;
        }

        public int read(final byte[] b, final int off, final int len) throws IOException {
            if (len == 0) {
                return 0;
            }

            final ByteBuffer dst = ByteBuffer.wrap(b, off, len);

            while (true) {
                final int read = channel.read(dst);

                if (read != 0) {
                    return read;
                }

                waitForRead();
            }
        }

        public void close() throws IOException {
            try {
                selector.close();
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        private void waitForRead() throws IOException {
            try {
                while (selector.select() == 0) { }

                final Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    keys.next();
                    keys.remove();
                }
            } catch (ClosedSelectorException e) {
                throw new IOException(e);
            }
        }
    }

    private static final class SelectableChannelOutputStream extends OutputStream {

        private final SocketChannel channel;
        private final Selector selector;

        SelectableChannelOutputStream(final SocketChannel channel) throws IOException {
            this.channel = channel;
            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_WRITE);
        }

        public void write(final int b) throws IOException {
            final byte[] bytes = new byte[] { (byte)b };
            write(bytes, 0, bytes.length);
        }

        public void write(final byte[] b, final int off, final int len) throws IOException {
            final ByteBuffer src = ByteBuffer.wrap(b, off, len);

            while (src.hasRemaining()) {
                final int written = channel.write(src);

                if (written == 0) {
                    waitForWrite();
                }
            }
        }

        public void close() throws IOException {
            try {
                selector.close();
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        private void waitForWrite() throws IOException {
            try {
                while (selector.select() == 0) { }

                final Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    keys.next();
                    keys.remove();
                }
            } catch (ClosedSelectorException e) {
                throw new IOException(e);
            }
        }
    }
}
