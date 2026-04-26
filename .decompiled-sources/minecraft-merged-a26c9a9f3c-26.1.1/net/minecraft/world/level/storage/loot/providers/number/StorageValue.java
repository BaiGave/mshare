/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public record StorageValue(Identifier storage, NbtPathArgument.NbtPath path) implements NumberProvider
{
    public static final MapCodec<StorageValue> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Identifier.CODEC.fieldOf("storage")).forGetter(StorageValue::storage), ((MapCodec)NbtPathArgument.NbtPath.CODEC.fieldOf("path")).forGetter(StorageValue::path)).apply((Applicative<StorageValue, ?>)i, StorageValue::new));

    public MapCodec<StorageValue> codec() {
        return MAP_CODEC;
    }

    private Number getNumericTag(LootContext context, Number _default) {
        CompoundTag value = context.getLevel().getServer().getCommandStorage().get(this.storage);
        try {
            Tag tag;
            List<Tag> selectedTags = this.path.get(value);
            if (selectedTags.size() == 1 && (tag = selectedTags.getFirst()) instanceof NumericTag) {
                NumericTag result = (NumericTag)tag;
                return result.box();
            }
        }
        catch (CommandSyntaxException commandSyntaxException) {
            // empty catch block
        }
        return _default;
    }

    @Override
    public float getFloat(LootContext context) {
        return this.getNumericTag(context, Float.valueOf(0.0f)).floatValue();
    }

    @Override
    public int getInt(LootContext context) {
        return this.getNumericTag(context, 0).intValue();
    }
}

