/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib.minecraft;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public interface TelemetryPropertyContainer {
    public void addProperty(String var1, String var2);

    public void addProperty(String var1, int var2);

    public void addProperty(String var1, long var2);

    public void addProperty(String var1, boolean var2);

    public void addNullProperty(String var1);

    public static TelemetryPropertyContainer forJsonObject(final JsonObject object) {
        return new TelemetryPropertyContainer(){

            @Override
            public void addProperty(String id, String value) {
                object.addProperty(id, value);
            }

            @Override
            public void addProperty(String id, int value) {
                object.addProperty(id, value);
            }

            @Override
            public void addProperty(String id, long value) {
                object.addProperty(id, value);
            }

            @Override
            public void addProperty(String id, boolean value) {
                object.addProperty(id, value);
            }

            @Override
            public void addNullProperty(String id) {
                object.add(id, JsonNull.INSTANCE);
            }
        };
    }
}

