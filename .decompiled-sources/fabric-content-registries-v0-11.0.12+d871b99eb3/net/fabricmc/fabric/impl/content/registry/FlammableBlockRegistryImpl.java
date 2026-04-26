/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.content.registry;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.impl.content.registry.FireBlockHooks;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class FlammableBlockRegistryImpl
implements FlammableBlockRegistry {
    private static final FlammableBlockRegistry.Entry REMOVED = new FlammableBlockRegistry.Entry(0, 0);
    private static final Map<Block, FlammableBlockRegistryImpl> REGISTRIES = new HashMap<Block, FlammableBlockRegistryImpl>();
    private final Map<Block, FlammableBlockRegistry.Entry> registeredEntriesBlock = new HashMap<Block, FlammableBlockRegistry.Entry>();
    private final Map<TagKey<Block>, FlammableBlockRegistry.Entry> registeredEntriesTag = new HashMap<TagKey<Block>, FlammableBlockRegistry.Entry>();
    private volatile Map<Block, FlammableBlockRegistry.Entry> computedEntries = null;
    private final Block key;

    private FlammableBlockRegistryImpl(Block key) {
        this.key = key;
        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            this.computedEntries = null;
        });
    }

    private Map<Block, FlammableBlockRegistry.Entry> getEntryMap() {
        Map<Block, FlammableBlockRegistry.Entry> ret = this.computedEntries;
        if (ret == null) {
            ret = new IdentityHashMap<Block, FlammableBlockRegistry.Entry>();
            for (TagKey<Block> tag : this.registeredEntriesTag.keySet()) {
                FlammableBlockRegistry.Entry entry = this.registeredEntriesTag.get(tag);
                for (Holder<Block> block : BuiltInRegistries.BLOCK.getTagOrEmpty(tag)) {
                    ret.put(block.value(), entry);
                }
            }
            ret.putAll(this.registeredEntriesBlock);
            this.computedEntries = ret;
        }
        return ret;
    }

    @Override
    public FlammableBlockRegistry.Entry get(Block block) {
        FlammableBlockRegistry.Entry entry = this.getEntryMap().get(block);
        if (entry != null) {
            return entry;
        }
        return ((FireBlockHooks)((Object)this.key)).fabric_getVanillaEntry(block.defaultBlockState());
    }

    public FlammableBlockRegistry.Entry getFabric(Block block) {
        return this.getEntryMap().get(block);
    }

    @Override
    public void add(Block block, FlammableBlockRegistry.Entry value) {
        this.registeredEntriesBlock.put(block, value);
        this.computedEntries = null;
    }

    @Override
    public void add(TagKey<Block> tag, FlammableBlockRegistry.Entry value) {
        this.registeredEntriesTag.put(tag, value);
        this.computedEntries = null;
    }

    @Override
    public void remove(Block block) {
        this.add(block, REMOVED);
    }

    @Override
    public void remove(TagKey<Block> tag) {
        this.add(tag, REMOVED);
    }

    @Override
    public void clear(Block block) {
        this.registeredEntriesBlock.remove(block);
        this.computedEntries = null;
    }

    @Override
    public void clear(TagKey<Block> tag) {
        this.registeredEntriesTag.remove(tag);
        this.computedEntries = null;
    }

    public static FlammableBlockRegistryImpl getInstance(Block block) {
        if (!(block instanceof FireBlockHooks)) {
            throw new RuntimeException("Not a hookable fire block: " + String.valueOf(block));
        }
        return REGISTRIES.computeIfAbsent(block, FlammableBlockRegistryImpl::new);
    }
}

