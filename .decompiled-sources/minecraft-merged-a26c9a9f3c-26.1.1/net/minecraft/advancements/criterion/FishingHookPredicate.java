/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.advancements.criterion.EntitySubPredicates;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public record FishingHookPredicate(Optional<Boolean> inOpenWater) implements EntitySubPredicate
{
    public static final FishingHookPredicate ANY = new FishingHookPredicate(Optional.empty());
    public static final MapCodec<FishingHookPredicate> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(Codec.BOOL.optionalFieldOf("in_open_water").forGetter(FishingHookPredicate::inOpenWater)).apply((Applicative<FishingHookPredicate, ?>)i, FishingHookPredicate::new));

    public static FishingHookPredicate inOpenWater(boolean requirement) {
        return new FishingHookPredicate(Optional.of(requirement));
    }

    public MapCodec<FishingHookPredicate> codec() {
        return EntitySubPredicates.FISHING_HOOK;
    }

    @Override
    public boolean matches(Entity entity, ServerLevel level, @Nullable Vec3 position) {
        if (this.inOpenWater.isEmpty()) {
            return true;
        }
        if (entity instanceof FishingHook) {
            FishingHook hook = (FishingHook)entity;
            return this.inOpenWater.get().booleanValue() == hook.isOpenWaterFishing();
        }
        return false;
    }
}

