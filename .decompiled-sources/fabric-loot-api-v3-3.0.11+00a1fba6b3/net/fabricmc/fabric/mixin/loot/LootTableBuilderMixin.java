/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.loot;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ListIterator;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.loot.v3.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v3.FabricLootTableBuilder;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={LootTable.Builder.class})
abstract class LootTableBuilderMixin
implements FabricLootTableBuilder {
    @Shadow
    @Final
    @Mutable
    private ImmutableList.Builder<LootPool> pools;
    @Shadow
    @Final
    private ImmutableList.Builder<LootItemFunction> functions;

    LootTableBuilderMixin() {
    }

    @Unique
    private LootTable.Builder self() {
        return (LootTable.Builder)((Object)this);
    }

    @Override
    public LootTable.Builder pool(LootPool pool) {
        this.pools.add((Object)pool);
        return this.self();
    }

    @Override
    public LootTable.Builder apply(LootItemFunction function) {
        this.functions.add((Object)function);
        return this.self();
    }

    @Override
    public LootTable.Builder pools(Collection<? extends LootPool> pools) {
        this.pools.addAll(pools);
        return this.self();
    }

    @Override
    public LootTable.Builder apply(Collection<? extends LootItemFunction> functions) {
        this.functions.addAll(functions);
        return this.self();
    }

    @Override
    public LootTable.Builder modifyPools(Consumer<? super LootPool.Builder> modifier) {
        ArrayList list = new ArrayList(this.pools.build());
        ListIterator<LootPool> iterator = list.listIterator();
        while (iterator.hasNext()) {
            LootPool.Builder poolBuilder = FabricLootPoolBuilder.copyOf((LootPool)iterator.next());
            modifier.accept(poolBuilder);
            iterator.set(poolBuilder.build());
        }
        this.pools = ImmutableList.builder();
        this.pools.addAll((Iterable)list);
        return this.self();
    }
}

