package com.crankuptheamps.ttunnel;

public class DevnullLogger implements ConnectionLogger {

    private final String name;

    DevnullLogger(final String route_name, final int id) {
        this.name = "devnull-" + route_name + "-" + id;
    }

    public void enable() {
        System.out.println(name + "enable()");
    }

    public void disable() {
        System.out.println(name + "disable()");
    }

    public void debug(final String msg) {
        System.out.println(name + "DEBUG: " + msg);
    }

    public void info(final String msg) {
        System.out.println(name + " INFO:  " + msg);
    }

    public void warn(final String msg) {
        System.out.println(name + " WARN: " + msg);
    }

    public void entering(final String name, final Object o, String ... args) {
        info(StringUtils.shortName(o.getClass()) + "-" + o.hashCode() + " entering " + name + "(" + StringUtils.join(args, ", ") + ")");
    }

    public void leaving(final String name, final Object o) {
        info(StringUtils.shortName(o.getClass()) + "-" + o.hashCode() +  " leaving " + name + "()");
    }

}
