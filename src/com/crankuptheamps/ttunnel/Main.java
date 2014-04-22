package com.crankuptheamps.ttunnel;


import com.crankuptheamps.ttunnel.filters.Filter;
import com.crankuptheamps.ttunnel.filters.WanFilter;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;


public class Main {


    public static void main(String[] args) throws Exception {
//        for (String s : args) {
//            PropertyConfigurator.configure(s);
//        }
//        ml.info("count: " + args.length);
//        ml.info("args[0]: " + args[0]);
//        ml.info("args[1]: " + args[1]);
//        return;
        final String usage = "trouble-tunnel  <config file> [ admin port ]";
        if (args.length == 0) {
            error("no cmdline args found");
            throw new Exception(usage);
        }
        info("parsing config file: " + args[0]);
        ConfigParser.parse(new File(args[0]), new MainConfigHandler());
        info("leaving main()");
    }

    private static void log(final String lvl, final String msg) {
        System.out.println("[Main] " + lvl + ": " + msg);
    }

    private static void info(final String msg) {
        log("INFO", msg);
    }

    private static void debug(final String msg) {
        log("DEBUG", msg);
    }

    private static void error(final String msg) {
        log("ERROR", msg);
    }

    private static void warn(final String msg) {
        log("WARN", msg);
    }

}

class MainConfigHandler implements ConfigHandler {


    @Override
    public void config(String name, String remote_addr, int listen_on, String log_dir, Properties[] filter_configs) {
                info("configuring(name:" + name + ", remote_addr:" + remote_addr + ", listen_on:" + listen_on + ", log_dir:" + log_dir + ", and " + filter_configs.length + " filter configs");
                try {
                    final Route route = new Route(name, listen_on, remote_addr, new File(log_dir), filter_configs);
                    info(name + " route constructed.");
                    new Thread(route, name).start();
                    info(name + " route thread started.");
                } catch (IOException e) {
                    error("route creation failed for " + name + e.getMessage());
                }
    }

    private void log(final String lvl, final String msg) {
        System.out.println("[Main] " + lvl + ": " + msg);
    }

    private void info(final String msg) {
        log("INFO", msg);
    }

    private void debug(final String msg) {
        log("DEBUG", msg);
    }

    private void error(final String msg) {
        log("ERROR", msg);
    }

    private void warn(final String msg) {
        log("WARN", msg);
    }

}