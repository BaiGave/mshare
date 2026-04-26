/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.transfer.fluid;

import java.util.ArrayList;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class CombinedProvidersImpl {
    public static Event<FluidStorage.CombinedItemApiProvider> createEvent(boolean invokeFallback) {
        return EventFactory.createArrayBacked(FluidStorage.CombinedItemApiProvider.class, listeners -> context -> {
            Storage<FluidVariant> fallbackFound;
            ArrayList<Storage<FluidVariant>> storages = new ArrayList<Storage<FluidVariant>>();
            for (FluidStorage.CombinedItemApiProvider listener : listeners) {
                Storage<FluidVariant> found = listener.find(context);
                if (found == null) continue;
                storages.add(found);
            }
            if (!storages.isEmpty() && invokeFallback && (fallbackFound = FluidStorage.GENERAL_COMBINED_PROVIDER.invoker().find(context)) != null) {
                storages.add(fallbackFound);
            }
            return storages.isEmpty() ? null : new CombinedStorage(storages);
        });
    }

    public static Event<FluidStorage.CombinedItemApiProvider> getOrCreateItemEvent(Item item) {
        ItemApiLookup.ItemApiProvider<Storage<FluidVariant>, ContainerItemContext> existingProvider = FluidStorage.ITEM.getProvider(item);
        if (existingProvider == null) {
            FluidStorage.ITEM.registerForItems(new Provider(), item);
            existingProvider = FluidStorage.ITEM.getProvider(item);
        }
        if (existingProvider instanceof Provider) {
            Provider registeredProvider = (Provider)existingProvider;
            return registeredProvider.event;
        }
        String errorMessage = String.format("An incompatible provider was already registered for item %s. Provider: %s.", item, existingProvider);
        throw new IllegalStateException(errorMessage);
    }

    private static class Provider
    implements ItemApiLookup.ItemApiProvider<Storage<FluidVariant>, ContainerItemContext> {
        private final Event<FluidStorage.CombinedItemApiProvider> event = CombinedProvidersImpl.createEvent(true);

        private Provider() {
        }

        @Override
        public @Nullable Storage<FluidVariant> find(ItemStack itemStack, ContainerItemContext context) {
            if (!context.getItemVariant().matches(itemStack)) {
                String errorMessage = String.format("Query stack %s and ContainerItemContext variant %s don't match.", itemStack, context.getItemVariant());
                throw new IllegalArgumentException(errorMessage);
            }
            return this.event.invoker().find(context);
        }
    }
}

