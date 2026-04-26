/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public record ConstantValue(float value) implements NumberProvider
{
    public static final MapCodec<ConstantValue> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Codec.FLOAT.fieldOf("value")).forGetter(ConstantValue::value)).apply((Applicative<ConstantValue, ?>)i, ConstantValue::new));
    public static final Codec<ConstantValue> INLINE_CODEC = Codec.FLOAT.xmap(ConstantValue::new, ConstantValue::value);

    public MapCodec<ConstantValue> codec() {
        return MAP_CODEC;
    }

    @Override
    public float getFloat(LootContext random) {
        return this.value;
    }

    public static ConstantValue exactly(float value) {
        return new ConstantValue(value);
    }
}

