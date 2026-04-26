/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.model.loading.v1;

import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import org.jspecify.annotations.Nullable;

public interface FabricModelManager {
    default public <T> @Nullable T getModel(ExtraModelKey<T> key) {
        throw new UnsupportedOperationException("Implemented via mixin.");
    }
}

