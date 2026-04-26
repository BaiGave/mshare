/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.consume_effects;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.Level;

public record RemoveStatusEffectsConsumeEffect(HolderSet<MobEffect> effects) implements ConsumeEffect
{
    public static final MapCodec<RemoveStatusEffectsConsumeEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)RegistryCodecs.homogeneousList(Registries.MOB_EFFECT).fieldOf("effects")).forGetter(RemoveStatusEffectsConsumeEffect::effects)).apply((Applicative<RemoveStatusEffectsConsumeEffect, ?>)i, RemoveStatusEffectsConsumeEffect::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveStatusEffectsConsumeEffect> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.holderSet(Registries.MOB_EFFECT), RemoveStatusEffectsConsumeEffect::effects, RemoveStatusEffectsConsumeEffect::new);

    public RemoveStatusEffectsConsumeEffect(Holder<MobEffect> only) {
        this(HolderSet.direct(only));
    }

    public ConsumeEffect.Type<RemoveStatusEffectsConsumeEffect> getType() {
        return ConsumeEffect.Type.REMOVE_EFFECTS;
    }

    @Override
    public boolean apply(Level level, ItemStack stack, LivingEntity user) {
        boolean hasRemovedAny = false;
        for (Holder holder : this.effects) {
            if (!user.removeEffect(holder)) continue;
            hasRemovedAny = true;
        }
        return hasRemovedAny;
    }
}

