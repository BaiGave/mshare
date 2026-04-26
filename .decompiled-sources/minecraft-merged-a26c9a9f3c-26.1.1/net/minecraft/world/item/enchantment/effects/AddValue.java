/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.enchantment.effects;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;

public record AddValue(LevelBasedValue value) implements EnchantmentValueEffect
{
    public static final MapCodec<AddValue> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)LevelBasedValue.CODEC.fieldOf("value")).forGetter(AddValue::value)).apply((Applicative<AddValue, ?>)i, AddValue::new));

    @Override
    public float process(int enchantmentLevel, RandomSource random, float inputValue) {
        return inputValue + this.value.calculate(enchantmentLevel);
    }

    public MapCodec<AddValue> codec() {
        return CODEC;
    }
}

