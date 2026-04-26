/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.core.component.predicates;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.DataComponentPredicate;

public record DamagePredicate(MinMaxBounds.Ints durability, MinMaxBounds.Ints damage) implements DataComponentPredicate
{
    public static final Codec<DamagePredicate> CODEC = RecordCodecBuilder.create(i -> i.group(MinMaxBounds.Ints.CODEC.optionalFieldOf("durability", MinMaxBounds.Ints.ANY).forGetter(DamagePredicate::durability), MinMaxBounds.Ints.CODEC.optionalFieldOf("damage", MinMaxBounds.Ints.ANY).forGetter(DamagePredicate::damage)).apply((Applicative<DamagePredicate, ?>)i, DamagePredicate::new));

    @Override
    public boolean matches(DataComponentGetter components) {
        Integer damage = components.get(DataComponents.DAMAGE);
        if (damage == null) {
            return false;
        }
        int maxDamage = components.getOrDefault(DataComponents.MAX_DAMAGE, 0);
        if (!this.durability.matches(maxDamage - damage)) {
            return false;
        }
        return this.damage.matches(damage);
    }

    public static DamagePredicate durability(MinMaxBounds.Ints range) {
        return new DamagePredicate(range, MinMaxBounds.Ints.ANY);
    }
}

