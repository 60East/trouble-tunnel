package com.crankuptheamps.ttunnel;

public interface ConnectionLogger {

    public void enable();

    public void disable();

    public void debug(final String msg);

    public void info(final String msg);

    public void warn(final String msg);

    public void entering(String name, Object o, String... args);

    public void leaving(String name, Object o);

}
