package com.crankuptheamps.ttunnel;

import java.util.Properties;

public interface ConfigHandler {

    public void config(String name, String remote_addr, int listen_on, String log_dir, Properties[] filter_configs);

}

