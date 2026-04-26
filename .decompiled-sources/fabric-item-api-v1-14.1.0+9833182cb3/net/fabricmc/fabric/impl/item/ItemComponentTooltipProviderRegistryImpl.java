/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.fabricmc.fabric.impl.item.VanillaTooltipProviderOrder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;

public final class ItemComponentTooltipProviderRegistryImpl {
    private static final List<DataComponentType<? extends TooltipProvider>> first = new ArrayList<DataComponentType<? extends TooltipProvider>>();
    private static final List<DataComponentType<? extends TooltipProvider>> last = new ArrayList<DataComponentType<? extends TooltipProvider>>();
    private static final Map<DataComponentType<?>, List<DataComponentType<? extends TooltipProvider>>> before = new IdentityHashMap();
    private static final Map<DataComponentType<?>, List<DataComponentType<? extends TooltipProvider>>> after = new IdentityHashMap();
    private static boolean hasModdedEntries = false;

    public static void addFirst(DataComponentType<? extends TooltipProvider> componentType) {
        first.add(componentType);
        ItemComponentTooltipProviderRegistryImpl.onModified();
    }

    public static void addLast(DataComponentType<? extends TooltipProvider> componentType) {
        last.add(componentType);
        ItemComponentTooltipProviderRegistryImpl.onModified();
    }

    public static void addBefore(DataComponentType<?> anchor, DataComponentType<? extends TooltipProvider> componentType) {
        before.computeIfAbsent(anchor, k -> new ArrayList()).add(componentType);
        ItemComponentTooltipProviderRegistryImpl.onModified();
    }

    public static void addAfter(DataComponentType<?> anchor, DataComponentType<? extends TooltipProvider> componentType) {
        after.computeIfAbsent(anchor, k -> new ArrayList()).add(componentType);
        ItemComponentTooltipProviderRegistryImpl.onModified();
    }

    private static void onModified() {
        hasModdedEntries = true;
        VanillaTooltipProviderOrder.load();
    }

    public static boolean hasModdedEntries() {
        return hasModdedEntries;
    }

    public static void onFirst(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> componentConsumer, TooltipFlag flag) {
        HashSet cycleDetector = new HashSet();
        for (DataComponentType<? extends TooltipProvider> componentType : first) {
            ItemComponentTooltipProviderRegistryImpl.appendCustomComponentTooltip(stack, componentType, context, displayComponent, componentConsumer, flag, cycleDetector);
        }
    }

    public static void onLast(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> componentConsumer, TooltipFlag flag) {
        HashSet cycleDetector = new HashSet();
        for (DataComponentType<? extends TooltipProvider> componentType : last) {
            ItemComponentTooltipProviderRegistryImpl.appendCustomComponentTooltip(stack, componentType, context, displayComponent, componentConsumer, flag, cycleDetector);
        }
    }

    public static void onBefore(ItemStack stack, DataComponentType<?> componentType, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> componentConsumer, TooltipFlag flag, Set<DataComponentType<?>> cycleDetector) {
        List<DataComponentType<? extends TooltipProvider>> befores = before.get(componentType);
        if (befores != null) {
            for (DataComponentType<? extends TooltipProvider> beforeComponentType : befores) {
                ItemComponentTooltipProviderRegistryImpl.appendCustomComponentTooltip(stack, beforeComponentType, context, displayComponent, componentConsumer, flag, cycleDetector);
            }
        }
    }

    public static void onAfter(ItemStack stack, DataComponentType<?> componentType, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> componentConsumer, TooltipFlag flag, Set<DataComponentType<?>> cycleDetector) {
        List<DataComponentType<? extends TooltipProvider>> afters = after.get(componentType);
        if (afters != null) {
            for (DataComponentType<? extends TooltipProvider> afterComponentType : afters) {
                ItemComponentTooltipProviderRegistryImpl.appendCustomComponentTooltip(stack, afterComponentType, context, displayComponent, componentConsumer, flag, cycleDetector);
            }
        }
    }

    private static void appendCustomComponentTooltip(ItemStack stack, DataComponentType<? extends TooltipProvider> componentType, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> componentConsumer, TooltipFlag flag, Set<DataComponentType<?>> cycleDetector) {
        if (!cycleDetector.add(componentType)) {
            return;
        }
        ItemComponentTooltipProviderRegistryImpl.onBefore(stack, componentType, context, displayComponent, componentConsumer, flag, cycleDetector);
        stack.addToTooltip(componentType, context, displayComponent, componentConsumer, flag);
        ItemComponentTooltipProviderRegistryImpl.onAfter(stack, componentType, context, displayComponent, componentConsumer, flag, cycleDetector);
        cycleDetector.remove(componentType);
    }
}

