/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.datagen.v1.provider;

import com.google.common.base.Preconditions;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.minecraft.data.DataProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface FabricLootTableSubProvider
extends LootTableSubProvider,
DataProvider {
    default public BiConsumer<ResourceKey<LootTable>, LootTable.Builder> withConditions(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> exporter, ResourceCondition ... conditions) {
        Preconditions.checkArgument(conditions.length > 0, "Must add at least one condition.");
        return (id, table) -> {
            FabricDataGenHelper.addConditions(table, conditions);
            exporter.accept((ResourceKey<LootTable>)id, (LootTable.Builder)table);
        };
    }
}

