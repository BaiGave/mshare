/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.object.builder;

import net.fabricmc.fabric.impl.object.builder.FabricEntityDataRegistryImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={EntityDataSerializers.class})
abstract class EntityDataSerializersMixin {
    EntityDataSerializersMixin() {
    }

    @Inject(method={"<clinit>"}, at={@At(value="TAIL")})
    private static void storeVanillaHandlers(CallbackInfo ci) {
        FabricEntityDataRegistryImpl.storeVanillaHandlers();
    }

    @Inject(method={"registerSerializer(Lnet/minecraft/network/syncher/EntityDataSerializer;)V"}, at={@At(value="HEAD")})
    private static void onHeadRegister(EntityDataSerializer<?> handler, CallbackInfo ci) {
        if (FabricEntityDataRegistryImpl.hasStoredVanillaHandlers() && FabricLoader.getInstance().isDevelopmentEnvironment()) {
            throw new IllegalStateException("Tried to register entity data serializer " + String.valueOf(handler) + " using registerSerializer.registerSerializer. This is not allowed as it can lead to desynchronization issues; use FabricEntityDataRegistry.register instead.");
        }
    }
}

