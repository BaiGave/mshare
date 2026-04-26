/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public record LootItemRandomChanceCondition(NumberProvider chance) implements LootItemCondition
{
    public static final MapCodec<LootItemRandomChanceCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)NumberProviders.CODEC.fieldOf("chance")).forGetter(LootItemRandomChanceCondition::chance)).apply((Applicative<LootItemRandomChanceCondition, ?>)i, LootItemRandomChanceCondition::new));

    public MapCodec<LootItemRandomChanceCondition> codec() {
        return MAP_CODEC;
    }

    @Override
    public boolean test(LootContext context) {
        float probability = this.chance.getFloat(context);
        return context.getRandom().nextFloat() < probability;
    }

    public static LootItemCondition.Builder randomChance(float probability) {
        return () -> new LootItemRandomChanceCondition(ConstantValue.exactly(probability));
    }

    public static LootItemCondition.Builder randomChance(NumberProvider probability) {
        return () -> new LootItemRandomChanceCondition(probability);
    }
}

