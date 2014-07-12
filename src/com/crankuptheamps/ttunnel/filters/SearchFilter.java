package com.crankuptheamps.ttunnel.filters;

import com.crankuptheamps.ttunnel.ConnectionProcessor;
import com.crankuptheamps.ttunnel.StringUtils;

import java.nio.CharBuffer;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// "action": "remove, replace, halt, pause_egress, pause_ingress, pause",


public class SearchFilter extends Filter {

    // "action": "remove, replace, halt, pause_egress, pause_ingress, pause",

    private static final String KEY_ACTION = "action";
    private static final String KEY_SEARCH_TERM= "search_term";
    private static final String KEY_REPLACEMENT = "replacement";
    private static final String KEY_PAUSE_DURATION = "pause_duration";

    private static final String ACTION_REPLACE = "replace";
    private static final String ACTION_PAUSE = "pause";
    private static final String ACTION_PAUSE_INGRESS = "pause_ingress";
    private static final String ACTION_PAUSE_EGRESS = "pause_egress";
    private static final String ACTION_HALT = "halt";

    private static final Set<String> ACTIONS = new HashSet<String>();
    static {
        ACTIONS.add(ACTION_REPLACE);
        ACTIONS.add(ACTION_PAUSE);
        ACTIONS.add(ACTION_PAUSE_EGRESS);
        ACTIONS.add(ACTION_PAUSE_INGRESS);
        ACTIONS.add(ACTION_HALT);
    }

    private final String action;
    private final String search_term;
    private final String replacement;
    private final long pause_duration;

    public SearchFilter(ConnectionProcessor connectionProcessor, Properties config) {
        super(connectionProcessor, config);
        requireConfigKey(KEY_ACTION);
        action = config.getProperty(KEY_ACTION);
        if (!ACTIONS.contains(action)) {
            throw new Error("unrecognized action: '" + action + "' choose from (" + StringUtils.join(ACTIONS.iterator(), ", ") + ")");
        }
        requireConfigKey(KEY_SEARCH_TERM);
        search_term = config.getProperty(KEY_SEARCH_TERM);
        if (action.equals(ACTION_REPLACE)) {
            requireConfigKey(KEY_REPLACEMENT);
            replacement = config.getProperty(KEY_REPLACEMENT);
            if (replacement.length() != search_term.length()) {
                throw new Error("search term(" + search_term + ") must be longer than or same length as replacement(" + replacement + ")");
            }
        } else {
            replacement = null;
        }
        if (action.equals(ACTION_PAUSE) || action.equals(ACTION_PAUSE_INGRESS) || action.equals(ACTION_PAUSE_EGRESS)) {
            requireConfigKey(KEY_PAUSE_DURATION);
            pause_duration = Long.parseLong(config.getProperty(KEY_PAUSE_DURATION));
        } else {
            pause_duration = 0;
        }
    }

    @java.lang.Override
    public int filter(int datum) {
        return datum;
    }

    @java.lang.Override
    public int filter(byte[] b, int off, int len) {
        int retval = len;
        final String str = new String(b, off, len);
        if (str.indexOf(search_term) != -1) {
            if (action.equals(ACTION_REPLACE)) {
                final byte[] processed = str.replaceAll(search_term, replacement).getBytes();
                System.arraycopy(processed, 0, b, off, processed.length);
            } else if (action.equals(ACTION_PAUSE)) {
                try {
                    getConnectionProcessor().get_logger().info("pausing for " + pause_duration + "ms");
                    Thread.sleep(pause_duration);
                } catch (InterruptedException e) {
                    getConnectionProcessor().get_logger().warn(StringUtils.get_stack(e));
                }
            }
        } else if (action.equals(ACTION_PAUSE_INGRESS)) {
            getConnectionProcessor().pause_ingress();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(pause_duration);
                    } catch (InterruptedException e) {
                        getConnectionProcessor().get_logger().warn(StringUtils.get_stack(e));
                    } finally {
                        getConnectionProcessor().resume_ingress();
                    }
                }
            }).start();
        } else if (action.equals(ACTION_PAUSE_EGRESS)) {
            getConnectionProcessor().pause_egress();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(pause_duration);
                    } catch (InterruptedException e) {
                        getConnectionProcessor().get_logger().warn(StringUtils.get_stack(e));
                    } finally {
                        getConnectionProcessor().resume_egress();
                    }
                }
            }).start();
        } else if (action.equals(ACTION_HALT)) {
            getConnectionProcessor().disconnect();
        } else {
            getConnectionProcessor().get_logger().warn("unrecognized action: " + action);
        }
        return retval;
    }

    public void debug(String s) {
        getConnectionProcessor().get_logger().debug(StringUtils.shortName(getClass()) + s);
    }

}
