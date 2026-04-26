/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1;

import java.util.function.Supplier;

public final class RenderStateDataKey<T> {
    private final Supplier<String> name;

    private RenderStateDataKey(Supplier<String> debugName) {
        this.name = debugName;
    }

    public static <T> RenderStateDataKey<T> create(Supplier<String> debugName) {
        return new RenderStateDataKey<T>(debugName);
    }

    public static <T> RenderStateDataKey<T> create() {
        return new RenderStateDataKey<T>(() -> "unnamed");
    }

    public String toString() {
        return "RenderStateDataKey(" + this.name.get() + ")";
    }
}

