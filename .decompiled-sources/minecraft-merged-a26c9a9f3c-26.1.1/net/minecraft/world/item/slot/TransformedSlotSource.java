/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.slot;

import com.mojang.datafixers.Products;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.slot.SlotCollection;
import net.minecraft.world.item.slot.SlotSource;
import net.minecraft.world.item.slot.SlotSources;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;

public abstract class TransformedSlotSource
implements SlotSource {
    protected final SlotSource slotSource;

    protected TransformedSlotSource(SlotSource slotSource) {
        this.slotSource = slotSource;
    }

    public abstract MapCodec<? extends TransformedSlotSource> codec();

    protected static <T extends TransformedSlotSource> Products.P1<RecordCodecBuilder.Mu<T>, SlotSource> commonFields(RecordCodecBuilder.Instance<T> i) {
        return i.group(((MapCodec)SlotSources.CODEC.fieldOf("slot_source")).forGetter(t -> t.slotSource));
    }

    protected abstract SlotCollection transform(SlotCollection var1);

    @Override
    public final SlotCollection provide(LootContext context) {
        return this.transform(this.slotSource.provide(context));
    }

    @Override
    public void validate(ValidationContext context) {
        SlotSource.super.validate(context);
        Validatable.validate(context, "slot_source", this.slotSource);
    }
}

