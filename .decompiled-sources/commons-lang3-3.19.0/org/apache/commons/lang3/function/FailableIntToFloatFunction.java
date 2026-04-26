/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.function;

@FunctionalInterface
public interface FailableIntToFloatFunction<E extends Throwable> {
    public static final FailableIntToFloatFunction NOP = t -> 0.0f;

    public static <E extends Throwable> FailableIntToFloatFunction<E> nop() {
        return NOP;
    }

    public float applyAsFloat(int var1) throws E;
}

