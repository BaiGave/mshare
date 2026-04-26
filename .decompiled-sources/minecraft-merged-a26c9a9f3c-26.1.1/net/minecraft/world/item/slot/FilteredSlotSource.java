/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.slot;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.world.item.slot.SlotCollection;
import net.minecraft.world.item.slot.SlotSource;
import net.minecraft.world.item.slot.TransformedSlotSource;

public class FilteredSlotSource
extends TransformedSlotSource {
    public static final MapCodec<FilteredSlotSource> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> FilteredSlotSource.commonFields(i).and(((MapCodec)ItemPredicate.CODEC.fieldOf("item_filter")).forGetter(t -> t.filter)).apply((Applicative<FilteredSlotSource, ?>)i, FilteredSlotSource::new));
    private final ItemPredicate filter;

    private FilteredSlotSource(SlotSource slotSource, ItemPredicate filter) {
        super(slotSource);
        this.filter = filter;
    }

    public MapCodec<FilteredSlotSource> codec() {
        return MAP_CODEC;
    }

    @Override
    protected SlotCollection transform(SlotCollection slots) {
        return slots.filter(this.filter);
    }
}

