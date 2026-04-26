/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.blockgetter.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.fabric.impl.blockgetter.client.RenderDataMapConsumer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={RenderRegionCache.class})
public abstract class RenderRegionCacheMixin {
    @Unique
    private static final AtomicInteger ERROR_COUNTER = new AtomicInteger();
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger(RenderRegionCacheMixin.class);

    @Inject(method={"createRegion"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/chunk/RenderRegionCache;getSectionDataCopy(Lnet/minecraft/world/level/Level;III)Lnet/minecraft/client/renderer/chunk/SectionCopy;")})
    private void copyDataForChunk(ClientLevel level, long sectionNode, CallbackInfoReturnable<RenderSectionRegion> cir, @Share(value="dataMap") LocalRef<Long2ObjectOpenHashMap<Object>> mapRef, @Local(name={"regionSectionX"}) int regionSectionX, @Local(name={"regionSectionY"}) int regionSectionY, @Local(name={"regionSectionZ"}) int regionSectionZ) {
        while (true) {
            try {
                mapRef.set(RenderRegionCacheMixin.mapChunk(level.getChunk(regionSectionX, regionSectionZ), SectionPos.of(sectionNode), mapRef.get()));
            }
            catch (ConcurrentModificationException e) {
                int count = ERROR_COUNTER.incrementAndGet();
                if (count > 5) continue;
                LOGGER.warn("[Block Entity Render Data] Encountered CME during render region build. A mod is accessing or changing chunk data outside the main thread. Retrying.", e);
                if (count != 5) continue;
                LOGGER.info("[Block Entity Render Data] Subsequent exceptions will be suppressed.");
                continue;
            }
            break;
        }
    }

    @Inject(method={"createRegion"}, at={@At(value="RETURN")})
    private void createDataMap(ClientLevel level, long l, CallbackInfoReturnable<RenderSectionRegion> cir, @Share(value="dataMap") LocalRef<Long2ObjectOpenHashMap<Object>> mapRef) {
        RenderSectionRegion rendererRegion = cir.getReturnValue();
        Long2ObjectOpenHashMap<Object> map = mapRef.get();
        if (map != null) {
            ((RenderDataMapConsumer)((Object)rendererRegion)).fabric_acceptRenderDataMap(map);
        }
    }

    @Unique
    private static Long2ObjectOpenHashMap<Object> mapChunk(LevelChunk chunk, SectionPos sectionPos, Long2ObjectOpenHashMap<Object> map) {
        if (chunk.getBlockEntities().isEmpty()) {
            return map;
        }
        int xMin = SectionPos.sectionToBlockCoord(sectionPos.x() - 1);
        int yMin = SectionPos.sectionToBlockCoord(sectionPos.y() - 1);
        int zMin = SectionPos.sectionToBlockCoord(sectionPos.z() - 1);
        int xMax = SectionPos.sectionToBlockCoord(sectionPos.x() + 1);
        int yMax = SectionPos.sectionToBlockCoord(sectionPos.y() + 1);
        int zMax = SectionPos.sectionToBlockCoord(sectionPos.z() + 1);
        for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
            Object data;
            BlockPos pos = entry.getKey();
            if (pos.getX() < xMin || pos.getX() > xMax || pos.getY() < yMin || pos.getY() > yMax || pos.getZ() < zMin || pos.getZ() > zMax || (data = entry.getValue().getRenderData()) == null) continue;
            if (map == null) {
                map = new Long2ObjectOpenHashMap();
            }
            map.put(pos.asLong(), data);
        }
        return map;
    }
}

