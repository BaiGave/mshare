/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.registry.sync.trackers.vanilla;

import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public final class BlockItemTracker
implements RegistryEntryAddedCallback<Item> {
    private BlockItemTracker() {
    }

    public static void register(Registry<Item> registry) {
        BlockItemTracker tracker = new BlockItemTracker();
        RegistryEntryAddedCallback.event(registry).register(tracker);
    }

    @Override
    public void onEntryAdded(int rawId, Identifier id, Item object) {
        if (object instanceof BlockItem) {
            ((BlockItem)object).registerBlocks(Item.BY_BLOCK, object);
        }
    }
}

