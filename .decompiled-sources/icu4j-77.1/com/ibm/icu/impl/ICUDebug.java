/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl;

public final class ICUDebug {
    private static String params;
    private static boolean debug;
    private static boolean help;

    public static boolean enabled() {
        return debug;
    }

    public static boolean enabled(String arg) {
        if (debug) {
            boolean result;
            boolean bl = result = params.indexOf(arg) != -1;
            if (help) {
                System.out.println("\nICUDebug.enabled(" + arg + ") = " + result);
            }
            return result;
        }
        return false;
    }

    public static String value(String arg) {
        String result = "false";
        if (debug) {
            int index = params.indexOf(arg);
            if (index != -1) {
                int limit;
                result = params.length() > (index += arg.length()) && params.charAt(index) == '=' ? params.substring(index, (limit = params.indexOf(",", ++index)) == -1 ? params.length() : limit) : "true";
            }
            if (help) {
                System.out.println("\nICUDebug.value(" + arg + ") = " + result);
            }
        }
        return result;
    }

    static {
        try {
            params = System.getProperty("ICUDebug");
        }
        catch (SecurityException securityException) {
            // empty catch block
        }
        debug = params != null;
        boolean bl = help = debug && (params.equals("") || params.indexOf("help") != -1);
        if (debug) {
            System.out.println("\nICUDebug=" + params);
        }
    }
}

