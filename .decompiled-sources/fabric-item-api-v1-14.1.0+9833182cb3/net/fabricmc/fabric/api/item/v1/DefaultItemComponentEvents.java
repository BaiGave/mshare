/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.item.v1;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;

public final class DefaultItemComponentEvents {
    public static final Event<ModifyCallback> MODIFY = EventFactory.createArrayBacked(ModifyCallback.class, listeners -> context -> {
        for (ModifyCallback listener : listeners) {
            listener.modify(context);
        }
    });

    private DefaultItemComponentEvents() {
    }

    @FunctionalInterface
    public static interface ModifyCallback {
        public void modify(ModifyContext var1);
    }

    public static interface ModifyContext {
        public void modify(Predicate<Item> var1, ModifyConsumer var2);

        default public void modify(Item item, ModifyConsumer builderConsumer) {
            this.modify(Predicate.isEqual(item), builderConsumer);
        }

        default public void modify(Collection<Item> items, ModifyConsumer builderConsumer) {
            this.modify(items::contains, builderConsumer);
        }

        default public void modify(Predicate<Item> itemPredicate, BiConsumer<DataComponentMap.Builder, Item> builderConsumer) {
            this.modify(itemPredicate, (DataComponentMap.Builder builder, HolderLookup.Provider _lookupProvider, Item item) -> builderConsumer.accept(builder, item));
        }

        default public void modify(Item item, Consumer<DataComponentMap.Builder> builderConsumer) {
            this.modify(Predicate.isEqual(item), (DataComponentMap.Builder builder, Item _item) -> builderConsumer.accept((DataComponentMap.Builder)builder));
        }

        default public void modify(Collection<Item> items, BiConsumer<DataComponentMap.Builder, Item> builderConsumer) {
            this.modify(items::contains, builderConsumer);
        }
    }

    @FunctionalInterface
    public static interface ModifyConsumer {
        public void modify(DataComponentMap.Builder var1, HolderLookup.Provider var2, Item var3);
    }
}

