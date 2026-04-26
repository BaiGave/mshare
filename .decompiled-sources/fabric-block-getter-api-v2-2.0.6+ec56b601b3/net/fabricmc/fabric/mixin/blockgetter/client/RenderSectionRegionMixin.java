/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.blockgetter.client;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.fabricmc.fabric.impl.blockgetter.client.RenderDataMapConsumer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={RenderSectionRegion.class})
public abstract class RenderSectionRegionMixin
implements BlockAndTintGetter,
RenderDataMapConsumer {
    @Shadow
    @Final
    private ClientLevel level;
    @Unique
    private @Nullable Long2ObjectMap<Object> fabric_renderDataMap;

    @Override
    public Object getBlockEntityRenderData(BlockPos pos) {
        return this.fabric_renderDataMap == null ? null : this.fabric_renderDataMap.get(pos.asLong());
    }

    @Override
    public void fabric_acceptRenderDataMap(Long2ObjectMap<Object> renderDataMap) {
        this.fabric_renderDataMap = renderDataMap;
    }

    @Override
    public boolean hasBiomes() {
        return true;
    }

    @Override
    public Holder<Biome> getBiomeFabric(BlockPos pos) {
        return this.level.getBiome(pos);
    }
}

