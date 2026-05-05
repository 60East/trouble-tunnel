package com.crankuptheamps.ttunnel;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface RouteConnection extends Closeable {
    InputStream input() throws IOException;
    OutputStream output() throws IOException;
}
