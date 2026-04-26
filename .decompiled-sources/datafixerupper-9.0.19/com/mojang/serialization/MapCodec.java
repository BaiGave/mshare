/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.serialization;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.CompressorHolder;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.KeyDispatchCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public abstract class MapCodec<A>
extends CompressorHolder
implements MapDecoder<A>,
MapEncoder<A> {
    public static <A> MapCodec<A> assumeMapUnsafe(final Codec<A> codec) {
        return new MapCodec<A>(){
            private static final String COMPRESSED_VALUE_KEY = "value";

            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.of(ops.createString(COMPRESSED_VALUE_KEY));
            }

            @Override
            public <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input) {
                if (ops.compressMaps()) {
                    T value = input.get(COMPRESSED_VALUE_KEY);
                    if (value == null) {
                        return DataResult.error(() -> "Missing value");
                    }
                    return codec.parse(ops, value);
                }
                return codec.parse(ops, ops.createMap(input.entries()));
            }

            @Override
            public <T> RecordBuilder<T> encode(A input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                DataResult<T> encoded = codec.encodeStart(ops, input);
                if (ops.compressMaps()) {
                    return prefix.add(COMPRESSED_VALUE_KEY, encoded);
                }
                DataResult encodedMapResult = encoded.flatMap(ops::getMap);
                return encodedMapResult.map((? super R encodedMap) -> {
                    encodedMap.entries().forEach(pair -> prefix.add(pair.getFirst(), pair.getSecond()));
                    return prefix;
                }).result().orElseGet(() -> prefix.withErrorsFrom(encodedMapResult));
            }
        };
    }

    public final <O> RecordCodecBuilder<O, A> forGetter(Function<O, A> getter) {
        return RecordCodecBuilder.of(getter, this);
    }

    public static <A> MapCodec<A> of(MapEncoder<A> encoder, MapDecoder<A> decoder) {
        return MapCodec.of(encoder, decoder, () -> "MapCodec[" + String.valueOf(encoder) + " " + String.valueOf(decoder) + "]");
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

    public static <A> MapCodec<A> recursive(String name, Function<Codec<A>, MapCodec<A>> wrapped) {
        return new RecursiveMapCodec<A>(name, wrapped);
    }

    public MapCodec<A> fieldOf(String name) {
        return this.codec().fieldOf(name);
    }

    @Override
    public MapCodec<A> withLifecycle(final Lifecycle lifecycle) {
        return new MapCodec<A>(){

            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return MapCodec.this.keys(ops);
            }

            @Override
            public <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input) {
                return MapCodec.this.decode(ops, input).setLifecycle(lifecycle);
            }

            @Override
            public <T> RecordBuilder<T> encode(A input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                return MapCodec.this.encode(input, ops, prefix).setLifecycle(lifecycle);
            }

            public String toString() {
                return MapCodec.this.toString();
            }
        };
    }

    public Codec<A> codec() {
        return new MapCodecCodec(this);
    }

    public MapCodec<A> stable() {
        return this.withLifecycle(Lifecycle.stable());
    }

    public MapCodec<A> deprecated(int since) {
        return this.withLifecycle(Lifecycle.deprecated(since));
    }

    public <S> MapCodec<S> xmap(Function<? super A, ? extends S> to, Function<? super S, ? extends A> from) {
        return MapCodec.of(this.comap(from), this.map(to), () -> this.toString() + "[xmapped]");
    }

    public <S> MapCodec<S> flatXmap(Function<? super A, ? extends DataResult<? extends S>> to, Function<? super S, ? extends DataResult<? extends A>> from) {
        return Codec.of(this.flatComap(from), this.flatMap(to), () -> this.toString() + "[flatXmapped]");
    }

    public MapCodec<A> validate(Function<A, DataResult<A>> checker) {
        return this.flatXmap(checker, checker);
    }

    public <E> MapCodec<A> dependent(MapCodec<E> initialInstance, Function<A, Pair<E, MapCodec<E>>> splitter, BiFunction<A, E, A> combiner) {
        return new Dependent<A, E>(this, initialInstance, splitter, combiner);
    }

    public <E> Codec<E> dispatch(Function<? super E, ? extends A> type, Function<? super A, ? extends MapCodec<? extends E>> codec) {
        return this.partialDispatch(type.andThen(DataResult::success), codec.andThen(DataResult::success));
    }

    public <E> Codec<E> dispatchStable(Function<? super E, ? extends A> type, Function<? super A, ? extends MapCodec<? extends E>> codec) {
        return this.partialDispatch(e -> DataResult.success(type.apply((Object)e), Lifecycle.stable()), a -> DataResult.success((MapCodec)codec.apply((Object)a), Lifecycle.stable()));
    }

    public <E> Codec<E> partialDispatch(Function<? super E, ? extends DataResult<? extends A>> type, Function<? super A, ? extends DataResult<? extends MapCodec<? extends E>>> codec) {
        return new KeyDispatchCodec<A, E>(this, type, codec).codec();
    }

    public <E> MapCodec<E> dispatchMap(Function<? super E, ? extends A> type, Function<? super A, ? extends MapCodec<? extends E>> codec) {
        return new KeyDispatchCodec<A, E>(this, type.andThen(DataResult::success), codec.andThen(DataResult::success));
    }

    @Override
    public abstract <T> Stream<T> keys(DynamicOps<T> var1);

    public MapCodec<A> mapResult(final ResultFunction<A> function) {
        return new MapCodec<A>(){

            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return MapCodec.this.keys(ops);
            }

            @Override
            public <T> RecordBuilder<T> encode(A input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                return function.coApply(ops, input, MapCodec.this.encode(input, ops, prefix));
            }

            @Override
            public <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input) {
                return function.apply(ops, input, MapCodec.this.decode(ops, input));
            }

            public String toString() {
                return String.valueOf(MapCodec.this) + "[mapResult " + String.valueOf(function) + "]";
            }
        };
    }

    public MapCodec<A> orElse(Consumer<String> onError, A value) {
        return this.orElse(DataFixUtils.consumerToFunction(onError), value);
    }

    public MapCodec<A> orElse(final UnaryOperator<String> onError, final A value) {
        return this.mapResult(new ResultFunction<A>(){

            @Override
            public <T> DataResult<A> apply(DynamicOps<T> ops, MapLike<T> input, DataResult<A> a) {
                return DataResult.success(a.mapError(onError).result().orElse(value));
            }

            @Override
            public <T> RecordBuilder<T> coApply(DynamicOps<T> ops, A input, RecordBuilder<T> t) {
                return t.mapError(onError);
            }

            public String toString() {
                return "OrElse[" + String.valueOf(onError) + " " + String.valueOf(value) + "]";
            }
        });
    }

    public MapCodec<A> orElseGet(Consumer<String> onError, Supplier<? extends A> value) {
        return this.orElseGet(DataFixUtils.consumerToFunction(onError), value);
    }

    public MapCodec<A> orElseGet(final UnaryOperator<String> onError, final Supplier<? extends A> value) {
        return this.mapResult(new ResultFunction<A>(){

            @Override
            public <T> DataResult<A> apply(DynamicOps<T> ops, MapLike<T> input, DataResult<A> a) {
                return DataResult.success(a.mapError(onError).result().orElseGet(value));
            }

            @Override
            public <T> RecordBuilder<T> coApply(DynamicOps<T> ops, A input, RecordBuilder<T> t) {
                return t.mapError(onError);
            }

            public String toString() {
                return "OrElseGet[" + String.valueOf(onError) + " " + String.valueOf(value.get()) + "]";
            }
        });
    }

    public MapCodec<A> orElse(final A value) {
        return this.mapResult(new ResultFunction<A>(){

            @Override
            public <T> DataResult<A> apply(DynamicOps<T> ops, MapLike<T> input, DataResult<A> a) {
                return DataResult.success(a.result().orElse(value));
            }

            @Override
            public <T> RecordBuilder<T> coApply(DynamicOps<T> ops, A input, RecordBuilder<T> t) {
                return t;
            }

            public String toString() {
                return "OrElse[" + String.valueOf(value) + "]";
            }
        });
    }

    public MapCodec<A> orElseGet(final Supplier<? extends A> value) {
        return this.mapResult(new ResultFunction<A>(){

            @Override
            public <T> DataResult<A> apply(DynamicOps<T> ops, MapLike<T> input, DataResult<A> a) {
                return DataResult.success(a.result().orElseGet(value));
            }

            @Override
            public <T> RecordBuilder<T> coApply(DynamicOps<T> ops, A input, RecordBuilder<T> t) {
                return t;
            }

            public String toString() {
                return "OrElseGet[" + String.valueOf(value.get()) + "]";
            }
        });
    }

    public MapCodec<A> setPartial(final Supplier<A> value) {
        return this.mapResult(new ResultFunction<A>(){

            @Override
            public <T> DataResult<A> apply(DynamicOps<T> ops, MapLike<T> input, DataResult<A> a) {
                return a.setPartial((Object)value);
            }

            @Override
            public <T> RecordBuilder<T> coApply(DynamicOps<T> ops, A input, RecordBuilder<T> t) {
                return t;
            }

            public String toString() {
                return "SetPartial[" + String.valueOf(value) + "]";
            }
        });
    }

    public static <A> MapCodec<A> unit(A defaultValue) {
        return MapCodec.unit(() -> defaultValue);
    }

    public static <A> MapCodec<A> unit(final Supplier<A> value) {
        return new MapCodec<A>(){

            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.empty();
            }

            @Override
            public <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input) {
                return DataResult.success(value.get());
            }

            @Override
            public <T> RecordBuilder<T> encode(A input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                return prefix;
            }

            @Override
            public Codec<A> codec() {
                return 10.unitCodec(value);
            }

            public String toString() {
                return "Unit[" + String.valueOf(value.get()) + "]";
            }
        };
    }

    public static <A> Codec<A> unitCodec(A value) {
        return MapCodec.unitCodec(() -> value);
    }

    public static <A> Codec<A> unitCodec(final Supplier<A> value) {
        return new Codec<A>(){

            @Override
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
                DataResult<Object> check = ops.compressMaps() ? ops.getList(input) : ops.getMap(input);
                return check.map(arg_0 -> 11.lambda$decode$0((Supplier)value, input, arg_0));
            }

            @Override
            public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
                return ops.mergeToMap(prefix, MapLike.empty());
            }

            public String toString() {
                return "Unit[" + String.valueOf(value.get()) + "]";
            }

            private static /* synthetic */ Pair lambda$decode$0(Supplier value2, Object input, Object ignore) {
                return Pair.of(value2.get(), input);
            }
        };
    }

    private static class RecursiveMapCodec<A>
    extends MapCodec<A> {
        private final String name;
        private final Supplier<MapCodec<A>> wrapped;

        private RecursiveMapCodec(String name, Function<Codec<A>, MapCodec<A>> wrapped) {
            this.name = name;
            this.wrapped = Suppliers.memoize(() -> (MapCodec)wrapped.apply(this.codec()));
        }

        @Override
        public <T> RecordBuilder<T> encode(A input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            return this.wrapped.get().encode(input, ops, prefix);
        }

        @Override
        public <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input) {
            return this.wrapped.get().decode(ops, input);
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return this.wrapped.get().keys(ops);
        }

        public String toString() {
            return "RecursiveMapCodec[" + this.name + "]";
        }
    }

    public record MapCodecCodec<A>(MapCodec<A> codec) implements Codec<A>
    {
        @Override
        public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
            return this.codec.compressedDecode(ops, input).map((? super R r) -> Pair.of(r, input));
        }

        @Override
        public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
            return this.codec.encode(input, ops, this.codec.compressedBuilder(ops)).build(prefix);
        }

        @Override
        public String toString() {
            return this.codec.toString();
        }
    }

    private static class Dependent<O, E>
    extends MapCodec<O> {
        private final MapCodec<E> initialInstance;
        private final Function<O, Pair<E, MapCodec<E>>> splitter;
        private final MapCodec<O> codec;
        private final BiFunction<O, E, O> combiner;

        public Dependent(MapCodec<O> codec, MapCodec<E> initialInstance, Function<O, Pair<E, MapCodec<E>>> splitter, BiFunction<O, E, O> combiner) {
            this.initialInstance = initialInstance;
            this.splitter = splitter;
            this.codec = codec;
            this.combiner = combiner;
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.concat(this.codec.keys(ops), this.initialInstance.keys(ops));
        }

        @Override
        public <T> DataResult<O> decode(DynamicOps<T> ops, MapLike<T> input) {
            return this.codec.decode(ops, input).flatMap((? super R base) -> this.splitter.apply(base).getSecond().decode(ops, input).map((? super R e) -> this.combiner.apply(base, e)).setLifecycle(Lifecycle.experimental()));
        }

        @Override
        public <T> RecordBuilder<T> encode(O input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            this.codec.encode(input, ops, prefix);
            Pair<E, MapCodec<E>> e = this.splitter.apply(input);
            e.getSecond().encode(e.getFirst(), ops, prefix);
            return prefix.setLifecycle(Lifecycle.experimental());
        }
    }

    public static interface ResultFunction<A> {
        public <T> DataResult<A> apply(DynamicOps<T> var1, MapLike<T> var2, DataResult<A> var3);

        public <T> RecordBuilder<T> coApply(DynamicOps<T> var1, A var2, RecordBuilder<T> var3);
    }
}

