package com.crankuptheamps.ttunnel;

import com.crankuptheamps.ttunnel.StringUtils;

public class DevnullLogger implements ConnectionLogger {

    private final String name;

    DevnullLogger() {
        this.name = "devnull-logger";
    }

    public void enable() {
    }

    public void disable() {
    }

    public void debug(final String msg) {
    }

    public void info(final String msg) {
    }

    public void warn(final String msg) {
    }

    public void entering(final String name, final Object o, String... args) {
//        info(StringUtils.shortName(o.getClass()) + "-" + o.hashCode() + " entering " + name + "(" + StringUtils.join(args, ", ") + ")");
    }

    public void leaving(final String name, final Object o) {
//        info(StringUtils.shortName(o.getClass()) + "-" + o.hashCode() + " leaving " + name + "()");
    }

}
