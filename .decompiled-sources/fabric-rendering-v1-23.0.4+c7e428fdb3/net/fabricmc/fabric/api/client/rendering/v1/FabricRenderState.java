/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1;

import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.NonExtendable
public interface FabricRenderState {
    default public <T> @Nullable T getData(RenderStateDataKey<T> key) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default public <T> T getDataOrDefault(RenderStateDataKey<T> key, T defaultValue) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default public <T> void setData(RenderStateDataKey<T> key, @Nullable T value) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default public void clearExtraData() {
        throw new UnsupportedOperationException("Implemented via mixin");
    }
}

