/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.registry;

import net.fabricmc.fabric.api.util.Block2ObjectMap;
import net.fabricmc.fabric.impl.content.registry.FlammableBlockRegistryImpl;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public interface FlammableBlockRegistry
extends Block2ObjectMap<Entry> {
    public static FlammableBlockRegistry getDefaultInstance() {
        return FlammableBlockRegistry.getInstance(Blocks.FIRE);
    }

    public static FlammableBlockRegistry getInstance(Block block) {
        return FlammableBlockRegistryImpl.getInstance(block);
    }

    default public void add(Block block, int igniteOdds, int burnOdds) {
        this.add(block, new Entry(igniteOdds, burnOdds));
    }

    default public void add(TagKey<Block> tag, int igniteOdds, int burnOdds) {
        this.add(tag, new Entry(igniteOdds, burnOdds));
    }

    public static final class Entry {
        private final int igniteOdds;
        private final int burnOdds;

        public Entry(int igniteOdds, int burnOdds) {
            this.igniteOdds = igniteOdds;
            this.burnOdds = burnOdds;
        }

        public int getIgniteOdds() {
            return this.igniteOdds;
        }

        public int getBurnOdds() {
            return this.burnOdds;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry other = (Entry)o;
            return other.igniteOdds == this.igniteOdds && other.burnOdds == this.burnOdds;
        }

        public int hashCode() {
            return this.igniteOdds * 11 + this.burnOdds;
        }
    }
}

