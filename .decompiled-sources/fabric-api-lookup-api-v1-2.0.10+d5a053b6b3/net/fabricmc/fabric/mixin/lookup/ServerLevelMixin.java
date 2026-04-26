/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.lookup;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.impl.lookup.block.BlockApiCacheImpl;
import net.fabricmc.fabric.impl.lookup.block.ServerLevelCache;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={ServerLevel.class})
abstract class ServerLevelMixin
implements ServerLevelCache {
    @Unique
    private final Map<BlockPos, List<WeakReference<BlockApiCacheImpl<?, ?>>>> apiLookupCaches = new Object2ReferenceOpenHashMap();
    @Unique
    private int apiLookupAccessesWithoutCleanup = 0;

    ServerLevelMixin() {
    }

    @Override
    public void fabric_registerCache(BlockPos pos, BlockApiCacheImpl<?, ?> cache) {
        List caches = this.apiLookupCaches.computeIfAbsent(pos.immutable(), ignored -> new ArrayList());
        caches.removeIf(weakReference -> weakReference.get() == null);
        caches.add(new WeakReference(cache));
        ++this.apiLookupAccessesWithoutCleanup;
    }

    @Override
    public void fabric_invalidateCache(BlockPos pos) {
        List<WeakReference<BlockApiCacheImpl<?, ?>>> caches = this.apiLookupCaches.get(pos);
        if (caches != null) {
            caches.removeIf(weakReference -> weakReference.get() == null);
            if (caches.size() == 0) {
                this.apiLookupCaches.remove(pos);
            } else {
                caches.forEach(weakReference -> {
                    BlockApiCacheImpl cache = (BlockApiCacheImpl)weakReference.get();
                    if (cache != null) {
                        cache.invalidate();
                    }
                });
            }
        }
        ++this.apiLookupAccessesWithoutCleanup;
        if (this.apiLookupAccessesWithoutCleanup > 2 * this.apiLookupCaches.size()) {
            this.apiLookupCaches.entrySet().removeIf(entry -> {
                ((List)entry.getValue()).removeIf(weakReference -> weakReference.get() == null);
                return ((List)entry.getValue()).isEmpty();
            });
            this.apiLookupAccessesWithoutCleanup = 0;
        }
    }
}

