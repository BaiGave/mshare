/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.resource.v1.pack;

public enum PackActivationType {
    NORMAL,
    DEFAULT_ENABLED,
    ALWAYS_ENABLED;


    public boolean isEnabledByDefault() {
        return this == DEFAULT_ENABLED || this == ALWAYS_ENABLED;
    }
}

