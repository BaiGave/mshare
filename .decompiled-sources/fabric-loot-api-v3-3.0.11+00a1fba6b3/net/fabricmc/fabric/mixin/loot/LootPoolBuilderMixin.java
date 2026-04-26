/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.loot;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import net.fabricmc.fabric.api.loot.v3.FabricLootPoolBuilder;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={LootPool.Builder.class})
abstract class LootPoolBuilderMixin
implements FabricLootPoolBuilder {
    @Shadow
    @Final
    private ImmutableList.Builder<LootPoolEntryContainer> entries;
    @Shadow
    @Final
    private ImmutableList.Builder<LootItemCondition> conditions;
    @Shadow
    @Final
    private ImmutableList.Builder<LootItemFunction> functions;

    LootPoolBuilderMixin() {
    }

    @Unique
    private LootPool.Builder self() {
        return (LootPool.Builder)((Object)this);
    }

    @Override
    public LootPool.Builder add(LootPoolEntryContainer entry) {
        this.entries.add((Object)entry);
        return this.self();
    }

    @Override
    public LootPool.Builder add(Collection<? extends LootPoolEntryContainer> entries) {
        this.entries.addAll(entries);
        return this.self();
    }

    @Override
    public LootPool.Builder when(LootItemCondition condition) {
        this.conditions.add((Object)condition);
        return this.self();
    }

    @Override
    public LootPool.Builder when(Collection<? extends LootItemCondition> conditions) {
        this.conditions.addAll(conditions);
        return this.self();
    }

    @Override
    public LootPool.Builder apply(LootItemFunction function) {
        this.functions.add((Object)function);
        return this.self();
    }

    @Override
    public LootPool.Builder apply(Collection<? extends LootItemFunction> functions) {
        this.functions.addAll(functions);
        return this.self();
    }
}

