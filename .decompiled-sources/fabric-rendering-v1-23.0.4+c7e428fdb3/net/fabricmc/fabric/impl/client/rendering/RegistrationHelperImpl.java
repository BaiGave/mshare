/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.rendering;

import java.util.Objects;
import java.util.function.Function;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public final class RegistrationHelperImpl
implements LivingEntityRenderLayerRegistrationCallback.RegistrationHelper {
    private final Function<RenderLayer<?, ?>, Boolean> delegate;

    public RegistrationHelperImpl(Function<RenderLayer<?, ?>, Boolean> delegate) {
        this.delegate = delegate;
    }

    @Override
    public <T extends EntityRenderState> void register(RenderLayer<T, ? extends EntityModel<T>> renderLayer) {
        Objects.requireNonNull(renderLayer, "Render layer cannot be null");
        this.delegate.apply(renderLayer);
    }
}

