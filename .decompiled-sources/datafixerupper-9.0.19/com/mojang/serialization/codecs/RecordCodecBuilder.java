/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.serialization.codecs;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public final class RecordCodecBuilder<O, F>
implements App<Mu<O>, F> {
    private final Function<O, F> getter;
    private final Function<O, MapEncoder<F>> encoder;
    private final MapDecoder<F> decoder;

    public static <O, F> RecordCodecBuilder<O, F> unbox(App<Mu<O>, F> box) {
        return (RecordCodecBuilder)box;
    }

    private RecordCodecBuilder(Function<O, F> getter, Function<O, MapEncoder<F>> encoder, MapDecoder<F> decoder) {
        this.getter = getter;
        this.encoder = encoder;
        this.decoder = decoder;
    }

    public static <O> Instance<O> instance() {
        return new Instance();
    }

    public static <O, F> RecordCodecBuilder<O, F> of(Function<O, F> getter, String name, Codec<F> fieldCodec) {
        return RecordCodecBuilder.of(getter, fieldCodec.fieldOf(name));
    }

    public static <O, F> RecordCodecBuilder<O, F> of(Function<O, F> getter, MapCodec<F> codec) {
        return new RecordCodecBuilder<Object, F>(getter, o -> codec, codec);
    }

    public static <O, F> RecordCodecBuilder<O, F> point(F instance) {
        return new RecordCodecBuilder<Object, Object>(o -> instance, o -> Encoder.empty(), Decoder.unit(instance));
    }

    public static <O, F> RecordCodecBuilder<O, F> stable(F instance) {
        return RecordCodecBuilder.point(instance, Lifecycle.stable());
    }

    public static <O, F> RecordCodecBuilder<O, F> deprecated(F instance, int since) {
        return RecordCodecBuilder.point(instance, Lifecycle.deprecated(since));
    }

    public static <O, F> RecordCodecBuilder<O, F> point(F instance, Lifecycle lifecycle) {
        return new RecordCodecBuilder<Object, Object>(o -> instance, o -> Encoder.empty().withLifecycle(lifecycle), Decoder.unit(instance).withLifecycle(lifecycle));
    }

    public static <O> Codec<O> create(Function<Instance<O>, ? extends App<Mu<O>, O>> builder) {
        return RecordCodecBuilder.build(builder.apply(RecordCodecBuilder.instance())).codec();
    }

    public static <O> MapCodec<O> mapCodec(Function<Instance<O>, ? extends App<Mu<O>, O>> builder) {
        return RecordCodecBuilder.build(builder.apply(RecordCodecBuilder.instance()));
    }

    public <E> RecordCodecBuilder<O, E> dependent(Function<O, E> getter, final MapEncoder<E> encoder, final Function<? super F, ? extends MapDecoder<E>> decoderGetter) {
        return new RecordCodecBuilder<Object, E>(getter, o -> encoder, new MapDecoder.Implementation<E>(){

            @Override
            public <T> DataResult<E> decode(DynamicOps<T> ops, MapLike<T> input) {
                return RecordCodecBuilder.this.decoder.decode(ops, input).map(decoderGetter).flatMap((? super R decoder1) -> decoder1.decode(ops, input).map(Function.identity()));
            }

            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return encoder.keys(ops);
            }

            public String toString() {
                return "Dependent[" + String.valueOf(encoder) + "]";
            }
        });
    }

    public static <O> MapCodec<O> build(App<Mu<O>, O> builderBox) {
        final RecordCodecBuilder<O, O> builder = RecordCodecBuilder.unbox(builderBox);
        return new MapCodec<O>(){

            @Override
            public <T> DataResult<O> decode(DynamicOps<T> ops, MapLike<T> input) {
                return builder.decoder.decode(ops, input);
            }

            @Override
            public <T> RecordBuilder<T> encode(O input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                return builder.encoder.apply(input).encode(input, ops, prefix);
            }

            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return builder.decoder.keys(ops);
            }

            public String toString() {
                return "RecordCodec[" + String.valueOf(builder.decoder) + "]";
            }
        };
    }

    public static final class Instance<O>
    implements Applicative<com.mojang.serialization.codecs.RecordCodecBuilder$Mu<O>, Mu<O>> {
        public <A> App<com.mojang.serialization.codecs.RecordCodecBuilder$Mu<O>, A> stable(A a) {
            return RecordCodecBuilder.stable(a);
        }

        public <A> App<com.mojang.serialization.codecs.RecordCodecBuilder$Mu<O>, A> deprecated(A a, int since) {
            return RecordCodecBuilder.deprecated(a, since);
        }

        public <A> App<com.mojang.serialization.codecs.RecordCodecBuilder$Mu<O>, A> point(A a, Lifecycle lifecycle) {
            return RecordCodecBuilder.point(a, lifecycle);
        }

        @Override
        public <A> App<com.mojang.serialization.codecs.RecordCodecBuilder$Mu<O>, A> point(A a) {
            return RecordCodecBuilder.point(a);
        }

        @Override
        public <A, R> Function<App<com.mojang.serialization.codecs.RecordCodecBuilder$Mu<O>, A>, App<com.mojang.serialization.codecs.RecordCodecBuilder$Mu<O>, R>> lift1(App<com.mojang.serialization.codecs.RecordCodecBuilder$Mu<O>, Function<A, R>> function) {
            return fa -> {
                final RecordCodecBuilder f = RecordCodecBuilder.unbox(function);
                final RecordCodecBuilder a = RecordCodecBuilder.unbox(fa);
                return new RecordCodecBuilder<Object, Object>(o -> ((Function)f.getter.apply(o)).apply(a.getter.apply(o)), o -> {
                    final MapEncoder fEnc = f.encoder.apply(o);
                    final MapEncoder aEnc = a.encoder.apply(o);
                    final Object aFromO = a.getter.apply(o);
                    return new MapEncoder.Implementation<R>(){

                        @Override
                        public <T> RecordBuilder<T> encode(R input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                            fEnc.encode(a1 -> input, ops, prefix);
                            aEnc.encode(aFromO, ops, prefix);
                            return prefix;
                        }

                        @Override
                        public <T> Stream<T> keys(DynamicOps<T> ops) {
                            return Stream.concat(aEnc.keys(ops), fEnc.keys(ops));
                        }

                        public String toString() {
                            return String.valueOf(fEnc) + " * " + String.valueOf(aEnc);
                        }
                    };
                }, new MapDecoder.Implementation<R>(){

                    @Override
                    public <T> DataResult<R> decode(DynamicOps<T> ops, MapLike<T> input) {
                        return a.decoder.decode(ops, input).flatMap((? super R ar) -> f2.decoder.decode(ops, input).map((? super R fr) -> fr.apply(ar)));
                    }

                    @Override
                    public <T> Stream<T> keys(DynamicOps<T> ops) {
                        return Stream.concat(a.decoder.keys(ops), f.decoder.keys(ops));
                    }

                    public String toString() {
                        return String.valueOf(f.decoder) + " * " + String.valueOf(a.decoder);
                    }
                });
            };
        }

        @Override
        public <A, B, R> App<com.mojang.serialization.codecs.RecordCodecBuilder$Mu<O>, R> ap2(App<com.mojang.serialization.codecs.RecordCodecBuilder$Mu<O>, BiFunction<A, B, R>> func, App<com.mojang.serialization.codecs.RecordCodecBuilder$Mu<O>, A> a, App<com.mojang.serialization.codecs.RecordCodecBuilder$Mu<O>, B> b) {
            final RecordCodecBuilder<O, BiFunction<A, B, R>> function = RecordCodecBuilder.unbox(func);
            final RecordCodecBuilder<O, A> fa = RecordCodecBuilder.unbox(a);
            final RecordCodecBuilder<O, B> fb = RecordCodecBuilder.unbox(b);
            return new RecordCodecBuilder<Object, Object>(o -> ((BiFunction)function.getter.apply(o)).apply(fa.getter.apply(o), fb.getter.apply(o)), o -> {
                final MapEncoder fEncoder = function.encoder.apply(o);
                final MapEncoder aEncoder = fa.encoder.apply(o);
                final Object aFromO = fa.getter.apply(o);
                final MapEncoder bEncoder = fb.encoder.apply(o);
                final Object bFromO = fb.getter.apply(o);
                return new MapEncoder.Implementation<R>(){

                    @Override
                    public <T> RecordBuilder<T> encode(R input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                        fEncoder.encode((a1, b1) -> input, ops, prefix);
                        aEncoder.encode(aFromO, ops, prefix);
                        bEncoder.encode(bFromO, ops, prefix);
                        return prefix;
                    }

                    @Override
                    public <T> Stream<T> keys(DynamicOps<T> ops) {
                        return Stream.of(fEncoder.keys(ops), aEncoder.keys(ops), bEncoder.keys(ops)).flatMap(Function.identity());
                    }

                    public String toString() {
                        return String.valueOf(fEncoder) + " * " + String.valueOf(aEncoder) + " * " + String.valueOf(bEncoder);
                    }
                };
            }, new MapDecoder.Implementation<R>(){

                @Override
                public <T> DataResult<R> decode(DynamicOps<T> ops, MapLike<T> input) {
                    return DataResult.unbox(DataResult.instance().ap2(function.decoder.decode(ops, input), fa.decoder.decode(ops, input), fb.decoder.decode(ops, input)));
                }

                @Override
                public <T> Stream<T> keys(DynamicOps<T> ops) {
                    return Stream.of(function.decoder.keys(ops), fa.decoder.keys(ops), fb.decoder.keys(ops)).flatMap(Function.identity());
                }

                public String toString() {
                    return String.valueOf(function.decoder) + " * " + String.valueOf(fa.decoder) + " * " + String.valueOf(fb.decoder);
                }
            });
        }

        @Override
        public <T1, T2, T3, R> App<com.mojang.serialization.codecs.RecordCodecBuilder$Mu<O>, R> ap3(App<com.mojang.serialization.codecs.RecordCodecBuilder$Mu<O>, Function3<T1, T2, T3, R>> func, App<com.mojang.serialization.codecs.RecordCodecBuilder$Mu<O>, T1> t1, App<com.mojang.serialization.codecs.RecordCodecBuilder$Mu<O>, T2> t2, App<com.mojang.serialization.codecs.RecordCodecBuilder$Mu<O>, T3> t3) {
            final RecordCodecBuilder<O, Function3<T1, T2, T3, R>> function = RecordCodecBuilder.unbox(func);
            final RecordCodecBuilder<O, T1> f1 = RecordCodecBuilder.unbox(t1);
            final RecordCodecBuilder<O, T2> f2 = RecordCodecBuilder.unbox(t2);
            final RecordCodecBuilder<O, T3> f3 = RecordCodecBuilder.unbox(t3);
            return new RecordCodecBuilder<Object, Object>(o -> ((Function3)function.getter.apply(o)).apply(f1.getter.apply(o), f2.getter.apply(o), f3.getter.apply(o)), o -> {
                final MapEncoder fEncoder = function.encoder.apply(o);
                final MapEncoder e1 = f1.encoder.apply(o);
                final Object v1 = f1.getter.apply(o);
                final MapEncoder e2 = f2.encoder.apply(o);
                final Object v2 = f2.getter.apply(o);
                final MapEncoder e3 = f3.encoder.apply(o);
                final Object v3 = f3.getter.apply(o);
                return new MapEncoder.Implementation<R>(){

                    @Override
                    public <T> RecordBuilder<T> encode(R input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                        fEncoder.encode((t1, t2, t3) -> input, ops, prefix);
                        e1.encode(v1, ops, prefix);
                        e2.encode(v2, ops, prefix);
                        e3.encode(v3, ops, prefix);
                        return prefix;
                    }

                    @Override
                    public <T> Stream<T> keys(DynamicOps<T> ops) {
                        return Stream.of(fEncoder.keys(ops), e1.keys(ops), e2.keys(ops), e3.keys(ops)).flatMap(Function.identity());
                    }

                    public String toString() {
                        return String.valueOf(fEncoder) + " * " + String.valueOf(e1) + " * " + String.valueOf(e2) + " * " + String.valueOf(e3);
                    }
                };
            }, new MapDecoder.Implementation<R>(){

                @Override
                public <T> DataResult<R> decode(DynamicOps<T> ops, MapLike<T> input) {
                    return DataResult.unbox(DataResult.instance().ap3(function.decoder.decode(ops, input), f1.decoder.decode(ops, input), f2.decoder.decode(ops, input), f3.decoder.decode(ops, input)));
                }

                @Override
                public <T> Stream<T> keys(DynamicOps<T> ops) {
                    return Stream.of(function.decoder.keys(ops), f1.decoder.keys(ops), f2.decoder.keys(ops), f3.decoder.keys(ops)).flatMap(Function.identity());
                }

                public String toString() {
                    return String.valueOf(function.decoder) + " * " + String.valueOf(f1.decoder) + " * " + String.valueOf(f2.decoder) + " * " + String.valueOf(f3.decoder);
                }
            });
        }

        @Override
        public <T1, T2, T3, T4, R> App<com.mojang.serialization.codecs.RecordCodecBuilder$Mu<O>, R> ap4(App<com.mojang.serialization.codecs.RecordCodecBuilder$Mu<O>, Function4<T1, T2, T3, T4, R>> func, App<com.mojang.serialization.codecs.RecordCodecBuilder$Mu<O>, T1> t1, App<com.mojang.serialization.codecs.RecordCodecBuilder$Mu<O>, T2> t2, App<com.mojang.serialization.codecs.RecordCodecBuilder$Mu<O>, T3> t3, App<com.mojang.serialization.codecs.RecordCodecBuilder$Mu<O>, T4> t4) {
            final RecordCodecBuilder<O, Function4<T1, T2, T3, T4, R>> function = RecordCodecBuilder.unbox(func);
            final RecordCodecBuilder<O, T1> f1 = RecordCodecBuilder.unbox(t1);
            final RecordCodecBuilder<O, T2> f2 = RecordCodecBuilder.unbox(t2);
            final RecordCodecBuilder<O, T3> f3 = RecordCodecBuilder.unbox(t3);
            final RecordCodecBuilder<O, T4> f4 = RecordCodecBuilder.unbox(t4);
            return new RecordCodecBuilder<Object, Object>(o -> ((Function4)function.getter.apply(o)).apply(f1.getter.apply(o), f2.getter.apply(o), f3.getter.apply(o), f4.getter.apply(o)), o -> {
                final MapEncoder fEncoder = function.encoder.apply(o);
                final MapEncoder e1 = f1.encoder.apply(o);
                final Object v1 = f1.getter.apply(o);
                final MapEncoder e2 = f2.encoder.apply(o);
                final Object v2 = f2.getter.apply(o);
                final MapEncoder e3 = f3.encoder.apply(o);
                final Object v3 = f3.getter.apply(o);
                final MapEncoder e4 = f4.encoder.apply(o);
                final Object v4 = f4.getter.apply(o);
                return new MapEncoder.Implementation<R>(){

                    @Override
                    public <T> RecordBuilder<T> encode(R input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                        fEncoder.encode((t1, t2, t3, t4) -> input, ops, prefix);
                        e1.encode(v1, ops, prefix);
                        e2.encode(v2, ops, prefix);
                        e3.encode(v3, ops, prefix);
                        e4.encode(v4, ops, prefix);
                        return prefix;
                    }

                    @Override
                    public <T> Stream<T> keys(DynamicOps<T> ops) {
                        return Stream.of(fEncoder.keys(ops), e1.keys(ops), e2.keys(ops), e3.keys(ops), e4.keys(ops)).flatMap(Function.identity());
                    }

                    public String toString() {
                        return String.valueOf(fEncoder) + " * " + String.valueOf(e1) + " * " + String.valueOf(e2) + " * " + String.valueOf(e3) + " * " + String.valueOf(e4);
                    }
                };
            }, new MapDecoder.Implementation<R>(){

                @Override
                public <T> DataResult<R> decode(DynamicOps<T> ops, MapLike<T> input) {
                    return DataResult.unbox(DataResult.instance().ap4(function.decoder.decode(ops, input), f1.decoder.decode(ops, input), f2.decoder.decode(ops, input), f3.decoder.decode(ops, input), f4.decoder.decode(ops, input)));
                }

                @Override
                public <T> Stream<T> keys(DynamicOps<T> ops) {
                    return Stream.of(function.decoder.keys(ops), f1.decoder.keys(ops), f2.decoder.keys(ops), f3.decoder.keys(ops), f4.decoder.keys(ops)).flatMap(Function.identity());
                }

                public String toString() {
                    return String.valueOf(function.decoder) + " * " + String.valueOf(f1.decoder) + " * " + String.valueOf(f2.decoder) + " * " + String.valueOf(f3.decoder) + " * " + String.valueOf(f4.decoder);
                }
            });
        }

        @Override
        public <T, R> App<com.mojang.serialization.codecs.RecordCodecBuilder$Mu<O>, R> map(Function<? super T, ? extends R> func, App<com.mojang.serialization.codecs.RecordCodecBuilder$Mu<O>, T> ts) {
            final RecordCodecBuilder unbox = RecordCodecBuilder.unbox(ts);
            final Function getter = unbox.getter;
            return new RecordCodecBuilder<Object, R>(getter.andThen(func), o -> new MapEncoder.Implementation<R>(){
                private final MapEncoder<T> encoder;
                {
                    this.encoder = unbox.encoder.apply(o);
                }

                @Override
                public <U> RecordBuilder<U> encode(R input, DynamicOps<U> ops, RecordBuilder<U> prefix) {
                    return this.encoder.encode(getter.apply(o), ops, prefix);
                }

                public <U> Stream<U> keys(DynamicOps<U> ops) {
                    return this.encoder.keys(ops);
                }

                public String toString() {
                    return String.valueOf(this.encoder) + "[mapped]";
                }
            }, unbox.decoder.map(func));
        }

        private static final class Mu<O>
        implements Applicative.Mu {
            private Mu() {
            }
        }
    }

    public static final class Mu<O>
    implements K1 {
    }
}

