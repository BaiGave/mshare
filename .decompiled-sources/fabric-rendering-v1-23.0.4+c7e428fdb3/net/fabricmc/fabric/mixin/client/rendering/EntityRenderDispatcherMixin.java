/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.impl.client.rendering.ArmorRendererRegistryImpl;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={EntityRenderDispatcher.class})
class EntityRenderDispatcherMixin {
    EntityRenderDispatcherMixin() {
    }

    @Inject(method={"onResourceManagerReload"}, at={@At(value="TAIL")})
    private void createArmorRenderers(ResourceManager manager, CallbackInfo ci, @Local(name={"context"}) EntityRendererProvider.Context context) {
        ArmorRendererRegistryImpl.createArmorRenderers(context);
    }
}

