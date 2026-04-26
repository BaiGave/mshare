/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.realmsclient.dto;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RealmsText {
    private static final String TRANSLATION_KEY = "translationKey";
    private static final String ARGS = "args";
    private final String translationKey;
    private final String @Nullable [] args;

    private RealmsText(String translationKey, String @Nullable [] args) {
        this.translationKey = translationKey;
        this.args = args;
    }

    public Component createComponent(Component fallback) {
        return Objects.requireNonNullElse(this.createComponent(), fallback);
    }

    public @Nullable Component createComponent() {
        if (!I18n.exists(this.translationKey)) {
            return null;
        }
        if (this.args == null) {
            return Component.translatable(this.translationKey);
        }
        return Component.translatable(this.translationKey, this.args);
    }

    public static RealmsText parse(JsonObject jsonObject) {
        String[] args;
        String translationKey = JsonUtils.getRequiredString(TRANSLATION_KEY, jsonObject);
        JsonElement argsJsonElement = jsonObject.get(ARGS);
        if (argsJsonElement == null || argsJsonElement.isJsonNull()) {
            args = null;
        } else {
            JsonArray argsJsonArray = argsJsonElement.getAsJsonArray();
            args = new String[argsJsonArray.size()];
            for (int i = 0; i < argsJsonArray.size(); ++i) {
                args[i] = argsJsonArray.get(i).getAsString();
            }
        }
        return new RealmsText(translationKey, args);
    }

    public String toString() {
        return this.translationKey;
    }
}

