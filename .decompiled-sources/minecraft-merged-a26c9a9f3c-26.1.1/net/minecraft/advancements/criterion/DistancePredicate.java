/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.util.Mth;

public record DistancePredicate(MinMaxBounds.Doubles x, MinMaxBounds.Doubles y, MinMaxBounds.Doubles z, MinMaxBounds.Doubles horizontal, MinMaxBounds.Doubles absolute) {
    public static final Codec<DistancePredicate> CODEC = RecordCodecBuilder.create(i -> i.group(MinMaxBounds.Doubles.CODEC.optionalFieldOf("x", MinMaxBounds.Doubles.ANY).forGetter(DistancePredicate::x), MinMaxBounds.Doubles.CODEC.optionalFieldOf("y", MinMaxBounds.Doubles.ANY).forGetter(DistancePredicate::y), MinMaxBounds.Doubles.CODEC.optionalFieldOf("z", MinMaxBounds.Doubles.ANY).forGetter(DistancePredicate::z), MinMaxBounds.Doubles.CODEC.optionalFieldOf("horizontal", MinMaxBounds.Doubles.ANY).forGetter(DistancePredicate::horizontal), MinMaxBounds.Doubles.CODEC.optionalFieldOf("absolute", MinMaxBounds.Doubles.ANY).forGetter(DistancePredicate::absolute)).apply((Applicative<DistancePredicate, ?>)i, DistancePredicate::new));

    public static DistancePredicate horizontal(MinMaxBounds.Doubles horizontal) {
        return new DistancePredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, horizontal, MinMaxBounds.Doubles.ANY);
    }

    public static DistancePredicate vertical(MinMaxBounds.Doubles y) {
        return new DistancePredicate(MinMaxBounds.Doubles.ANY, y, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY);
    }

    public static DistancePredicate absolute(MinMaxBounds.Doubles absolute) {
        return new DistancePredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, absolute);
    }

    public boolean matches(double x0, double y0, double z0, double x1, double y1, double z1) {
        float xd = (float)(x0 - x1);
        float yd = (float)(y0 - y1);
        float zd = (float)(z0 - z1);
        if (!(this.x.matches(Mth.abs(xd)) && this.y.matches(Mth.abs(yd)) && this.z.matches(Mth.abs(zd)))) {
            return false;
        }
        if (!this.horizontal.matchesSqr(xd * xd + zd * zd)) {
            return false;
        }
        return this.absolute.matchesSqr(xd * xd + yd * yd + zd * zd);
    }
}

