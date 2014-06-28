package com.crankuptheamps.ttunnel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Random;
import java.util.StringTokenizer;

public class StringUtils {
    public static String join(final String[] args, final String delimiter) {
        final StringBuffer sb = new StringBuffer();
        for (int i = 0 ; i < args.length ; ++i) {
            sb.append(args[i]);
            if (i+1 < args.length) sb.append(delimiter);
        }
        return sb.toString();
    }

    public static String join(final Iterator<String> strings, final String delimiter) {
        final StringBuffer sb = new StringBuffer();
        while (strings.hasNext()) {
            sb.append(strings.next());
            if (strings.hasNext()) sb.append(delimiter);
        }
        return sb.toString();
    }

    public static String shortName(final Class c) {
        final String n = c.getName();
        final int i = n.lastIndexOf(".");
        return i == -1 || i == n.length() ? n : n.substring(i+1);
    }

    public static String lipsum() {
        return "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.".replaceAll(",", "");
    }

    public static String lipsum(final int len) {
        final String[] tokens = lipsum().split(" ");
        final Random r = new Random();
        final StringBuilder sb = new StringBuilder();
        while (sb.length() < len) {
            sb.append(tokens[r.nextInt(tokens.length)] + " ");
        }
        return sb.toString().substring(0, len);
    }

    public static String get_stack(final Throwable t) {
        final PrintWriter pw = new PrintWriter(new StringWriter());
        t.printStackTrace(pw);
        return pw.toString();
    }

}
