/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.function;

@FunctionalInterface
public interface FailableToBooleanFunction<T, E extends Throwable> {
    public static final FailableToBooleanFunction NOP = t -> false;

    public static <T, E extends Throwable> FailableToBooleanFunction<T, E> nop() {
        return NOP;
    }

    public boolean applyAsBoolean(T var1) throws E;
}

