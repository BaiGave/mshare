/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.serialization;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/*
 * Uses 'sealed' constructs - enablewith --sealed true
 */
public interface DataResult<R>
extends App<Mu, R> {
    public static <R> DataResult<R> unbox(App<Mu, R> box) {
        return (DataResult)box;
    }

    public static <R> DataResult<R> success(R result) {
        return DataResult.success(result, Lifecycle.experimental());
    }

    public static <R> DataResult<R> error(Supplier<String> message, R partialResult) {
        return DataResult.error(message, partialResult, Lifecycle.experimental());
    }

    public static <R> DataResult<R> error(Supplier<String> message) {
        return DataResult.error(message, Lifecycle.experimental());
    }

    public static <R> DataResult<R> success(R result, Lifecycle lifecycle) {
        return new Success<R>(result, lifecycle);
    }

    public static <R> DataResult<R> error(Supplier<String> message, R partialResult, Lifecycle lifecycle) {
        return new Error<R>(message, Optional.of(partialResult), lifecycle);
    }

    public static <R> DataResult<R> error(Supplier<String> message, Lifecycle lifecycle) {
        return new Error(message, Optional.empty(), lifecycle);
    }

    public static <K, V> Function<K, DataResult<V>> partialGet(Function<K, V> partialGet, Supplier<String> errorPrefix) {
        return name -> Optional.ofNullable(partialGet.apply(name)).map(DataResult::success).orElseGet(() -> DataResult.lambda$partialGet$1((Supplier)errorPrefix, name));
    }

    public static Instance instance() {
        return Instance.INSTANCE;
    }

    public static String appendMessages(String first, String second) {
        return first + "; " + second;
    }

    public Optional<R> result();

    public Optional<Error<R>> error();

    public Lifecycle lifecycle();

    public boolean hasResultOrPartial();

    public Optional<R> resultOrPartial(Consumer<String> var1);

    public Optional<R> resultOrPartial();

    public <E extends Throwable> R getOrThrow(Function<String, E> var1) throws E;

    public <E extends Throwable> R getPartialOrThrow(Function<String, E> var1) throws E;

    default public R getOrThrow() {
        return this.getOrThrow(IllegalStateException::new);
    }

    default public R getPartialOrThrow() {
        return this.getPartialOrThrow(IllegalStateException::new);
    }

    public <T> DataResult<T> map(Function<? super R, ? extends T> var1);

    public <T> T mapOrElse(Function<? super R, ? extends T> var1, Function<? super Error<R>, ? extends T> var2);

    public DataResult<R> ifSuccess(Consumer<? super R> var1);

    public DataResult<R> ifError(Consumer<? super Error<R>> var1);

    public DataResult<R> promotePartial(Consumer<String> var1);

    public <R2> DataResult<R2> flatMap(Function<? super R, ? extends DataResult<R2>> var1);

    public <R2> DataResult<R2> ap(DataResult<Function<R, R2>> var1);

    default public <R2, S> DataResult<S> apply2(BiFunction<R, R2, S> function, DataResult<R2> second) {
        return DataResult.unbox(DataResult.instance().apply2(function, this, second));
    }

    default public <R2, S> DataResult<S> apply2stable(BiFunction<R, R2, S> function, DataResult<R2> second) {
        Instance instance = DataResult.instance();
        DataResult f = DataResult.unbox(instance.point(function)).setLifecycle(Lifecycle.stable());
        return DataResult.unbox(instance.ap2(f, this, second));
    }

    default public <R2, R3, S> DataResult<S> apply3(Function3<R, R2, R3, S> function, DataResult<R2> second, DataResult<R3> third) {
        return DataResult.unbox(DataResult.instance().apply3(function, this, second, third));
    }

    public DataResult<R> setPartial(Supplier<R> var1);

    public DataResult<R> setPartial(R var1);

    public DataResult<R> mapError(UnaryOperator<String> var1);

    public DataResult<R> setLifecycle(Lifecycle var1);

    default public DataResult<R> addLifecycle(Lifecycle lifecycle) {
        return this.setLifecycle(this.lifecycle().add(lifecycle));
    }

    public boolean isSuccess();

    default public boolean isError() {
        return !this.isSuccess();
    }

    private static /* synthetic */ DataResult lambda$partialGet$1(Supplier errorPrefix, Object name) {
        return DataResult.error(() -> DataResult.lambda$partialGet$0((Supplier)errorPrefix, name));
    }

    private static /* synthetic */ String lambda$partialGet$0(Supplier errorPrefix, Object name) {
        return (String)errorPrefix.get() + String.valueOf(name);
    }

    public record Success<R>(R value, Lifecycle lifecycle) implements DataResult<R>
    {
        @Override
        public Optional<R> result() {
            return Optional.of(this.value);
        }

        @Override
        public Optional<Error<R>> error() {
            return Optional.empty();
        }

        @Override
        public boolean hasResultOrPartial() {
            return true;
        }

        @Override
        public Optional<R> resultOrPartial(Consumer<String> onError) {
            return Optional.of(this.value);
        }

        @Override
        public Optional<R> resultOrPartial() {
            return Optional.of(this.value);
        }

        @Override
        public <E extends Throwable> R getOrThrow(Function<String, E> exceptionSupplier) throws E {
            return this.value;
        }

        @Override
        public <E extends Throwable> R getPartialOrThrow(Function<String, E> exceptionSupplier) throws E {
            return this.value;
        }

        @Override
        public <T> DataResult<T> map(Function<? super R, ? extends T> function) {
            return new Success<T>(function.apply(this.value), this.lifecycle);
        }

        @Override
        public <T> T mapOrElse(Function<? super R, ? extends T> successFunction, Function<? super Error<R>, ? extends T> errorFunction) {
            return successFunction.apply(this.value);
        }

        @Override
        public DataResult<R> ifSuccess(Consumer<? super R> ifSuccess) {
            ifSuccess.accept(this.value);
            return this;
        }

        @Override
        public DataResult<R> ifError(Consumer<? super Error<R>> ifError) {
            return this;
        }

        @Override
        public DataResult<R> promotePartial(Consumer<String> onError) {
            return this;
        }

        @Override
        public <R2> DataResult<R2> flatMap(Function<? super R, ? extends DataResult<R2>> function) {
            return function.apply(this.value).addLifecycle(this.lifecycle);
        }

        @Override
        public <R2> DataResult<R2> ap(DataResult<Function<R, R2>> functionResult) {
            Lifecycle combinedLifecycle = this.lifecycle.add(functionResult.lifecycle());
            if (functionResult instanceof Success) {
                Success funcSuccess = (Success)functionResult;
                return new Success(((Function)funcSuccess.value).apply(this.value), combinedLifecycle);
            }
            if (functionResult instanceof Error) {
                Error funcError = (Error)functionResult;
                return new Error<Object>(funcError.messageSupplier, funcError.partialValue.map((? super T f) -> f.apply(this.value)), combinedLifecycle);
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public DataResult<R> setPartial(Supplier<R> partial) {
            return this;
        }

        @Override
        public DataResult<R> setPartial(R partial) {
            return this;
        }

        @Override
        public DataResult<R> mapError(UnaryOperator<String> function) {
            return this;
        }

        @Override
        public DataResult<R> setLifecycle(Lifecycle lifecycle) {
            if (this.lifecycle.equals(lifecycle)) {
                return this;
            }
            return new Success<R>(this.value, lifecycle);
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public String toString() {
            return "DataResult.Success[" + String.valueOf(this.value) + "]";
        }
    }

    public record Error<R>(Supplier<String> messageSupplier, Optional<R> partialValue, Lifecycle lifecycle) implements DataResult<R>
    {
        public String message() {
            return this.messageSupplier.get();
        }

        @Override
        public Optional<R> result() {
            return Optional.empty();
        }

        @Override
        public Optional<Error<R>> error() {
            return Optional.of(this);
        }

        @Override
        public boolean hasResultOrPartial() {
            return this.partialValue.isPresent();
        }

        @Override
        public Optional<R> resultOrPartial(Consumer<String> onError) {
            onError.accept(this.messageSupplier.get());
            return this.partialValue;
        }

        @Override
        public Optional<R> resultOrPartial() {
            return this.partialValue;
        }

        @Override
        public <E extends Throwable> R getOrThrow(Function<String, E> exceptionSupplier) throws E {
            throw (Throwable)exceptionSupplier.apply(this.message());
        }

        @Override
        public <E extends Throwable> R getPartialOrThrow(Function<String, E> exceptionSupplier) throws E {
            if (this.partialValue.isPresent()) {
                return this.partialValue.get();
            }
            throw (Throwable)exceptionSupplier.apply(this.message());
        }

        @Override
        public <T> Error<T> map(Function<? super R, ? extends T> function) {
            if (this.partialValue.isEmpty()) {
                return this;
            }
            return new Error<T>(this.messageSupplier, this.partialValue.map(function), this.lifecycle);
        }

        @Override
        public <T> T mapOrElse(Function<? super R, ? extends T> successFunction, Function<? super Error<R>, ? extends T> errorFunction) {
            return errorFunction.apply(this);
        }

        @Override
        public DataResult<R> ifSuccess(Consumer<? super R> ifSuccess) {
            return this;
        }

        @Override
        public DataResult<R> ifError(Consumer<? super Error<R>> ifError) {
            ifError.accept(this);
            return this;
        }

        @Override
        public DataResult<R> promotePartial(Consumer<String> onError) {
            onError.accept(this.messageSupplier.get());
            return this.partialValue.map((? super T value) -> new Success<Object>(value, this.lifecycle)).orElse(this);
        }

        @Override
        public <R2> Error<R2> flatMap(Function<? super R, ? extends DataResult<R2>> function) {
            if (this.partialValue.isEmpty()) {
                return this;
            }
            DataResult<R2> second = function.apply(this.partialValue.get());
            Lifecycle combinedLifecycle = this.lifecycle.add(second.lifecycle());
            if (second instanceof Success) {
                Success secondSuccess = (Success)second;
                return new Error(this.messageSupplier, Optional.of(secondSuccess.value), combinedLifecycle);
            }
            if (second instanceof Error) {
                Error secondError = (Error)second;
                return new Error<R>(() -> DataResult.appendMessages(this.messageSupplier.get(), secondError.messageSupplier.get()), secondError.partialValue, combinedLifecycle);
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public <R2> Error<R2> ap(DataResult<Function<R, R2>> functionResult) {
            Lifecycle combinedLifecycle = this.lifecycle.add(functionResult.lifecycle());
            if (functionResult instanceof Success) {
                Success func = (Success)functionResult;
                return new Error(this.messageSupplier, this.partialValue.map((Function)func.value), combinedLifecycle);
            }
            if (functionResult instanceof Error) {
                Error funcError = (Error)functionResult;
                return new Error(() -> DataResult.appendMessages(this.messageSupplier.get(), funcError.messageSupplier.get()), this.partialValue.flatMap((? super T a) -> funcError.partialValue.map((? super T f) -> f.apply(a))), combinedLifecycle);
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public Error<R> setPartial(Supplier<R> partial) {
            return this.setPartial((Object)partial.get());
        }

        @Override
        public Error<R> setPartial(R partial) {
            return new Error<R>(this.messageSupplier, Optional.of(partial), this.lifecycle);
        }

        @Override
        public Error<R> mapError(UnaryOperator<String> function) {
            return new Error<R>(() -> (String)function.apply(this.messageSupplier.get()), this.partialValue, this.lifecycle);
        }

        @Override
        public Error<R> setLifecycle(Lifecycle lifecycle) {
            if (this.lifecycle.equals(lifecycle)) {
                return this;
            }
            return new Error<R>(this.messageSupplier, this.partialValue, lifecycle);
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String toString() {
            return "DataResult.Error['" + this.message() + "'" + this.partialValue.map((? super T value) -> ": " + String.valueOf(value)).orElse("") + "]";
        }
    }

    public static enum Instance implements Applicative<com.mojang.serialization.DataResult$Mu, Mu>
    {
        INSTANCE;


        @Override
        public <T, R> App<com.mojang.serialization.DataResult$Mu, R> map(Function<? super T, ? extends R> func, App<com.mojang.serialization.DataResult$Mu, T> ts) {
            return DataResult.unbox(ts).map(func);
        }

        @Override
        public <A> App<com.mojang.serialization.DataResult$Mu, A> point(A a) {
            return DataResult.success(a);
        }

        @Override
        public <A, R> Function<App<com.mojang.serialization.DataResult$Mu, A>, App<com.mojang.serialization.DataResult$Mu, R>> lift1(App<com.mojang.serialization.DataResult$Mu, Function<A, R>> function) {
            return fa -> this.ap(function, (App)fa);
        }

        @Override
        public <A, R> App<com.mojang.serialization.DataResult$Mu, R> ap(App<com.mojang.serialization.DataResult$Mu, Function<A, R>> func, App<com.mojang.serialization.DataResult$Mu, A> arg) {
            return DataResult.unbox(arg).ap(DataResult.unbox(func));
        }

        @Override
        public <A, B, R> App<com.mojang.serialization.DataResult$Mu, R> ap2(App<com.mojang.serialization.DataResult$Mu, BiFunction<A, B, R>> func, App<com.mojang.serialization.DataResult$Mu, A> a, App<com.mojang.serialization.DataResult$Mu, B> b) {
            DataResult<BiFunction<A, B, R>> fr = DataResult.unbox(func);
            DataResult<A> ra = DataResult.unbox(a);
            DataResult<B> rb = DataResult.unbox(b);
            if (fr.result().isPresent() && ra.result().isPresent() && rb.result().isPresent()) {
                return new Success<R>(fr.result().get().apply(ra.result().get(), rb.result().get()), fr.lifecycle().add(ra.lifecycle()).add(rb.lifecycle()));
            }
            return Applicative.super.ap2(func, a, b);
        }

        @Override
        public <T1, T2, T3, R> App<com.mojang.serialization.DataResult$Mu, R> ap3(App<com.mojang.serialization.DataResult$Mu, Function3<T1, T2, T3, R>> func, App<com.mojang.serialization.DataResult$Mu, T1> t1, App<com.mojang.serialization.DataResult$Mu, T2> t2, App<com.mojang.serialization.DataResult$Mu, T3> t3) {
            DataResult<Function3<T1, T2, T3, R>> fr = DataResult.unbox(func);
            DataResult<T1> dr1 = DataResult.unbox(t1);
            DataResult<T2> dr2 = DataResult.unbox(t2);
            DataResult<T3> dr3 = DataResult.unbox(t3);
            if (fr.result().isPresent() && dr1.result().isPresent() && dr2.result().isPresent() && dr3.result().isPresent()) {
                return new Success<R>(fr.result().get().apply(dr1.result().get(), dr2.result().get(), dr3.result().get()), fr.lifecycle().add(dr1.lifecycle()).add(dr2.lifecycle()).add(dr3.lifecycle()));
            }
            return Applicative.super.ap3(func, t1, t2, t3);
        }

        public static final class Mu
        implements Applicative.Mu {
        }
    }

    public static final class Mu
    implements K1 {
    }
}

