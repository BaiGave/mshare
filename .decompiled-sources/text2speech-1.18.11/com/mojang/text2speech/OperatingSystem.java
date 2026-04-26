/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.text2speech;

import java.util.Locale;
import javax.annotation.Nullable;

public enum OperatingSystem {
    LINUX("linux"),
    WINDOWS("win"),
    MAC_OS("mac"),
    UNSUPPORTED(null);

    @Nullable
    private final String detectWith;

    private OperatingSystem(String detectWith) {
        this.detectWith = detectWith;
    }

    public static OperatingSystem get() {
        String test = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        for (OperatingSystem value : OperatingSystem.values()) {
            if (value.detectWith == null || !test.contains(value.detectWith)) continue;
            return value;
        }
        return UNSUPPORTED;
    }
}

