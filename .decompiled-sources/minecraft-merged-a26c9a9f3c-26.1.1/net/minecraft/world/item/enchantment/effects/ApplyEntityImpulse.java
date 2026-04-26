/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.enchantment.effects;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

public record ApplyEntityImpulse(Vec3 direction, Vec3 coordinateScale, LevelBasedValue magnitude) implements EnchantmentEntityEffect
{
    public static final MapCodec<ApplyEntityImpulse> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Vec3.CODEC.fieldOf("direction")).forGetter(ApplyEntityImpulse::direction), ((MapCodec)Vec3.CODEC.fieldOf("coordinate_scale")).forGetter(ApplyEntityImpulse::coordinateScale), ((MapCodec)LevelBasedValue.CODEC.fieldOf("magnitude")).forGetter(ApplyEntityImpulse::magnitude)).apply((Applicative<ApplyEntityImpulse, ?>)i, ApplyEntityImpulse::new));
    private static final int POST_IMPULSE_CONTEXT_RESET_GRACE_TIME_TICKS = 10;

    @Override
    public void apply(ServerLevel serverLevel, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 position) {
        Vec3 look = entity.getLookAngle();
        Vec3 direction = look.addLocalCoordinates(this.direction).multiply(this.coordinateScale).scale(this.magnitude.calculate(enchantmentLevel));
        entity.addDeltaMovement(direction);
        entity.hurtMarked = true;
        entity.needsSync = true;
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            livingEntity.applyPostImpulseGraceTime(10);
        }
    }

    public MapCodec<ApplyEntityImpulse> codec() {
        return CODEC;
    }
}

