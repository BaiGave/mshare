/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.resource;

import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;

@Deprecated
public enum ResourcePackActivationType {
    NORMAL(PackActivationType.NORMAL),
    DEFAULT_ENABLED(PackActivationType.DEFAULT_ENABLED),
    ALWAYS_ENABLED(PackActivationType.ALWAYS_ENABLED);

    final PackActivationType replacement;

    private ResourcePackActivationType(PackActivationType replacement) {
        this.replacement = replacement;
    }

    public boolean isEnabledByDefault() {
        return this == DEFAULT_ENABLED || this == ALWAYS_ENABLED;
    }
}

