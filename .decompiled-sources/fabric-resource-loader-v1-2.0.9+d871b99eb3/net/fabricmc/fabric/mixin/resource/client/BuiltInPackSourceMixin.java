/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.resource.client;

import java.util.function.Consumer;
import net.fabricmc.fabric.impl.resource.pack.ModResourcePackCreator;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BuiltInPackSource.class})
public class BuiltInPackSourceMixin {
    @Inject(method={"loadPacks"}, at={@At(value="RETURN")})
    private void addBuiltinResourcePacks(Consumer<Pack> consumer, CallbackInfo ci) {
        if (this instanceof ClientPackSource) {
            ModResourcePackCreator.CLIENT_RESOURCE_PACK_PROVIDER.loadPacks(consumer);
        }
    }
}

