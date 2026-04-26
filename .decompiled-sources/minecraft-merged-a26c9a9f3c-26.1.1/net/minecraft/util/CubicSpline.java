/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.util.BoundedFloatFunction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.VisibleForDebug;
import org.apache.commons.lang3.mutable.MutableObject;

public interface CubicSpline<C, I extends BoundedFloatFunction<C>>
extends BoundedFloatFunction<C> {
    @VisibleForDebug
    public String parityString();

    public CubicSpline<C, I> mapAll(CoordinateVisitor<I> var1);

    public static <C, I extends BoundedFloatFunction<C>> Codec<CubicSpline<C, I>> codec(Codec<I> coordinateCodec) {
        record Point<C, I extends BoundedFloatFunction<C>>(float location, CubicSpline<C, I> value, float derivative) {
        }
        MutableObject<Codec<CubicSpline>> result = new MutableObject<Codec<CubicSpline>>();
        Codec pointCodec = RecordCodecBuilder.create(i -> i.group(((MapCodec)Codec.FLOAT.fieldOf("location")).forGetter(Point::location), ((MapCodec)Codec.lazyInitialized(result).fieldOf("value")).forGetter(Point::value), ((MapCodec)Codec.FLOAT.fieldOf("derivative")).forGetter(Point::derivative)).apply((Applicative<Point, ?>)i, (x$0, x$1, x$2) -> new Point(x$0.floatValue(), x$1, x$2.floatValue())));
        Codec multipointCodec = RecordCodecBuilder.create(i -> i.group(((MapCodec)coordinateCodec.fieldOf("coordinate")).forGetter(Multipoint::coordinate), ((MapCodec)ExtraCodecs.nonEmptyList(pointCodec.listOf()).fieldOf("points")).forGetter(m -> IntStream.range(0, m.locations.length).mapToObj(p -> new Point(m.locations()[p], m.values().get(p), m.derivatives()[p])).toList())).apply((Applicative<Multipoint, ?>)i, (coordinate, points) -> {
            float[] locations = new float[points.size()];
            ImmutableList.Builder values = ImmutableList.builder();
            float[] derivatives = new float[points.size()];
            for (int p = 0; p < points.size(); ++p) {
                Point point = (Point)points.get(p);
                locations[p] = point.location();
                values.add(point.value());
                derivatives[p] = point.derivative();
            }
            return Multipoint.create(coordinate, locations, values.build(), derivatives);
        }));
        result.setValue(Codec.either(Codec.FLOAT, multipointCodec).xmap(e -> (CubicSpline)((Object)e.map(Constant::new, m -> m)), s -> {
            Either either;
            if (s instanceof Constant) {
                Constant c = (Constant)s;
                either = Either.left(Float.valueOf(c.value()));
            } else {
                either = Either.right((Multipoint)s);
            }
            return either;
        }));
        return (Codec)result.get();
    }

    public static <C, I extends BoundedFloatFunction<C>> CubicSpline<C, I> constant(float value) {
        return new Constant(value);
    }

    public static <C, I extends BoundedFloatFunction<C>> Builder<C, I> builder(I coordinate) {
        return new Builder(coordinate);
    }

    public static <C, I extends BoundedFloatFunction<C>> Builder<C, I> builder(I coordinate, BoundedFloatFunction<Float> valueTransformer) {
        return new Builder(coordinate, valueTransformer);
    }

    @VisibleForDebug
    public record Constant<C, I extends BoundedFloatFunction<C>>(float value) implements CubicSpline<C, I>
    {
        @Override
        public float apply(C c) {
            return this.value;
        }

        @Override
        public String parityString() {
            return String.format(Locale.ROOT, "k=%.3f", Float.valueOf(this.value));
        }

        @Override
        public float minValue() {
            return this.value;
        }

        @Override
        public float maxValue() {
            return this.value;
        }

        @Override
        public CubicSpline<C, I> mapAll(CoordinateVisitor<I> visitor) {
            return this;
        }
    }

    public static final class Builder<C, I extends BoundedFloatFunction<C>> {
        private final I coordinate;
        private final BoundedFloatFunction<Float> valueTransformer;
        private final FloatList locations = new FloatArrayList();
        private final List<CubicSpline<C, I>> values = Lists.newArrayList();
        private final FloatList derivatives = new FloatArrayList();

        protected Builder(I coordinate) {
            this(coordinate, BoundedFloatFunction.IDENTITY);
        }

        protected Builder(I coordinate, BoundedFloatFunction<Float> valueTransformer) {
            this.coordinate = coordinate;
            this.valueTransformer = valueTransformer;
        }

        public Builder<C, I> addPoint(float location, float value) {
            return this.addPoint(location, new Constant(this.valueTransformer.apply(Float.valueOf(value))), 0.0f);
        }

        public Builder<C, I> addPoint(float location, float value, float derivative) {
            return this.addPoint(location, new Constant(this.valueTransformer.apply(Float.valueOf(value))), derivative);
        }

        public Builder<C, I> addPoint(float location, CubicSpline<C, I> sampler) {
            return this.addPoint(location, sampler, 0.0f);
        }

        private Builder<C, I> addPoint(float location, CubicSpline<C, I> sampler, float derivative) {
            if (!this.locations.isEmpty() && location <= this.locations.getFloat(this.locations.size() - 1)) {
                throw new IllegalArgumentException("Please register points in ascending order");
            }
            this.locations.add(location);
            this.values.add(sampler);
            this.derivatives.add(derivative);
            return this;
        }

        public CubicSpline<C, I> build() {
            if (this.locations.isEmpty()) {
                throw new IllegalStateException("No elements added");
            }
            return Multipoint.create(this.coordinate, this.locations.toFloatArray(), ImmutableList.copyOf(this.values), this.derivatives.toFloatArray());
        }
    }

    @VisibleForDebug
    public record Multipoint<C, I extends BoundedFloatFunction<C>>(I coordinate, float[] locations, List<CubicSpline<C, I>> values, float[] derivatives, float minValue, float maxValue) implements CubicSpline<C, I>
    {
        public Multipoint {
            Multipoint.validateSizes(locations, values, derivatives);
        }

        private static <C, I extends BoundedFloatFunction<C>> Multipoint<C, I> create(I coordinate, float[] locations, List<CubicSpline<C, I>> values, float[] derivatives) {
            float edge2;
            float edge1;
            Multipoint.validateSizes(locations, values, derivatives);
            int lastIndex = locations.length - 1;
            float minValue = Float.POSITIVE_INFINITY;
            float maxValue = Float.NEGATIVE_INFINITY;
            float minInput = coordinate.minValue();
            float maxInput = coordinate.maxValue();
            if (minInput < locations[0]) {
                edge1 = Multipoint.linearExtend(minInput, locations, values.get(0).minValue(), derivatives, 0);
                edge2 = Multipoint.linearExtend(minInput, locations, values.get(0).maxValue(), derivatives, 0);
                minValue = Math.min(minValue, Math.min(edge1, edge2));
                maxValue = Math.max(maxValue, Math.max(edge1, edge2));
            }
            if (maxInput > locations[lastIndex]) {
                edge1 = Multipoint.linearExtend(maxInput, locations, values.get(lastIndex).minValue(), derivatives, lastIndex);
                edge2 = Multipoint.linearExtend(maxInput, locations, values.get(lastIndex).maxValue(), derivatives, lastIndex);
                minValue = Math.min(minValue, Math.min(edge1, edge2));
                maxValue = Math.max(maxValue, Math.max(edge1, edge2));
            }
            for (CubicSpline<C, I> value : values) {
                minValue = Math.min(minValue, value.minValue());
                maxValue = Math.max(maxValue, value.maxValue());
            }
            for (int i = 0; i < lastIndex; ++i) {
                float x1 = locations[i];
                float x2 = locations[i + 1];
                float xDiff = x2 - x1;
                CubicSpline<C, I> v1 = values.get(i);
                CubicSpline<C, I> v2 = values.get(i + 1);
                float min1 = v1.minValue();
                float max1 = v1.maxValue();
                float min2 = v2.minValue();
                float max2 = v2.maxValue();
                float d1 = derivatives[i];
                float d2 = derivatives[i + 1];
                if (d1 == 0.0f && d2 == 0.0f) continue;
                float p1 = d1 * xDiff;
                float p2 = d2 * xDiff;
                float minLerp1 = Math.min(min1, min2);
                float maxLerp1 = Math.max(max1, max2);
                float minA = p1 - max2 + min1;
                float maxA = p1 - min2 + max1;
                float minB = -p2 + min2 - max1;
                float maxB = -p2 + max2 - min1;
                float minLerp2 = Math.min(minA, minB);
                float maxLerp2 = Math.max(maxA, maxB);
                minValue = Math.min(minValue, minLerp1 + 0.25f * minLerp2);
                maxValue = Math.max(maxValue, maxLerp1 + 0.25f * maxLerp2);
            }
            return new Multipoint<C, I>(coordinate, locations, values, derivatives, minValue, maxValue);
        }

        private static float linearExtend(float input, float[] locations, float value, float[] derivatives, int index) {
            float derivative = derivatives[index];
            if (derivative == 0.0f) {
                return value;
            }
            return value + derivative * (input - locations[index]);
        }

        private static <C, I extends BoundedFloatFunction<C>> void validateSizes(float[] locations, List<CubicSpline<C, I>> values, float[] derivatives) {
            if (locations.length != values.size() || locations.length != derivatives.length) {
                throw new IllegalArgumentException("All lengths must be equal, got: " + locations.length + " " + values.size() + " " + derivatives.length);
            }
            if (locations.length == 0) {
                throw new IllegalArgumentException("Cannot create a multipoint spline with no points");
            }
        }

        @Override
        public float apply(C c) {
            float input = this.coordinate.apply(c);
            int start = Multipoint.findIntervalStart(this.locations, input);
            int lastIndex = this.locations.length - 1;
            if (start < 0) {
                return Multipoint.linearExtend(input, this.locations, this.values.get(0).apply(c), this.derivatives, 0);
            }
            if (start == lastIndex) {
                return Multipoint.linearExtend(input, this.locations, this.values.get(lastIndex).apply(c), this.derivatives, lastIndex);
            }
            float x1 = this.locations[start];
            float x2 = this.locations[start + 1];
            float t = (input - x1) / (x2 - x1);
            BoundedFloatFunction f1 = this.values.get(start);
            BoundedFloatFunction f2 = this.values.get(start + 1);
            float d1 = this.derivatives[start];
            float d2 = this.derivatives[start + 1];
            float y1 = f1.apply(c);
            float y2 = f2.apply(c);
            float a = d1 * (x2 - x1) - (y2 - y1);
            float b = -d2 * (x2 - x1) + (y2 - y1);
            float offset = Mth.lerp(t, y1, y2) + t * (1.0f - t) * Mth.lerp(t, a, b);
            return offset;
        }

        private static int findIntervalStart(float[] locations, float input) {
            return Mth.binarySearch(0, locations.length, i -> input < locations[i]) - 1;
        }

        @Override
        @VisibleForTesting
        public String parityString() {
            return "Spline{coordinate=" + String.valueOf(this.coordinate) + ", locations=" + this.toString(this.locations) + ", derivatives=" + this.toString(this.derivatives) + ", values=" + this.values.stream().map(CubicSpline::parityString).collect(Collectors.joining(", ", "[", "]")) + "}";
        }

        private String toString(float[] arr) {
            return "[" + IntStream.range(0, arr.length).mapToDouble(i -> arr[i]).mapToObj(f -> String.format(Locale.ROOT, "%.3f", f)).collect(Collectors.joining(", ")) + "]";
        }

        @Override
        public CubicSpline<C, I> mapAll(CoordinateVisitor<I> visitor) {
            return Multipoint.create((BoundedFloatFunction)visitor.visit(this.coordinate), this.locations, this.values().stream().map(v -> v.mapAll(visitor)).toList(), this.derivatives);
        }
    }

    public static interface CoordinateVisitor<I> {
        public I visit(I var1);
    }
}

