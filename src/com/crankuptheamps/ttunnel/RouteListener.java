package com.crankuptheamps.ttunnel;

import java.io.Closeable;
import java.io.IOException;

public interface RouteListener extends Closeable {
    RouteConnection accept() throws IOException;
}
