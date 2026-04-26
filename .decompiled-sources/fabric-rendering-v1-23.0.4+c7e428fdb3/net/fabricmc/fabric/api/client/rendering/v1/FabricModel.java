/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.NonExtendable
public interface FabricModel<S> {
    default public @Nullable ModelPart getChildPart(String name) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default public void copyTransforms(Model<?> model) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }
}

