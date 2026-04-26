/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.biome;

import java.util.function.Function;
import net.fabricmc.fabric.impl.biome.NetherBiomeData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets={"net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList$Preset$1"})
public class NetherBiomePresetMixin {
    @Inject(method={"apply"}, at={@At(value="RETURN")}, cancellable=true)
    public <T> void apply(Function<ResourceKey<Biome>, T> function, CallbackInfoReturnable<Climate.ParameterList<T>> cir) {
        cir.setReturnValue(NetherBiomeData.withModdedBiomeEntries(cir.getReturnValue(), function));
    }
}

