/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import java.util.Objects;
import java.util.function.Function;

public interface BoundedFloatFunction<C> {
    public static final BoundedFloatFunction<Float> IDENTITY = BoundedFloatFunction.createUnlimited(input -> input);

    public float apply(C var1);

    public float minValue();

    public float maxValue();

    public static BoundedFloatFunction<Float> createUnlimited(final Float2FloatFunction function) {
        return new BoundedFloatFunction<Float>(){

            @Override
            public float apply(Float aFloat) {
                return ((Float)function.apply(aFloat)).floatValue();
            }

            @Override
            public float minValue() {
                return Float.NEGATIVE_INFINITY;
            }

            @Override
            public float maxValue() {
                return Float.POSITIVE_INFINITY;
            }
        };
    }

    default public <C2> BoundedFloatFunction<C2> comap(final Function<C2, C> function) {
        final BoundedFloatFunction outer = this;
        return new BoundedFloatFunction<C2>(this){
            {
                Objects.requireNonNull(this$0);
            }

            @Override
            public float apply(C2 c2) {
                return outer.apply(function.apply(c2));
            }

            @Override
            public float minValue() {
                return outer.minValue();
            }

            @Override
            public float maxValue() {
                return outer.maxValue();
            }
        };
    }
}

