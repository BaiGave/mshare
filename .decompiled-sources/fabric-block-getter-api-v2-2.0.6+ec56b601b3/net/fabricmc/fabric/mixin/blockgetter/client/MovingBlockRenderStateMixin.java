/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.blockgetter.client;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={MovingBlockRenderState.class})
abstract class MovingBlockRenderStateMixin
implements BlockAndTintGetter {
    @Shadow
    public @Nullable Holder<Biome> biome;

    MovingBlockRenderStateMixin() {
    }

    @Override
    public boolean hasBiomes() {
        return this.biome != null;
    }

    @Override
    public Holder<Biome> getBiomeFabric(BlockPos pos) {
        return this.biome;
    }
}

