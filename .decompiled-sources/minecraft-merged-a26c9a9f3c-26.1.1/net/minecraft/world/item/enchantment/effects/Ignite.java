/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.enchantment.effects;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

public record Ignite(LevelBasedValue duration) implements EnchantmentEntityEffect
{
    public static final MapCodec<Ignite> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)LevelBasedValue.CODEC.fieldOf("duration")).forGetter(e -> e.duration)).apply((Applicative<Ignite, ?>)i, Ignite::new));

    @Override
    public void apply(ServerLevel serverLevel, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 position) {
        entity.igniteForSeconds(this.duration.calculate(enchantmentLevel));
    }

    public MapCodec<Ignite> codec() {
        return CODEC;
    }
}

