package com.crankuptheamps.ttunnel;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Properties;

public class ConfigParser {

    public static void parse(final File file, final ConfigHandler handler) throws IOException, ParseException {
        final String raw = IOUtils.toString(new FileInputStream(file));
        final JSONArray routes = (JSONArray) JSONValue.parseWithException(raw);

        for (Iterator<Object> iter= routes.iterator() ; iter.hasNext() ; ) {
            final JSONObject route = (JSONObject)iter.next();
            final String name = route.get("name").toString();
            final EndpointSpec listen = parseListenEndpoint(route);
            final EndpointSpec remote = parseRemoteEndpoint(route);
            final String log_dir = route.get("log_dir").toString();
            final JSONArray filters = (JSONArray)route.get("filters");
            Properties[] configs = new Properties[] {};

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

            handler.config(name, listen, remote, log_dir, configs);
        }
    }

    private static EndpointSpec parseListenEndpoint(final JSONObject route) {
        final Object explicit = route.get("listen");

        if (explicit != null) {
            return parseEndpoint((JSONObject) explicit, true);
        }

        final int listen_on = Integer.parseInt(route.get("listen_on").toString());
        return EndpointSpec.tcp(null, listen_on);
    }

    private static EndpointSpec parseRemoteEndpoint(final JSONObject route) {
        final Object explicit = route.get("remote");

        if (explicit != null) {
            return parseEndpoint((JSONObject) explicit, false);
        }

        return parseLegacyRemote(route.get("remote_addr").toString());
    }

    private static EndpointSpec parseLegacyRemote(final String remoteAddress) {
        if (remoteAddress.indexOf(":") == -1) {
            throw new Error("port missing in remote address, expected <hostname>:<port>: " + remoteAddress);
        }

        final String host = remoteAddress.substring(0, remoteAddress.indexOf(":"));
        final int port = Integer.parseInt(remoteAddress.substring(remoteAddress.indexOf(":") + 1, remoteAddress.length()));
        return EndpointSpec.tcp(host, port);
    }

    private static EndpointSpec parseEndpoint(final JSONObject endpoint, final boolean listener) {
        final String type = endpoint.get("type").toString();

        if ("unix".equalsIgnoreCase(type)) {
            final Object unlinkExisting = endpoint.get("unlink_existing");
            return EndpointSpec.unix(Paths.get(endpoint.get("path").toString()),
                                     unlinkExisting != null && Boolean.parseBoolean(unlinkExisting.toString()));
        }

        if ("tcp".equalsIgnoreCase(type)) {
            final Object host = endpoint.get("host");

            if (!listener && host == null) {
                throw new Error("remote tcp endpoint requires host");
            }

            return EndpointSpec.tcp(host == null ? null : host.toString(), Integer.parseInt(endpoint.get("port").toString()));
        }

        throw new Error("unknown endpoint type: " + type);
    }

}
