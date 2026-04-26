/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.item.v1;

import com.google.common.base.Preconditions;
import net.fabricmc.fabric.impl.item.ItemComponentTooltipProviderRegistryImpl;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.component.TooltipProvider;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface ItemComponentTooltipProviderRegistry {
    public static void addFirst(DataComponentType<? extends TooltipProvider> componentType) {
        Preconditions.checkNotNull(componentType, "componentType");
        ItemComponentTooltipProviderRegistryImpl.addFirst(componentType);
    }

    public static void addLast(DataComponentType<? extends TooltipProvider> componentType) {
        Preconditions.checkNotNull(componentType, "componentType");
        ItemComponentTooltipProviderRegistryImpl.addLast(componentType);
    }

    public static void addBefore(DataComponentType<?> anchor, DataComponentType<? extends TooltipProvider> componentType) {
        Preconditions.checkNotNull(anchor, "anchor");
        Preconditions.checkNotNull(componentType, "componentType");
        ItemComponentTooltipProviderRegistryImpl.addBefore(anchor, componentType);
    }

    public static void addAfter(DataComponentType<?> anchor, DataComponentType<? extends TooltipProvider> componentType) {
        Preconditions.checkNotNull(anchor, "anchor");
        Preconditions.checkNotNull(componentType, "componentType");
        ItemComponentTooltipProviderRegistryImpl.addAfter(anchor, componentType);
    }
}

