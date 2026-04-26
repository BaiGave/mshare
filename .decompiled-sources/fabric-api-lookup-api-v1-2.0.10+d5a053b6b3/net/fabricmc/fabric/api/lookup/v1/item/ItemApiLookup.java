/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.lookup.v1.item;

import net.fabricmc.fabric.impl.lookup.item.ItemApiLookupImpl;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.NonExtendable
public interface ItemApiLookup<A, C> {
    public static <A, C> ItemApiLookup<A, C> get(Identifier lookupId, Class<A> apiClass, Class<C> contextClass) {
        return ItemApiLookupImpl.get(lookupId, apiClass, contextClass);
    }

    public @Nullable A find(ItemStack var1, C var2);

    public void registerSelf(ItemLike ... var1);

    public void registerForItems(ItemApiProvider<A, C> var1, ItemLike ... var2);

    public void registerFallback(ItemApiProvider<A, C> var1);

    public Identifier getId();

    public Class<A> apiClass();

    public Class<C> contextClass();

    public @Nullable ItemApiProvider<A, C> getProvider(Item var1);

    @FunctionalInterface
    public static interface ItemApiProvider<A, C> {
        public @Nullable A find(ItemStack var1, C var2);
    }
}

