/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.biome.modification;

import net.fabricmc.fabric.impl.biome.modification.BiomeModificationImpl;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={MinecraftServer.class})
public abstract class MinecraftServerMixin {
    @Shadow
    public abstract RegistryAccess.Frozen registryAccess();

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void finalizeWorldGen(CallbackInfo ci) {
        BiomeModificationImpl.INSTANCE.finalizeWorldGen(this.registryAccess());
    }
}

