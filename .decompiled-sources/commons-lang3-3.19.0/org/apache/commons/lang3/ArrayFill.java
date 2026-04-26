/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import java.util.Arrays;
import org.apache.commons.lang3.function.FailableIntFunction;

public final class ArrayFill {
    public static boolean[] fill(boolean[] a, boolean val) {
        if (a != null) {
            Arrays.fill(a, val);
        }
        return a;
    }

    public static byte[] fill(byte[] a, byte val) {
        if (a != null) {
            Arrays.fill(a, val);
        }
        return a;
    }

    public static char[] fill(char[] a, char val) {
        if (a != null) {
            Arrays.fill(a, val);
        }
        return a;
    }

    public static double[] fill(double[] a, double val) {
        if (a != null) {
            Arrays.fill(a, val);
        }
        return a;
    }

    public static float[] fill(float[] a, float val) {
        if (a != null) {
            Arrays.fill(a, val);
        }
        return a;
    }

    public static int[] fill(int[] a, int val) {
        if (a != null) {
            Arrays.fill(a, val);
        }
        return a;
    }

    public static long[] fill(long[] a, long val) {
        if (a != null) {
            Arrays.fill(a, val);
        }
        return a;
    }

    public static short[] fill(short[] a, short val) {
        if (a != null) {
            Arrays.fill(a, val);
        }
        return a;
    }

    public static <T, E extends Throwable> T[] fill(T[] array, FailableIntFunction<? extends T, E> generator) throws E {
        if (array != null && generator != null) {
            for (int i = 0; i < array.length; ++i) {
                array[i] = generator.apply(i);
            }
        }
        return array;
    }

    public static <T> T[] fill(T[] a, T val) {
        if (a != null) {
            Arrays.fill(a, val);
        }
        return a;
    }

    private ArrayFill() {
    }
}

