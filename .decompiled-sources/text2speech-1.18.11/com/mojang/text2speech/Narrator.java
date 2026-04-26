/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.text2speech;

import com.mojang.text2speech.NarratorLinux;
import com.mojang.text2speech.NarratorMac;
import com.mojang.text2speech.NarratorWindows;
import com.mojang.text2speech.OperatingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Narrator {
    public static final Logger LOGGER = LoggerFactory.getLogger(Narrator.class);
    public static final Narrator EMPTY = new Narrator(){

        @Override
        public void say(String msg, boolean interrupt, float volume) {
        }

        @Override
        public void clear() {
        }

        @Override
        public boolean active() {
            return false;
        }

        @Override
        public void destroy() {
        }
    };

    public void say(String var1, boolean var2, float var3);

    public void clear();

    default public boolean active() {
        return true;
    }

    public void destroy();

    public static Narrator getNarrator() {
        try {
            return switch (OperatingSystem.get()) {
                case OperatingSystem.LINUX -> new NarratorLinux();
                case OperatingSystem.WINDOWS -> new NarratorWindows();
                case OperatingSystem.MAC_OS -> new NarratorMac();
                default -> throw new InitializeException("Unsupported platform " + System.getProperty("os.name"));
            };
        }
        catch (FatalException e) {
            throw e;
        }
        catch (Throwable e) {
            LOGGER.error("Error while loading the narrator", e);
            return EMPTY;
        }
    }

    public static class InitializeException
    extends Exception {
        public InitializeException(String message, Throwable cause) {
            super(message, cause);
        }

        public InitializeException(String message) {
            super(message);
        }
    }

    public static class FatalException
    extends RuntimeException {
        public FatalException(String message) {
            super(message);
        }
    }
}

