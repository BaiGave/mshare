/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.datafixers.functions;

import com.mojang.datafixers.functions.PointFreeRule;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;

public abstract class PointFree<T> {
    private volatile boolean initialized;
    @Nullable
    private Function<DynamicOps<?>, T> value;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Function<DynamicOps<?>, T> evalCached() {
        if (!this.initialized) {
            PointFree pointFree = this;
            synchronized (pointFree) {
                if (!this.initialized) {
                    this.value = this.eval();
                    this.initialized = true;
                }
            }
        }
        return this.value;
    }

    public abstract Type<T> type();

    public abstract Function<DynamicOps<?>, T> eval();

    Optional<? extends PointFree<T>> all(PointFreeRule rule) {
        return Optional.of(this);
    }

    Optional<? extends PointFree<T>> one(PointFreeRule rule) {
        return Optional.empty();
    }

    public final String toString() {
        return this.toString(0);
    }

    public static String indent(int level) {
        return " ".repeat(level);
    }

    public abstract String toString(int var1);
}

