/*
 * Decompiled with CFR 0.152.
 */
package com.llamalad7.mixinextras.utils;

import com.llamalad7.mixinextras.config.MixinExtrasConfig;
import com.llamalad7.mixinextras.lib.apache.commons.mutable.MutableObject;
import com.llamalad7.mixinextras.lib.gson.Strictness;
import com.llamalad7.mixinextras.lib.gson.stream.JsonReader;
import com.llamalad7.mixinextras.service.MixinExtrasVersion;
import com.llamalad7.mixinextras.utils.ResourceUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;

public class MixinConfigUtils {
    private static final String KEY_TOP_LEVEL_MIN_VERSION = "minMixinExtrasVersion";
    private static final String KEY_SUBCONFIG = "mixinextras";
    private static final String KEY_MIN_VERSION = "minVersion";
    private static final String KEY_PARENT = "parent";
    private static final Map<String, MixinExtrasConfig> CONFIG_CACHE = new HashMap<String, MixinExtrasConfig>();

    public static void requireMinVersion(IMixinConfig config, MixinExtrasVersion desiredVersion, String featureName) {
        MixinExtrasVersion min = MixinConfigUtils.extraConfigFor((String)config.getName()).minVersion;
        if (min == null || min.getNumber() < desiredVersion.getNumber()) {
            throw new UnsupportedOperationException(String.format("In order to use %s, Mixin Config '%s' needs to declare a reliance on MixinExtras >=%s! E.g. `\"%s\": {\"%s\": \"%s\"}`", new Object[]{featureName, config, desiredVersion, KEY_SUBCONFIG, KEY_MIN_VERSION, MixinExtrasVersion.LATEST}));
        }
    }

    private static MixinExtrasConfig extraConfigFor(String configName) {
        MixinExtrasConfig result = CONFIG_CACHE.get(configName);
        if (result == null) {
            result = MixinConfigUtils.readMixinExtrasConfig(configName);
            CONFIG_CACHE.put(configName, result);
        }
        return result;
    }

    private static MixinExtrasConfig readMixinExtrasConfig(String configName) {
        MutableObject parent = new MutableObject();
        MutableObject minVersion = new MutableObject();
        MixinConfigUtils.readConfig(configName, reader -> {
            reader.beginObject();
            block10: while (reader.hasNext()) {
                String key;
                switch (key = reader.nextName()) {
                    case "mixinextras": {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String innerKey = reader.nextName();
                            if (innerKey.equals(KEY_MIN_VERSION) && minVersion.getValue() == null) {
                                minVersion.setValue(reader.nextString());
                                continue;
                            }
                            reader.skipValue();
                        }
                        reader.endObject();
                        continue block10;
                    }
                    case "minMixinExtrasVersion": {
                        if (minVersion.getValue() == null) {
                            minVersion.setValue(reader.nextString());
                            continue block10;
                        }
                        reader.skipValue();
                        continue block10;
                    }
                    case "parent": {
                        String parentName = reader.nextString();
                        parent.setValue(MixinConfigUtils.extraConfigFor(parentName));
                        continue block10;
                    }
                }
                reader.skipValue();
            }
        });
        return new MixinExtrasConfig(configName, (MixinExtrasConfig)parent.getValue(), (String)minVersion.getValue());
    }

    private static void readConfig(String configName, JsonProcessor compute) {
        try (JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(ResourceUtils.getResourceAsStream(configName), StandardCharsets.UTF_8)));){
            reader.setStrictness(Strictness.LENIENT);
            compute.process(reader);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to read mixin config " + configName, e);
        }
    }

    @FunctionalInterface
    private static interface JsonProcessor {
        public void process(JsonReader var1) throws IOException;
    }
}

