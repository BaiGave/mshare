/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.util.UndashedUuid;
import java.io.IOException;
import java.util.UUID;

public class UUIDTypeAdapter
extends TypeAdapter<UUID> {
    @Override
    public void write(JsonWriter out, UUID value) throws IOException {
        out.value(UndashedUuid.toString(value));
    }

    @Override
    public UUID read(JsonReader in) throws IOException {
        return UndashedUuid.fromStringLenient(in.nextString());
    }
}

