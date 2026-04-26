/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.blockgetter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={LevelReader.class})
public interface LevelReaderMixin
extends BlockAndLightGetter {
    @Shadow
    public Holder<Biome> getBiome(BlockPos var1);

    @Override
    default public boolean hasBiomes() {
        return true;
    }

    @Override
    default public Holder<Biome> getBiomeFabric(BlockPos pos) {
        return this.getBiome(pos);
    }
}

