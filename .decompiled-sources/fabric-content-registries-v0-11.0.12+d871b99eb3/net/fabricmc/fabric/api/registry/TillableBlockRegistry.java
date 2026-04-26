/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.registry;

import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.fabricmc.fabric.mixin.content.registry.HoeItemAccessor;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class TillableBlockRegistry {
    private TillableBlockRegistry() {
    }

    public static void register(Block input, Predicate<UseOnContext> usagePredicate, Consumer<UseOnContext> tillingAction) {
        Objects.requireNonNull(input, "input block cannot be null");
        HoeItemAccessor.getTillables().put(input, Pair.of(usagePredicate, tillingAction));
    }

    public static void register(Block input, Predicate<UseOnContext> usagePredicate, BlockState tilled) {
        Objects.requireNonNull(tilled, "tilled block state cannot be null");
        TillableBlockRegistry.register(input, usagePredicate, HoeItem.changeIntoState(tilled));
    }

    public static void register(Block input, Predicate<UseOnContext> usagePredicate, BlockState tilled, ItemLike droppedItem) {
        Objects.requireNonNull(tilled, "tilled block state cannot be null");
        Objects.requireNonNull(droppedItem, "dropped item cannot be null");
        TillableBlockRegistry.register(input, usagePredicate, HoeItem.changeIntoStateAndDropItem(tilled, droppedItem));
    }
}

