package com.crankuptheamps.ttunnel;

import java.util.Properties;

public interface ConfigHandler {

    public void config(String name, EndpointSpec listen, EndpointSpec remote, String log_dir, Properties[] filter_configs);

}
