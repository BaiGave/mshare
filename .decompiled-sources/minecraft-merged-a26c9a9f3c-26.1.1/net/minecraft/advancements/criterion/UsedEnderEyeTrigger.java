/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public class UsedEnderEyeTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, BlockPos feature) {
        double xd = player.getX() - (double)feature.getX();
        double zd = player.getZ() - (double)feature.getZ();
        double sqrDist = xd * xd + zd * zd;
        this.trigger(player, (T t) -> t.matches(sqrDist));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, MinMaxBounds.Doubles distance) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(i -> i.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), MinMaxBounds.Doubles.CODEC.optionalFieldOf("distance", MinMaxBounds.Doubles.ANY).forGetter(TriggerInstance::distance)).apply((Applicative<TriggerInstance, ?>)i, TriggerInstance::new));

        public boolean matches(double sqrDistance) {
            return this.distance.matchesSqr(sqrDistance);
        }
    }
}

