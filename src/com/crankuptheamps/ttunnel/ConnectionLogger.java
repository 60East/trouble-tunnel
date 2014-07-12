package com.crankuptheamps.ttunnel;

import java.util.Map;

/**
 * Created by gibbs on 5/11/14.
 */
public interface ConnectionLogger {

    public void enable();

    public void disable();

    public void debug(String msg);

    public void info(String msg);

    public void warn(String msg);

    public void entering(final String name, final Object o, String... args);

    public void leaving(final String name, final Object o);

}
