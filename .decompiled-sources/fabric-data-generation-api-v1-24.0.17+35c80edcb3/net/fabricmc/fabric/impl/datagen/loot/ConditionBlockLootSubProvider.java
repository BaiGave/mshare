/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.datagen.loot;

import java.util.Collections;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.fabricmc.fabric.mixin.datagen.loot.BlockLootSubProviderAccessor;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

public class ConditionBlockLootSubProvider
extends BlockLootSubProvider {
    private final BlockLootSubProvider parent;
    private final ResourceCondition[] conditions;

    public ConditionBlockLootSubProvider(BlockLootSubProvider parent, ResourceCondition[] conditions) {
        super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags(), ((BlockLootSubProviderAccessor)((Object)parent)).getRegistries());
        this.parent = parent;
        this.conditions = conditions;
    }

    @Override
    public void generate() {
        throw new UnsupportedOperationException("generate() should not be called.");
    }

    @Override
    public void add(Block block, LootTable.Builder lootTable) {
        FabricDataGenHelper.addConditions(lootTable, this.conditions);
        this.parent.add(block, lootTable);
    }
}

