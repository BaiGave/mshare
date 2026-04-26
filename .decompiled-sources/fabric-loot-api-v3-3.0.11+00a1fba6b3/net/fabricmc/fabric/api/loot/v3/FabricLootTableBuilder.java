/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.loot.v3;

import java.util.Collection;
import java.util.function.Consumer;
import net.fabricmc.fabric.mixin.loot.LootTableAccessor;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface FabricLootTableBuilder {
    default public LootTable.Builder pool(LootPool pool) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default public LootTable.Builder apply(LootItemFunction function) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default public LootTable.Builder pools(Collection<? extends LootPool> pools) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default public LootTable.Builder apply(Collection<? extends LootItemFunction> functions) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default public LootTable.Builder modifyPools(Consumer<? super LootPool.Builder> modifier) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    public static LootTable.Builder copyOf(LootTable table) {
        LootTable.Builder builder = LootTable.lootTable();
        LootTableAccessor accessor = (LootTableAccessor)((Object)table);
        builder.setParamSet(table.getParamSet());
        builder.pools(accessor.fabric_getPools());
        builder.apply(accessor.fabric_getFunctions());
        accessor.fabric_getRandomSequence().ifPresent(builder::setRandomSequence);
        return builder;
    }
}

