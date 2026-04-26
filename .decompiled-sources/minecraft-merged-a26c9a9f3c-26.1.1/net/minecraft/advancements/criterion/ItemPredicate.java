/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.ItemLike;

public record ItemPredicate(Optional<HolderSet<Item>> items, MinMaxBounds.Ints count, DataComponentMatchers components) implements Predicate<ItemInstance>
{
    public static final Codec<ItemPredicate> CODEC = RecordCodecBuilder.create(i -> i.group(RegistryCodecs.homogeneousList(Registries.ITEM).optionalFieldOf("items").forGetter(ItemPredicate::items), MinMaxBounds.Ints.CODEC.optionalFieldOf("count", MinMaxBounds.Ints.ANY).forGetter(ItemPredicate::count), DataComponentMatchers.CODEC.forGetter(ItemPredicate::components)).apply((Applicative<ItemPredicate, ?>)i, ItemPredicate::new));

    @Override
    public boolean test(ItemInstance itemStack) {
        if (this.items.isPresent() && !itemStack.is(this.items.get())) {
            return false;
        }
        if (!this.count.matches(itemStack.count())) {
            return false;
        }
        return this.components.test(itemStack);
    }

    public static class Builder {
        private Optional<HolderSet<Item>> items = Optional.empty();
        private MinMaxBounds.Ints count = MinMaxBounds.Ints.ANY;
        private DataComponentMatchers components = DataComponentMatchers.ANY;

        public static Builder item() {
            return new Builder();
        }

        public Builder of(HolderGetter<Item> lookup, ItemLike ... items) {
            this.items = Optional.of(HolderSet.direct(i -> i.asItem().builtInRegistryHolder(), items));
            return this;
        }

        public Builder of(HolderGetter<Item> lookup, TagKey<Item> tag) {
            this.items = Optional.of(lookup.getOrThrow(tag));
            return this;
        }

        public Builder withCount(MinMaxBounds.Ints count) {
            this.count = count;
            return this;
        }

        public Builder withComponents(DataComponentMatchers components) {
            this.components = components;
            return this;
        }

        public ItemPredicate build() {
            return new ItemPredicate(this.items, this.count, this.components);
        }
    }
}

