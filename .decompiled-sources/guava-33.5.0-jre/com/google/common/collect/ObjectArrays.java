/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.ParametricNullness;
import com.google.common.collect.Platform;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public final class ObjectArrays {
    private ObjectArrays() {
    }

    @GwtIncompatible
    public static <T> T[] newArray(Class<@NonNull T> type, int length) {
        return (Object[])Array.newInstance(type, length);
    }

    public static <T> T[] newArray(T[] reference, int length) {
        return Platform.newArray(reference, length);
    }

    @GwtIncompatible
    public static <T> T[] concat(T[] first, T[] second, Class<@NonNull T> type) {
        T[] result = ObjectArrays.newArray(type, first.length + second.length);
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static <T> T[] concat(@ParametricNullness T element, T[] array) {
        T[] result = ObjectArrays.newArray(array, array.length + 1);
        result[0] = element;
        System.arraycopy(array, 0, result, 1, array.length);
        return result;
    }

    public static <T> T[] concat(T[] array, @ParametricNullness T element) {
        T[] result = Arrays.copyOf(array, array.length + 1);
        result[array.length] = element;
        return result;
    }

    static <T> T[] toArrayImpl(Collection<?> c, T[] array) {
        int size = c.size();
        if (array.length < size) {
            array = ObjectArrays.newArray(array, size);
        }
        ObjectArrays.fillArray(c, array);
        if (array.length > size) {
            @Nullable T[] unsoundlyCovariantArray = array;
            unsoundlyCovariantArray[size] = null;
        }
        return array;
    }

    static <T> T[] toArrayImpl(@Nullable Object[] src, int offset, int len, T[] dst) {
        Preconditions.checkPositionIndexes(offset, offset + len, src.length);
        if (dst.length < len) {
            dst = ObjectArrays.newArray(dst, len);
        } else if (dst.length > len) {
            T[] unsoundlyCovariantArray = dst;
            unsoundlyCovariantArray[len] = null;
        }
        System.arraycopy(src, offset, dst, 0, len);
        return dst;
    }

    static @Nullable Object[] toArrayImpl(Collection<?> c) {
        return ObjectArrays.fillArray(c, new Object[c.size()]);
    }

    static @Nullable Object[] copyAsObjectArray(@Nullable Object[] elements, int offset, int length) {
        Preconditions.checkPositionIndexes(offset, offset + length, elements.length);
        if (length == 0) {
            return new Object[0];
        }
        @Nullable Object[] result = new Object[length];
        System.arraycopy(elements, offset, result, 0, length);
        return result;
    }

    @CanIgnoreReturnValue
    private static @Nullable Object[] fillArray(Iterable<?> elements, @Nullable Object[] array) {
        int i = 0;
        for (Object element : elements) {
            array[i++] = element;
        }
        return array;
    }

    static void swap(Object[] array, int i, int j) {
        Object temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    @CanIgnoreReturnValue
    static Object[] checkElementsNotNull(Object ... array) {
        return ObjectArrays.checkElementsNotNull(array, array.length);
    }

    @CanIgnoreReturnValue
    static Object[] checkElementsNotNull(Object[] array, int length) {
        for (int i = 0; i < length; ++i) {
            ObjectArrays.checkElementNotNull(array[i], i);
        }
        return array;
    }

    @CanIgnoreReturnValue
    static Object checkElementNotNull(Object element, int index) {
        if (element == null) {
            throw new NullPointerException("at index " + index);
        }
        return element;
    }
}

