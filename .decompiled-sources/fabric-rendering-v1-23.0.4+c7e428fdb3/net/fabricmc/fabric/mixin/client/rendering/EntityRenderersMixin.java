/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.Map;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.impl.client.rendering.EntityRendererRegistryImpl;
import net.fabricmc.fabric.impl.client.rendering.RegistrationHelperImpl;
import net.fabricmc.fabric.mixin.client.rendering.LivingEntityRendererAccessor;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={EntityRenderers.class})
public abstract class EntityRenderersMixin {
    @Shadow
    @Final
    private static Map<EntityType<?>, EntityRendererProvider<?>> PROVIDERS;

    @Inject(method={"<clinit>*"}, at={@At(value="RETURN")})
    private static void onRegisterRenderers(CallbackInfo info) {
        EntityRendererRegistryImpl.setup((t, factory) -> PROVIDERS.put((EntityType<?>)t, (EntityRendererProvider<?>)factory));
    }

    @Redirect(method={"lambda$createEntityRenderers$0"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/entity/EntityRendererProvider;create(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)Lnet/minecraft/client/renderer/entity/EntityRenderer;"))
    private static EntityRenderer<?, ?> createEntityRenderer(EntityRendererProvider<?> entityRendererProvider, EntityRendererProvider.Context context, ImmutableMap.Builder builder, EntityRendererProvider.Context context2, EntityType<?> entityType) {
        EntityRenderer<?, ?> entityRenderer = entityRendererProvider.create(context);
        if (entityRenderer instanceof LivingEntityRenderer) {
            LivingEntityRendererAccessor accessor = (LivingEntityRendererAccessor)((Object)entityRenderer);
            LivingEntityRenderLayerRegistrationCallback.EVENT.invoker().registerLayers(entityType, (LivingEntityRenderer)entityRenderer, new RegistrationHelperImpl(accessor::callAddLayer), context);
        }
        return entityRenderer;
    }

    @WrapOperation(method={"createAvatarRenderers"}, at={@At(value="NEW", target="(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;Z)Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;")})
    private static AvatarRenderer createAvatarRenderer(EntityRendererProvider.Context context, boolean slim, Operation<AvatarRenderer> original) {
        AvatarRenderer entityRenderer = original.call(context, slim);
        LivingEntityRendererAccessor accessor = (LivingEntityRendererAccessor)((Object)entityRenderer);
        LivingEntityRenderLayerRegistrationCallback.EVENT.invoker().registerLayers(EntityType.PLAYER, entityRenderer, new RegistrationHelperImpl(accessor::callAddLayer), context);
        return entityRenderer;
    }
}

