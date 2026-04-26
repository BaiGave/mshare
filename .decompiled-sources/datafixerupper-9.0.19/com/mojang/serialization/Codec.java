/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.serialization;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.CompoundListCodec;
import com.mojang.serialization.codecs.DispatchedMapCodec;
import com.mojang.serialization.codecs.EitherCodec;
import com.mojang.serialization.codecs.EitherMapCodec;
import com.mojang.serialization.codecs.ListCodec;
import com.mojang.serialization.codecs.OptionalFieldCodec;
import com.mojang.serialization.codecs.PairCodec;
import com.mojang.serialization.codecs.PairMapCodec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.SimpleMapCodec;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import com.mojang.serialization.codecs.XorCodec;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public interface Codec<A>
extends Encoder<A>,
Decoder<A> {
    public static final PrimitiveCodec<Boolean> BOOL = new PrimitiveCodec<Boolean>(){

        @Override
        public <T> DataResult<Boolean> read(DynamicOps<T> ops, T input) {
            return ops.getBooleanValue(input);
        }

        @Override
        public <T> T write(DynamicOps<T> ops, Boolean value) {
            return ops.createBoolean(value);
        }

        public String toString() {
            return "Bool";
        }
    };
    public static final PrimitiveCodec<Byte> BYTE = new PrimitiveCodec<Byte>(){

        @Override
        public <T> DataResult<Byte> read(DynamicOps<T> ops, T input) {
            return ops.getNumberValue(input).map(Number::byteValue);
        }

        @Override
        public <T> T write(DynamicOps<T> ops, Byte value) {
            return ops.createByte(value);
        }

        public String toString() {
            return "Byte";
        }
    };
    public static final PrimitiveCodec<Short> SHORT = new PrimitiveCodec<Short>(){

        @Override
        public <T> DataResult<Short> read(DynamicOps<T> ops, T input) {
            return ops.getNumberValue(input).map(Number::shortValue);
        }

        @Override
        public <T> T write(DynamicOps<T> ops, Short value) {
            return ops.createShort(value);
        }

        public String toString() {
            return "Short";
        }
    };
    public static final PrimitiveCodec<Integer> INT = new PrimitiveCodec<Integer>(){

        @Override
        public <T> DataResult<Integer> read(DynamicOps<T> ops, T input) {
            return ops.getNumberValue(input).map(Number::intValue);
        }

        @Override
        public <T> T write(DynamicOps<T> ops, Integer value) {
            return ops.createInt(value);
        }

        public String toString() {
            return "Int";
        }
    };
    public static final PrimitiveCodec<Long> LONG = new PrimitiveCodec<Long>(){

        @Override
        public <T> DataResult<Long> read(DynamicOps<T> ops, T input) {
            return ops.getNumberValue(input).map(Number::longValue);
        }

        @Override
        public <T> T write(DynamicOps<T> ops, Long value) {
            return ops.createLong(value);
        }

        public String toString() {
            return "Long";
        }
    };
    public static final PrimitiveCodec<Float> FLOAT = new PrimitiveCodec<Float>(){

        @Override
        public <T> DataResult<Float> read(DynamicOps<T> ops, T input) {
            return ops.getNumberValue(input).map(Number::floatValue);
        }

        @Override
        public <T> T write(DynamicOps<T> ops, Float value) {
            return ops.createFloat(value.floatValue());
        }

        public String toString() {
            return "Float";
        }
    };
    public static final PrimitiveCodec<Double> DOUBLE = new PrimitiveCodec<Double>(){

        @Override
        public <T> DataResult<Double> read(DynamicOps<T> ops, T input) {
            return ops.getNumberValue(input).map(Number::doubleValue);
        }

        @Override
        public <T> T write(DynamicOps<T> ops, Double value) {
            return ops.createDouble(value);
        }

        public String toString() {
            return "Double";
        }
    };
    public static final PrimitiveCodec<String> STRING = new PrimitiveCodec<String>(){

        @Override
        public <T> DataResult<String> read(DynamicOps<T> ops, T input) {
            return ops.getStringValue(input);
        }

        @Override
        public <T> T write(DynamicOps<T> ops, String value) {
            return ops.createString(value);
        }

        public String toString() {
            return "String";
        }
    };
    public static final PrimitiveCodec<ByteBuffer> BYTE_BUFFER = new PrimitiveCodec<ByteBuffer>(){

        @Override
        public <T> DataResult<ByteBuffer> read(DynamicOps<T> ops, T input) {
            return ops.getByteBuffer(input);
        }

        @Override
        public <T> T write(DynamicOps<T> ops, ByteBuffer value) {
            return ops.createByteList(value);
        }

        public String toString() {
            return "ByteBuffer";
        }
    };
    public static final PrimitiveCodec<IntStream> INT_STREAM = new PrimitiveCodec<IntStream>(){

        @Override
        public <T> DataResult<IntStream> read(DynamicOps<T> ops, T input) {
            return ops.getIntStream(input);
        }

        @Override
        public <T> T write(DynamicOps<T> ops, IntStream value) {
            return ops.createIntList(value);
        }

        public String toString() {
            return "IntStream";
        }
    };
    public static final PrimitiveCodec<LongStream> LONG_STREAM = new PrimitiveCodec<LongStream>(){

        @Override
        public <T> DataResult<LongStream> read(DynamicOps<T> ops, T input) {
            return ops.getLongStream(input);
        }

        @Override
        public <T> T write(DynamicOps<T> ops, LongStream value) {
            return ops.createLongList(value);
        }

        public String toString() {
            return "LongStream";
        }
    };
    public static final Codec<Dynamic<?>> PASSTHROUGH = new Codec<Dynamic<?>>(){

        @Override
        public <T> DataResult<Pair<Dynamic<?>, T>> decode(DynamicOps<T> ops, T input) {
            return DataResult.success(Pair.of(new Dynamic<T>(ops, input), ops.empty()));
        }

        @Override
        public <T> DataResult<T> encode(Dynamic<?> input, DynamicOps<T> ops, T prefix) {
            if (input.getValue() == input.getOps().empty()) {
                return DataResult.success(prefix, Lifecycle.experimental());
            }
            Object casted = input.convert(ops).getValue();
            if (prefix == ops.empty()) {
                return DataResult.success(casted, Lifecycle.experimental());
            }
            DataResult toMap = ops.getMap(casted).flatMap((? super R map) -> ops.mergeToMap(prefix, (MapLike)map));
            return toMap.result().map(DataResult::success).orElseGet(() -> {
                DataResult toList = ops.getStream(casted).flatMap((? super R stream) -> ops.mergeToList(prefix, stream.collect(Collectors.toList())));
                return toList.result().map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Don't know how to merge " + String.valueOf(prefix) + " and " + String.valueOf(casted), prefix, Lifecycle.experimental()));
            });
        }

        public String toString() {
            return "passthrough";
        }
    };
    public static final MapCodec<Unit> EMPTY = MapCodec.unit(Unit.INSTANCE);

    @Override
    default public Codec<A> withLifecycle(final Lifecycle lifecycle) {
        return new Codec<A>(){

            @Override
            public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
                return Codec.this.encode(input, ops, prefix).setLifecycle(lifecycle);
            }

            @Override
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
                return Codec.this.decode(ops, input).setLifecycle(lifecycle);
            }

            public String toString() {
                return Codec.this.toString();
            }
        };
    }

    default public Codec<A> stable() {
        return this.withLifecycle(Lifecycle.stable());
    }

    default public Codec<A> deprecated(int since) {
        return this.withLifecycle(Lifecycle.deprecated(since));
    }

    public static <A> Codec<A> of(Encoder<A> encoder, Decoder<A> decoder) {
        return Codec.of(encoder, decoder, "Codec[" + String.valueOf(encoder) + " " + String.valueOf(decoder) + "]");
    }

    public static <A> Codec<A> of(final Encoder<A> encoder, final Decoder<A> decoder, final String name) {
        return new Codec<A>(){

            @Override
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
                return decoder.decode(ops, input);
            }

            @Override
            public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
                return encoder.encode(input, ops, prefix);
            }

            public String toString() {
                return name;
            }
        };
    }

    public static <A> MapCodec<A> of(MapEncoder<A> encoder, MapDecoder<A> decoder) {
        return Codec.of(encoder, decoder, () -> "MapCodec[" + String.valueOf(encoder) + " " + String.valueOf(decoder) + "]");
    }

    public static <A> MapCodec<A> of(final MapEncoder<A> encoder, final MapDecoder<A> decoder, final Supplier<String> name) {
        return new MapCodec<A>(){

            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.concat(encoder.keys(ops), decoder.keys(ops));
            }

            @Override
            public <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input) {
                return decoder.decode(ops, input);
            }

            @Override
            public <T> RecordBuilder<T> encode(A input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                return encoder.encode(input, ops, prefix);
            }

            public String toString() {
                return (String)name.get();
            }
        };
    }

    public static <F, S> Codec<Pair<F, S>> pair(Codec<F> first, Codec<S> second) {
        return new PairCodec<F, S>(first, second);
    }

    public static <F, S> Codec<Either<F, S>> either(Codec<F> first, Codec<S> second) {
        return new EitherCodec<F, S>(first, second);
    }

    public static <F, S> Codec<Either<F, S>> xor(Codec<F> first, Codec<S> second) {
        return new XorCodec<F, S>(first, second);
    }

    public static <T> Codec<T> withAlternative(Codec<T> primary, Codec<? extends T> alternative) {
        return primary.withAlternative(alternative);
    }

    default public Codec<A> withAlternative(Codec<? extends A> alternative) {
        return Codec.either(this, alternative).xmap(Either::unwrap, Either::left);
    }

    public static <T, U> Codec<T> withAlternative(Codec<T> primary, Codec<U> alternative, Function<U, T> converter) {
        return primary.withAlternative(alternative, converter);
    }

    default public <U> Codec<A> withAlternative(Codec<U> alternative, Function<U, A> converter) {
        return Codec.either(this, alternative).xmap(either -> either.map(v -> v, converter), Either::left);
    }

    public static <F, S> MapCodec<Pair<F, S>> mapPair(MapCodec<F> first, MapCodec<S> second) {
        return new PairMapCodec<F, S>(first, second);
    }

    public static <F, S> MapCodec<Either<F, S>> mapEither(MapCodec<F> first, MapCodec<S> second) {
        return new EitherMapCodec<F, S>(first, second);
    }

    public static <E> Codec<List<E>> list(Codec<E> elementCodec) {
        return Codec.list(elementCodec, 0, Integer.MAX_VALUE);
    }

    public static <E> Codec<List<E>> list(Codec<E> elementCodec, int minSize, int maxSize) {
        return new ListCodec<E>(elementCodec, minSize, maxSize);
    }

    public static <K, V> Codec<List<Pair<K, V>>> compoundList(Codec<K> keyCodec, Codec<V> elementCodec) {
        return new CompoundListCodec<K, V>(keyCodec, elementCodec);
    }

    public static <K, V> SimpleMapCodec<K, V> simpleMap(Codec<K> keyCodec, Codec<V> elementCodec, Keyable keys) {
        return new SimpleMapCodec<K, V>(keyCodec, elementCodec, keys);
    }

    public static <K, V> UnboundedMapCodec<K, V> unboundedMap(Codec<K> keyCodec, Codec<V> elementCodec) {
        return new UnboundedMapCodec<K, V>(keyCodec, elementCodec);
    }

    public static <K, V> Codec<Map<K, V>> dispatchedMap(Codec<K> keyCodec, Function<K, Codec<? extends V>> valueCodecFunction) {
        return new DispatchedMapCodec<K, V>(keyCodec, valueCodecFunction);
    }

    public static <E> Codec<E> stringResolver(Function<E, String> toString, Function<String, E> fromString) {
        return STRING.flatXmap(name -> Optional.ofNullable(fromString.apply((String)name)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown element name:" + name)), e -> Optional.ofNullable((String)toString.apply(e)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Element with unknown name: " + String.valueOf(e))));
    }

    public static <F> MapCodec<Optional<F>> optionalField(String name, Codec<F> elementCodec, boolean lenient) {
        return new OptionalFieldCodec<F>(name, elementCodec, lenient);
    }

    public static <A> Codec<A> recursive(String name, Function<Codec<A>, Codec<A>> wrapped) {
        return new RecursiveCodec(name, wrapped);
    }

    public static <A> Codec<A> lazyInitialized(Supplier<Codec<A>> delegate) {
        return new RecursiveCodec(delegate.toString(), self -> (Codec)delegate.get());
    }

    default public Codec<List<A>> listOf() {
        return Codec.list(this);
    }

    default public Codec<List<A>> listOf(int minSize, int maxSize) {
        return Codec.list(this, minSize, maxSize);
    }

    default public Codec<List<A>> sizeLimitedListOf(int maxSize) {
        return this.listOf(0, maxSize);
    }

    default public <S> Codec<S> xmap(Function<? super A, ? extends S> to, Function<? super S, ? extends A> from) {
        return Codec.of(this.comap(from), this.map(to), this.toString() + "[xmapped]");
    }

    default public <S> Codec<S> comapFlatMap(Function<? super A, ? extends DataResult<? extends S>> to, Function<? super S, ? extends A> from) {
        return Codec.of(this.comap(from), this.flatMap(to), this.toString() + "[comapFlatMapped]");
    }

    default public <S> Codec<S> flatComapMap(Function<? super A, ? extends S> to, Function<? super S, ? extends DataResult<? extends A>> from) {
        return Codec.of(this.flatComap(from), this.map(to), this.toString() + "[flatComapMapped]");
    }

    default public <S> Codec<S> flatXmap(Function<? super A, ? extends DataResult<? extends S>> to, Function<? super S, ? extends DataResult<? extends A>> from) {
        return Codec.of(this.flatComap(from), this.flatMap(to), this.toString() + "[flatXmapped]");
    }

    @Override
    default public MapCodec<A> fieldOf(String name) {
        return MapCodec.of(Encoder.super.fieldOf(name), Decoder.super.fieldOf(name), () -> "Field[" + name + ": " + this.toString() + "]");
    }

    default public MapCodec<Optional<A>> optionalFieldOf(String name) {
        return Codec.optionalField(name, this, false);
    }

    default public MapCodec<A> optionalFieldOf(String name, A defaultValue) {
        return this.optionalFieldOf(name, defaultValue, false);
    }

    default public MapCodec<A> optionalFieldOf(String name, A defaultValue, Lifecycle lifecycleOfDefault) {
        return this.optionalFieldOf(name, Lifecycle.experimental(), defaultValue, lifecycleOfDefault);
    }

    default public MapCodec<A> optionalFieldOf(String name, Lifecycle fieldLifecycle, A defaultValue, Lifecycle lifecycleOfDefault) {
        return this.optionalFieldOf(name, fieldLifecycle, defaultValue, lifecycleOfDefault, false);
    }

    default public MapCodec<Optional<A>> lenientOptionalFieldOf(String name) {
        return Codec.optionalField(name, this, true);
    }

    default public MapCodec<A> lenientOptionalFieldOf(String name, A defaultValue) {
        return this.optionalFieldOf(name, defaultValue, true);
    }

    default public MapCodec<A> lenientOptionalFieldOf(String name, A defaultValue, Lifecycle lifecycleOfDefault) {
        return this.lenientOptionalFieldOf(name, Lifecycle.experimental(), defaultValue, lifecycleOfDefault);
    }

    default public MapCodec<A> lenientOptionalFieldOf(String name, Lifecycle fieldLifecycle, A defaultValue, Lifecycle lifecycleOfDefault) {
        return this.optionalFieldOf(name, fieldLifecycle, defaultValue, lifecycleOfDefault, true);
    }

    private MapCodec<A> optionalFieldOf(String name, A defaultValue, boolean lenient) {
        return Codec.optionalField(name, this, lenient).xmap(o -> o.orElse(defaultValue), a -> Objects.equals(a, defaultValue) ? Optional.empty() : Optional.of(a));
    }

    private MapCodec<A> optionalFieldOf(String name, Lifecycle fieldLifecycle, A defaultValue, Lifecycle lifecycleOfDefault, boolean lenient) {
        return Codec.optionalField(name, this, lenient).stable().flatXmap(o -> o.map((? super T v) -> DataResult.success(v, fieldLifecycle)).orElse(DataResult.success(defaultValue, lifecycleOfDefault)), a -> Objects.equals(a, defaultValue) ? DataResult.success(Optional.empty(), lifecycleOfDefault) : DataResult.success(Optional.of(a), fieldLifecycle));
    }

    default public Codec<A> mapResult(final ResultFunction<A> function) {
        return new Codec<A>(){

            @Override
            public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
                return function.coApply(ops, input, Codec.this.encode(input, ops, prefix));
            }

            @Override
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
                return function.apply(ops, input, Codec.this.decode(ops, input));
            }

            public String toString() {
                return String.valueOf(Codec.this) + "[mapResult " + String.valueOf(function) + "]";
            }
        };
    }

    default public Codec<A> orElse(Consumer<String> onError, A value) {
        return this.orElse(DataFixUtils.consumerToFunction(onError), value);
    }

    default public Codec<A> orElse(final UnaryOperator<String> onError, final A value) {
        return this.mapResult(new ResultFunction<A>(){

            @Override
            public <T> DataResult<Pair<A, T>> apply(DynamicOps<T> ops, T input, DataResult<Pair<A, T>> a) {
                return DataResult.success(a.mapError(onError).result().orElseGet(() -> Pair.of(value, input)));
            }

            @Override
            public <T> DataResult<T> coApply(DynamicOps<T> ops, A input, DataResult<T> t) {
                return t.mapError(onError);
            }

            public String toString() {
                return "OrElse[" + String.valueOf(onError) + " " + String.valueOf(value) + "]";
            }
        });
    }

    default public Codec<A> orElseGet(Consumer<String> onError, Supplier<? extends A> value) {
        return this.orElseGet(DataFixUtils.consumerToFunction(onError), value);
    }

    default public Codec<A> orElseGet(final UnaryOperator<String> onError, final Supplier<? extends A> value) {
        return this.mapResult(new ResultFunction<A>(){

            @Override
            public <T> DataResult<Pair<A, T>> apply(DynamicOps<T> ops, T input, DataResult<Pair<A, T>> a) {
                return DataResult.success(a.mapError(onError).result().orElseGet(() -> 6.lambda$apply$0((Supplier)value, input)));
            }

            @Override
            public <T> DataResult<T> coApply(DynamicOps<T> ops, A input, DataResult<T> t) {
                return t.mapError(onError);
            }

            public String toString() {
                return "OrElseGet[" + String.valueOf(onError) + " " + String.valueOf(value.get()) + "]";
            }

            private static /* synthetic */ Pair lambda$apply$0(Supplier value2, Object input) {
                return Pair.of(value2.get(), input);
            }
        });
    }

    default public Codec<A> orElse(final A value) {
        return this.mapResult(new ResultFunction<A>(){

            @Override
            public <T> DataResult<Pair<A, T>> apply(DynamicOps<T> ops, T input, DataResult<Pair<A, T>> a) {
                return DataResult.success(a.result().orElseGet(() -> Pair.of(value, input)));
            }

            @Override
            public <T> DataResult<T> coApply(DynamicOps<T> ops, A input, DataResult<T> t) {
                return t;
            }

            public String toString() {
                return "OrElse[" + String.valueOf(value) + "]";
            }
        });
    }

    default public Codec<A> orElseGet(final Supplier<? extends A> value) {
        return this.mapResult(new ResultFunction<A>(){

            @Override
            public <T> DataResult<Pair<A, T>> apply(DynamicOps<T> ops, T input, DataResult<Pair<A, T>> a) {
                return DataResult.success(a.result().orElseGet(() -> 8.lambda$apply$0((Supplier)value, input)));
            }

            @Override
            public <T> DataResult<T> coApply(DynamicOps<T> ops, A input, DataResult<T> t) {
                return t;
            }

            public String toString() {
                return "OrElseGet[" + String.valueOf(value.get()) + "]";
            }

            private static /* synthetic */ Pair lambda$apply$0(Supplier value2, Object input) {
                return Pair.of(value2.get(), input);
            }
        });
    }

    @Override
    default public Codec<A> promotePartial(Consumer<String> onError) {
        return Codec.of(this, Decoder.super.promotePartial(onError));
    }

    default public <E> Codec<E> dispatch(Function<? super E, ? extends A> type, Function<? super A, ? extends MapCodec<? extends E>> codec) {
        return this.dispatch("type", type, codec);
    }

    default public <E> Codec<E> dispatch(String typeKey, Function<? super E, ? extends A> type, Function<? super A, ? extends MapCodec<? extends E>> codec) {
        return ((MapCodec)this.fieldOf(typeKey)).dispatch(type, codec);
    }

    default public <E> Codec<E> dispatchStable(Function<? super E, ? extends A> type, Function<? super A, ? extends MapCodec<? extends E>> codec) {
        return ((MapCodec)this.fieldOf("type")).dispatchStable(type, codec);
    }

    default public <E> Codec<E> partialDispatch(String typeKey, Function<? super E, ? extends DataResult<? extends A>> type, Function<? super A, ? extends DataResult<? extends MapCodec<? extends E>>> codec) {
        return ((MapCodec)this.fieldOf(typeKey)).partialDispatch(type, codec);
    }

    default public <E> MapCodec<E> dispatchMap(Function<? super E, ? extends A> type, Function<? super A, ? extends MapCodec<? extends E>> codec) {
        return this.dispatchMap("type", type, codec);
    }

    default public <E> MapCodec<E> dispatchMap(String typeKey, Function<? super E, ? extends A> type, Function<? super A, ? extends MapCodec<? extends E>> codec) {
        return ((MapCodec)this.fieldOf(typeKey)).dispatchMap(type, codec);
    }

    default public Codec<A> validate(Function<A, DataResult<A>> checker) {
        return this.flatXmap(checker, checker);
    }

    public static <N extends Number> Function<N, DataResult<N>> checkRange(N minInclusive, N maxInclusive) {
        return value -> {
            if (((Comparable)((Object)value)).compareTo(minInclusive) >= 0 && ((Comparable)((Object)value)).compareTo(maxInclusive) <= 0) {
                return DataResult.success(value);
            }
            return DataResult.error(() -> "Value " + String.valueOf(value) + " outside of range [" + String.valueOf(minInclusive) + ":" + String.valueOf(maxInclusive) + "]");
        };
    }

    public static Codec<Integer> intRange(int minInclusive, int maxInclusive) {
        Function<Integer, DataResult<Integer>> checker = Codec.checkRange(minInclusive, maxInclusive);
        return INT.flatXmap(checker, checker);
    }

    public static Codec<Float> floatRange(float minInclusive, float maxInclusive) {
        Function<Float, DataResult<Float>> checker = Codec.checkRange(Float.valueOf(minInclusive), Float.valueOf(maxInclusive));
        return FLOAT.flatXmap(checker, checker);
    }

    public static Codec<Double> doubleRange(double minInclusive, double maxInclusive) {
        Function<Double, DataResult<Double>> checker = Codec.checkRange(minInclusive, maxInclusive);
        return DOUBLE.flatXmap(checker, checker);
    }

    public static Codec<String> string(int minSize, int maxSize) {
        return STRING.validate(value -> {
            int length = value.length();
            if (length < minSize) {
                return DataResult.error(() -> "String \"" + value + "\" is too short: " + length + ", expected range [" + minSize + "-" + maxSize + "]");
            }
            if (length > maxSize) {
                return DataResult.error(() -> "String \"" + value + "\" is too long: " + length + ", expected range [" + minSize + "-" + maxSize + "]");
            }
            return DataResult.success(value);
        });
    }

    public static Codec<String> sizeLimitedString(int maxSize) {
        return Codec.string(0, maxSize);
    }

    public static class RecursiveCodec<T>
    implements Codec<T> {
        private final String name;
        private final Supplier<Codec<T>> wrapped;

        private RecursiveCodec(String name, Function<Codec<T>, Codec<T>> wrapped) {
            this.name = name;
            this.wrapped = Suppliers.memoize(() -> (Codec)wrapped.apply(this));
        }

        @Override
        public <S> DataResult<Pair<T, S>> decode(DynamicOps<S> ops, S input) {
            return this.wrapped.get().decode(ops, input);
        }

        @Override
        public <S> DataResult<S> encode(T input, DynamicOps<S> ops, S prefix) {
            return this.wrapped.get().encode(input, ops, prefix);
        }

        public String toString() {
            return "RecursiveCodec[" + this.name + "]";
        }
    }

    public static interface ResultFunction<A> {
        public <T> DataResult<Pair<A, T>> apply(DynamicOps<T> var1, T var2, DataResult<Pair<A, T>> var3);

        public <T> DataResult<T> coApply(DynamicOps<T> var1, A var2, DataResult<T> var3);
    }
}

