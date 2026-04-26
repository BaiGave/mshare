/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.enchantment;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public record TargetedConditionalEffect<T>(EnchantmentTarget enchanted, EnchantmentTarget affected, T effect, Optional<LootItemCondition> requirements) implements Validatable
{
    public static <S> Codec<TargetedConditionalEffect<S>> codec(Codec<S> effectCodec) {
        return RecordCodecBuilder.create(i -> i.group(((MapCodec)EnchantmentTarget.CODEC.fieldOf("enchanted")).forGetter(TargetedConditionalEffect::enchanted), ((MapCodec)EnchantmentTarget.CODEC.fieldOf("affected")).forGetter(TargetedConditionalEffect::affected), ((MapCodec)effectCodec.fieldOf("effect")).forGetter(TargetedConditionalEffect::effect), LootItemCondition.DIRECT_CODEC.optionalFieldOf("requirements").forGetter(TargetedConditionalEffect::requirements)).apply((Applicative<TargetedConditionalEffect, ?>)i, TargetedConditionalEffect::new));
    }

    public static <S> Codec<TargetedConditionalEffect<S>> equipmentDropsCodec(Codec<S> effectCodec) {
        return RecordCodecBuilder.create(i -> i.group(((MapCodec)EnchantmentTarget.NON_DAMAGE_CODEC.fieldOf("enchanted")).forGetter(TargetedConditionalEffect::enchanted), ((MapCodec)effectCodec.fieldOf("effect")).forGetter(TargetedConditionalEffect::effect), LootItemCondition.DIRECT_CODEC.optionalFieldOf("requirements").forGetter(TargetedConditionalEffect::requirements)).apply((Applicative<TargetedConditionalEffect, ?>)i, (target, effect, requirements) -> new TargetedConditionalEffect<Object>((EnchantmentTarget)target, EnchantmentTarget.VICTIM, effect, (Optional<LootItemCondition>)requirements)));
    }

    public boolean matches(LootContext context) {
        return this.requirements.isEmpty() || this.requirements.get().test(context);
    }

    @Override
    public void validate(ValidationContext context) {
        Validatable.validate(context, "requirements", this.requirements);
    }
}

