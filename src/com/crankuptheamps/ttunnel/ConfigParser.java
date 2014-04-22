package com.crankuptheamps.ttunnel;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

public class ConfigParser {

    public static void parse(final File file, final ConfigHandler handler) throws IOException, ParseException {
        final String raw = IOUtils.toString(new FileInputStream(file));
        final JSONArray routes = (JSONArray) JSONValue.parseWithException(raw);
        for (Iterator<Object> iter= routes.iterator() ; iter.hasNext() ; ) {
            final JSONObject route = (JSONObject)iter.next();

            final String remote_addr = route.get("remote_addr").toString();
            final String name = route.get("name").toString();
            final int listen_on = Integer.parseInt(route.get("listen_on").toString());
            final String log_dir = route.get("log_dir").toString();
            final JSONArray filters = (JSONArray)route.get("filters");
            Properties[] configs = new Properties[]{};
            if (filters != null && filters.size() > 0) {
                configs = new Properties[filters.size()];
                for (int i = 0; i < filters.size(); ++i) {
                    final JSONObject filter = (JSONObject) filters.get(i);
                    configs[i] = new Properties();
                    for (Object k : filter.keySet()) {
                        configs[i].setProperty((String) k, filter.get(k).toString());
                    }
                }
            }
            handler.config(name, remote_addr, listen_on, log_dir, configs);
        }
    }

}



