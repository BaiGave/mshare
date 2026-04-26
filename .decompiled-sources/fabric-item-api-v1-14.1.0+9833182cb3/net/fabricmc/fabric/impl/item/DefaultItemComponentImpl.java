/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.item;

import java.util.function.Predicate;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public class DefaultItemComponentImpl {
    public static final ScopedValue<HolderLookup.Provider> LOOKUP_PROVIDER_SCOPED_VALUE = ScopedValue.newInstance();

    public static void modifyItemComponents(HolderLookup.Provider registries) {
        DefaultItemComponentEvents.MODIFY.invoker().modify(new ModifyContextImpl(registries));
    }

    static class ModifyContextImpl
    implements DefaultItemComponentEvents.ModifyContext {
        private final HolderLookup.Provider registryLookup;

        private ModifyContextImpl(HolderLookup.Provider registries) {
            this.registryLookup = registries;
        }

        @Override
        public void modify(Predicate<Item> itemPredicate, DefaultItemComponentEvents.ModifyConsumer builderConsumer) {
            for (Item item : BuiltInRegistries.ITEM) {
                if (!itemPredicate.test(item)) continue;
                DataComponentMap.Builder builder = DataComponentMap.builder().addAll(item.components());
                builderConsumer.modify(builder, this.registryLookup, item);
                item.builtInRegistryHolder().bindComponents(builder.build());
            }
        }
    }
}

