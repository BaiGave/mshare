/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.biome;

import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={BiomeSource.class})
public class BiomeSourceMixin {
    @Redirect(method={"possibleBiomes"}, at=@At(value="INVOKE", target="Ljava/util/function/Supplier;get()Ljava/lang/Object;"))
    private Object getBiomes(Supplier<Set<Holder<Biome>>> instance) {
        return this.modifyBiomeSet(instance.get());
    }

    @Unique
    protected Set<Holder<Biome>> modifyBiomeSet(Set<Holder<Biome>> biomes) {
        return biomes;
    }
}

