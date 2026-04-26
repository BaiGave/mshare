/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

public class OSHelper {
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static OSType osType;

    public static String getOs() {
        return OS;
    }

    public static boolean isMac() {
        return OSType.MAC.equals((Object)osType);
    }

    public static boolean isWindows() {
        return OSType.WINDOWS.equals((Object)osType);
    }

    public static boolean isLinux() {
        return OSType.LINUX.equals((Object)osType);
    }

    static {
        if (OS.contains("windows")) {
            osType = OSType.WINDOWS;
        } else if (OS.contains("mac")) {
            osType = OSType.MAC;
        } else if (OS.contains("nux") || OS.contains("nix")) {
            osType = OSType.LINUX;
        }
    }

    static enum OSType {
        MAC,
        WINDOWS,
        LINUX;

    }
}

