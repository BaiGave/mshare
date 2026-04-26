/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib.minecraft;

import com.mojang.authlib.minecraft.TelemetryPropertyContainer;

public interface TelemetryEvent
extends TelemetryPropertyContainer {
    public static final TelemetryEvent EMPTY = new TelemetryEvent(){

        @Override
        public void addProperty(String id, String value) {
        }

        @Override
        public void addProperty(String id, int value) {
        }

        @Override
        public void addProperty(String id, long value) {
        }

        @Override
        public void addProperty(String id, boolean value) {
        }

        @Override
        public void addNullProperty(String id) {
        }

        @Override
        public void send() {
        }
    };

    public void send();
}

