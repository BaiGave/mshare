/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;

@FunctionalInterface
public interface LivingEntityRenderLayerRegistrationCallback {
    public static final Event<LivingEntityRenderLayerRegistrationCallback> EVENT = EventFactory.createArrayBacked(LivingEntityRenderLayerRegistrationCallback.class, callbacks -> (entityType, entityRenderer, registrationHelper, context) -> {
        for (LivingEntityRenderLayerRegistrationCallback callback : callbacks) {
            callback.registerLayers(entityType, entityRenderer, registrationHelper, context);
        }
    });

    public void registerLayers(EntityType<? extends LivingEntity> var1, LivingEntityRenderer<?, ?, ?> var2, RegistrationHelper var3, EntityRendererProvider.Context var4);

    @ApiStatus.NonExtendable
    public static interface RegistrationHelper {
        public <T extends EntityRenderState> void register(RenderLayer<T, ? extends EntityModel<T>> var1);
    }
}

