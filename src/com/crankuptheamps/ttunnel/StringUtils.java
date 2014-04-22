package com.crankuptheamps.ttunnel;

/**
 * Created with IntelliJ IDEA.
 * User: gibbs
 * Date: 4/22/14
 * Time: 2:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class StringUtils {
    public static String join(final String[] args, final String delimiter) {
        final StringBuffer sb = new StringBuffer();
        for (int i = 0 ; i < args.length ; ++i) {
            sb.append(args[i]);
            if (i+1 < args.length) sb.append(delimiter);
        }
        return sb.toString();
    }

    public static String shortName(final Class c) {
        final String n = c.getName();
        final int i = n.lastIndexOf(".");
        return i == -1 || i == n.length() ? n : n.substring(i+1);
    }
}
