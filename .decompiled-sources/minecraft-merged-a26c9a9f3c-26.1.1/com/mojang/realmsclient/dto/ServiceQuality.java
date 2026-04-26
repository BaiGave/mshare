/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.realmsclient.dto;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public enum ServiceQuality {
    GREAT(1, "icon/ping_5"),
    GOOD(2, "icon/ping_4"),
    OKAY(3, "icon/ping_3"),
    POOR(4, "icon/ping_2"),
    UNKNOWN(5, "icon/ping_unknown");

    private final int value;
    private final Identifier icon;

    private ServiceQuality(int value, String iconPath) {
        this.value = value;
        this.icon = Identifier.withDefaultNamespace(iconPath);
    }

    public static @Nullable ServiceQuality byValue(int value) {
        for (ServiceQuality quality : ServiceQuality.values()) {
            if (quality.getValue() != value) continue;
            return quality;
        }
        return null;
    }

    public int getValue() {
        return this.value;
    }

    public Identifier getIcon() {
        return this.icon;
    }

    @Environment(value=EnvType.CLIENT)
    public static class RealmsServiceQualityJsonAdapter
    extends TypeAdapter<ServiceQuality> {
        private static final Logger LOGGER = LogUtils.getLogger();

        @Override
        public void write(JsonWriter jsonWriter, ServiceQuality quality) throws IOException {
            jsonWriter.value(quality.value);
        }

        @Override
        public ServiceQuality read(JsonReader jsonReader) throws IOException {
            int value = jsonReader.nextInt();
            ServiceQuality quality = ServiceQuality.byValue(value);
            if (quality == null) {
                LOGGER.warn("Unsupported ServiceQuality {}", (Object)value);
                return UNKNOWN;
            }
            return quality;
        }
    }
}

