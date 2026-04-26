/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.event.lifecycle.client;

import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ClientConfigurationPacketListenerImpl.class})
public class ClientConfigurationPacketListenerImplMixin {
    @Inject(method={"lambda$handleConfigurationFinished$0"}, at={@At(value="RETURN")})
    private void invokeTagsLoaded(ResourceProvider provider, CallbackInfoReturnable<RegistryAccess.Frozen> cir) {
        CommonLifecycleEvents.TAGS_LOADED.invoker().onTagsLoaded(cir.getReturnValue(), true);
    }
}

