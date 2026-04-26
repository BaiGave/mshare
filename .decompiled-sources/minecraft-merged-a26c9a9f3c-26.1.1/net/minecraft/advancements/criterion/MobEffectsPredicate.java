/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.advancements.criterion;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public record MobEffectsPredicate(Map<Holder<MobEffect>, MobEffectInstancePredicate> effectMap) {
    public static final Codec<MobEffectsPredicate> CODEC = Codec.unboundedMap(MobEffect.CODEC, MobEffectInstancePredicate.CODEC).xmap(MobEffectsPredicate::new, MobEffectsPredicate::effectMap);

    public boolean matches(Entity entity) {
        LivingEntity living;
        return entity instanceof LivingEntity && this.matches((living = (LivingEntity)entity).getActiveEffectsMap());
    }

    public boolean matches(LivingEntity entity) {
        return this.matches(entity.getActiveEffectsMap());
    }

    public boolean matches(Map<Holder<MobEffect>, MobEffectInstance> effects) {
        for (Map.Entry<Holder<MobEffect>, MobEffectInstancePredicate> entry : this.effectMap.entrySet()) {
            MobEffectInstance instance = effects.get(entry.getKey());
            if (entry.getValue().matches(instance)) continue;
            return false;
        }
        return true;
    }

    public record MobEffectInstancePredicate(MinMaxBounds.Ints amplifier, MinMaxBounds.Ints duration, Optional<Boolean> ambient, Optional<Boolean> visible) {
        public static final Codec<MobEffectInstancePredicate> CODEC = RecordCodecBuilder.create(i -> i.group(MinMaxBounds.Ints.CODEC.optionalFieldOf("amplifier", MinMaxBounds.Ints.ANY).forGetter(MobEffectInstancePredicate::amplifier), MinMaxBounds.Ints.CODEC.optionalFieldOf("duration", MinMaxBounds.Ints.ANY).forGetter(MobEffectInstancePredicate::duration), Codec.BOOL.optionalFieldOf("ambient").forGetter(MobEffectInstancePredicate::ambient), Codec.BOOL.optionalFieldOf("visible").forGetter(MobEffectInstancePredicate::visible)).apply((Applicative<MobEffectInstancePredicate, ?>)i, MobEffectInstancePredicate::new));

        public MobEffectInstancePredicate() {
            this(MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, Optional.empty(), Optional.empty());
        }

        public boolean matches(@Nullable MobEffectInstance instance) {
            if (instance == null) {
                return false;
            }
            if (!this.amplifier.matches(instance.getAmplifier())) {
                return false;
            }
            if (!this.duration.matches(instance.getDuration())) {
                return false;
            }
            if (this.ambient.isPresent() && this.ambient.get().booleanValue() != instance.isAmbient()) {
                return false;
            }
            return !this.visible.isPresent() || this.visible.get().booleanValue() == instance.isVisible();
        }
    }

    public static class Builder {
        private final ImmutableMap.Builder<Holder<MobEffect>, MobEffectInstancePredicate> effectMap = ImmutableMap.builder();

        public static Builder effects() {
            return new Builder();
        }

        public Builder and(Holder<MobEffect> effect) {
            this.effectMap.put(effect, new MobEffectInstancePredicate());
            return this;
        }

        public Builder and(Holder<MobEffect> effect, MobEffectInstancePredicate predicate) {
            this.effectMap.put(effect, predicate);
            return this;
        }

        public Optional<MobEffectsPredicate> build() {
            return Optional.of(new MobEffectsPredicate(this.effectMap.build()));
        }
    }
}

