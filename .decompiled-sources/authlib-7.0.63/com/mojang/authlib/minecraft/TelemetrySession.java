/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib.minecraft;

import com.mojang.authlib.minecraft.TelemetryEvent;

public interface TelemetrySession {
    public static final TelemetrySession DISABLED = new TelemetrySession(){

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public TelemetryEvent createNewEvent(String type) {
            return TelemetryEvent.EMPTY;
        }
    };

    public boolean isEnabled();

    public TelemetryEvent createNewEvent(String var1);
}

