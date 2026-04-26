/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.lookup.item;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import net.fabricmc.fabric.api.lookup.v1.custom.ApiLookupMap;
import net.fabricmc.fabric.api.lookup.v1.custom.ApiProviderMap;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemApiLookupImpl<A, C>
implements ItemApiLookup<A, C> {
    private static final Logger LOGGER = LoggerFactory.getLogger("fabric-api-lookup-api-v1/item");
    private static final ApiLookupMap<ItemApiLookup<?, ?>> LOOKUPS = ApiLookupMap.create(ItemApiLookupImpl::new);
    private final Identifier identifier;
    private final Class<A> apiClass;
    private final Class<C> contextClass;
    private final ApiProviderMap<Item, ItemApiLookup.ItemApiProvider<A, C>> providerMap = ApiProviderMap.create();
    private final List<ItemApiLookup.ItemApiProvider<A, C>> fallbackProviders = new CopyOnWriteArrayList<ItemApiLookup.ItemApiProvider<A, C>>();

    public static <A, C> ItemApiLookup<A, C> get(Identifier lookupId, Class<A> apiClass, Class<C> contextClass) {
        return LOOKUPS.getLookup(lookupId, apiClass, contextClass);
    }

    private ItemApiLookupImpl(Identifier identifier, Class<?> apiClass, Class<?> contextClass) {
        this.identifier = identifier;
        this.apiClass = apiClass;
        this.contextClass = contextClass;
    }

    @Override
    public @Nullable A find(ItemStack itemStack, C context) {
        A instance;
        Objects.requireNonNull(itemStack, "ItemStack may not be null.");
        @Nullable ItemApiLookup.ItemApiProvider<A, C> provider = this.providerMap.get(itemStack.getItem());
        if (provider != null && (instance = provider.find(itemStack, context)) != null) {
            return instance;
        }
        for (ItemApiLookup.ItemApiProvider<A, C> fallbackProvider : this.fallbackProviders) {
            A instance2 = fallbackProvider.find(itemStack, context);
            if (instance2 == null) continue;
            return instance2;
        }
        return null;
    }

    @Override
    public void registerSelf(ItemLike ... items) {
        for (ItemLike itemLike : items) {
            Item item = itemLike.asItem();
            if (this.apiClass.isAssignableFrom(item.getClass())) continue;
            String errorMessage = String.format("Failed to register self-implementing items. API class %s is not assignable from item class %s.", this.apiClass.getCanonicalName(), item.getClass().getCanonicalName());
            throw new IllegalArgumentException(errorMessage);
        }
        this.registerForItems((itemStack, context) -> itemStack.getItem(), items);
    }

    @Override
    public void registerForItems(ItemApiLookup.ItemApiProvider<A, C> provider, ItemLike ... items) {
        Objects.requireNonNull(provider, "ItemApiProvider may not be null.");
        if (items.length == 0) {
            throw new IllegalArgumentException("Must register at least one ItemLike instance with an ItemApiProvider.");
        }
        for (ItemLike itemLike : items) {
            Item item = itemLike.asItem();
            Objects.requireNonNull(item, "ItemLike in item form may not be null.");
            if (this.providerMap.putIfAbsent(item, provider) == null) continue;
            LOGGER.warn("Encountered duplicate API provider registration for item: " + String.valueOf(BuiltInRegistries.ITEM.getKey(item)));
        }
    }

    @Override
    public void registerFallback(ItemApiLookup.ItemApiProvider<A, C> fallbackProvider) {
        Objects.requireNonNull(fallbackProvider, "ItemApiProvider may not be null.");
        this.fallbackProviders.add(fallbackProvider);
    }

    @Override
    public Identifier getId() {
        return this.identifier;
    }

    @Override
    public Class<A> apiClass() {
        return this.apiClass;
    }

    @Override
    public Class<C> contextClass() {
        return this.contextClass;
    }

    @Override
    public @Nullable ItemApiLookup.ItemApiProvider<A, C> getProvider(Item item) {
        return this.providerMap.get(item);
    }
}

