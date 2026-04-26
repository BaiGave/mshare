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

public record MultiplyValue(LevelBasedValue factor) implements EnchantmentValueEffect
{
    public static final MapCodec<MultiplyValue> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)LevelBasedValue.CODEC.fieldOf("factor")).forGetter(MultiplyValue::factor)).apply((Applicative<MultiplyValue, ?>)i, MultiplyValue::new));

    @Override
    public float process(int enchantmentLevel, RandomSource random, float inputValue) {
        return inputValue * this.factor.calculate(enchantmentLevel);
    }

    public MapCodec<MultiplyValue> codec() {
        return CODEC;
    }
}

