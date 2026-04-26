/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.loot.v3;

import java.util.Collection;
import net.fabricmc.fabric.mixin.loot.LootPoolAccessor;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface FabricLootPoolBuilder {
    default public LootPool.Builder add(LootPoolEntryContainer entry) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default public LootPool.Builder add(Collection<? extends LootPoolEntryContainer> entries) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default public LootPool.Builder when(LootItemCondition condition) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default public LootPool.Builder when(Collection<? extends LootItemCondition> conditions) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default public LootPool.Builder apply(LootItemFunction function) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default public LootPool.Builder apply(Collection<? extends LootItemFunction> functions) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    public static LootPool.Builder copyOf(LootPool pool) {
        LootPoolAccessor accessor = (LootPoolAccessor)((Object)pool);
        return LootPool.lootPool().setRolls(accessor.fabric_getRolls()).setBonusRolls(accessor.fabric_getBonusRolls()).add(accessor.fabric_getEntries()).when(accessor.fabric_getConditions()).apply(accessor.fabric_getFunctions());
    }
}

