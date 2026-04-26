/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.loot.v3;

import java.util.List;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jspecify.annotations.Nullable;

public final class LootTableEvents {
    public static final Event<Replace> REPLACE = EventFactory.createArrayBacked(Replace.class, listeners -> (key, original, source, holder) -> {
        for (Replace listener : listeners) {
            @Nullable LootTable replaced = listener.replaceLootTable(key, original, source, holder);
            if (replaced == null) continue;
            return replaced;
        }
        return null;
    });
    public static final Event<Modify> MODIFY = EventFactory.createArrayBacked(Modify.class, listeners -> (key, tableBuilder, source, holder) -> {
        for (Modify listener : listeners) {
            listener.modifyLootTable(key, tableBuilder, source, holder);
        }
    });
    public static final Event<Loaded> ALL_LOADED = EventFactory.createArrayBacked(Loaded.class, listeners -> (resourceManager, lootManager) -> {
        for (Loaded listener : listeners) {
            listener.onLootTablesLoaded(resourceManager, lootManager);
        }
    });
    public static final Event<ModifyDrops> MODIFY_DROPS = EventFactory.createArrayBacked(ModifyDrops.class, listeners -> (holder, context, drops) -> {
        for (ModifyDrops listener : listeners) {
            listener.modifyLootTableDrops(holder, context, drops);
        }
    });

    private LootTableEvents() {
    }

    @FunctionalInterface
    public static interface ModifyDrops {
        public void modifyLootTableDrops(Holder<LootTable> var1, LootContext var2, List<ItemStack> var3);
    }

    @FunctionalInterface
    public static interface Loaded {
        public void onLootTablesLoaded(ResourceManager var1, Registry<LootTable> var2);
    }

    @FunctionalInterface
    public static interface Modify {
        public void modifyLootTable(ResourceKey<LootTable> var1, LootTable.Builder var2, LootTableSource var3, HolderLookup.Provider var4);
    }

    @FunctionalInterface
    public static interface Replace {
        public @Nullable LootTable replaceLootTable(ResourceKey<LootTable> var1, LootTable var2, LootTableSource var3, HolderLookup.Provider var4);
    }
}

