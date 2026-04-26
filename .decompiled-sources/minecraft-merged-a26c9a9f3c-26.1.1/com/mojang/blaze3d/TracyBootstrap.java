/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d;

import com.mojang.jtracy.TracyClient;
import com.mojang.logging.LogListeners;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.event.Level;

@Environment(value=EnvType.CLIENT)
public class TracyBootstrap {
    private static boolean setup;

    public static void setup() {
        if (setup) {
            return;
        }
        TracyClient.load();
        if (!TracyClient.isAvailable()) {
            return;
        }
        LogListeners.addListener("Tracy", (message, level) -> TracyClient.message(message, TracyBootstrap.messageColor(level)));
        setup = true;
    }

    private static int messageColor(Level level) {
        return switch (level) {
            default -> 0xFFFFFF;
            case Level.DEBUG -> 0xAAAAAA;
            case Level.WARN -> 0xFFFFAA;
            case Level.ERROR -> 0xFFAAAA;
        };
    }
}

