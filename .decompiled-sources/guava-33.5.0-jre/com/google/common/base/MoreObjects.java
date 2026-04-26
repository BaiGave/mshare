/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public final class MoreObjects {
    public static <T> T firstNonNull(@Nullable T first, @Nullable T second) {
        if (first != null) {
            return first;
        }
        if (second != null) {
            return second;
        }
        throw new NullPointerException("Both parameters are null");
    }

    public static ToStringHelper toStringHelper(Object self) {
        return new ToStringHelper(self.getClass().getSimpleName());
    }

    public static ToStringHelper toStringHelper(Class<?> clazz) {
        return new ToStringHelper(clazz.getSimpleName());
    }

    public static ToStringHelper toStringHelper(String className) {
        return new ToStringHelper(className);
    }

    private MoreObjects() {
    }

    public static final class ToStringHelper {
        private final String className;
        private final ValueHolder holderHead;
        private ValueHolder holderTail;
        private boolean omitNullValues;
        private boolean omitEmptyValues;

        private ToStringHelper(String className) {
            this.holderTail = this.holderHead = new ValueHolder();
            this.omitNullValues = false;
            this.omitEmptyValues = false;
            this.className = Preconditions.checkNotNull(className);
        }

        @CanIgnoreReturnValue
        public ToStringHelper omitNullValues() {
            this.omitNullValues = true;
            return this;
        }

        @CanIgnoreReturnValue
        public ToStringHelper omitEmptyValues() {
            this.omitEmptyValues = true;
            return this;
        }

        @CanIgnoreReturnValue
        public ToStringHelper add(String name, @Nullable Object value) {
            return this.addHolder(name, value);
        }

        @CanIgnoreReturnValue
        public ToStringHelper add(String name, boolean value) {
            return this.addUnconditionalHolder(name, String.valueOf(value));
        }

        @CanIgnoreReturnValue
        public ToStringHelper add(String name, char value) {
            return this.addUnconditionalHolder(name, String.valueOf(value));
        }

        @CanIgnoreReturnValue
        public ToStringHelper add(String name, double value) {
            return this.addUnconditionalHolder(name, String.valueOf(value));
        }

        @CanIgnoreReturnValue
        public ToStringHelper add(String name, float value) {
            return this.addUnconditionalHolder(name, String.valueOf(value));
        }

        @CanIgnoreReturnValue
        public ToStringHelper add(String name, int value) {
            return this.addUnconditionalHolder(name, String.valueOf(value));
        }

        @CanIgnoreReturnValue
        public ToStringHelper add(String name, long value) {
            return this.addUnconditionalHolder(name, String.valueOf(value));
        }

        @CanIgnoreReturnValue
        public ToStringHelper addValue(@Nullable Object value) {
            return this.addHolder(value);
        }

        @CanIgnoreReturnValue
        public ToStringHelper addValue(boolean value) {
            return this.addUnconditionalHolder(String.valueOf(value));
        }

        @CanIgnoreReturnValue
        public ToStringHelper addValue(char value) {
            return this.addUnconditionalHolder(String.valueOf(value));
        }

        @CanIgnoreReturnValue
        public ToStringHelper addValue(double value) {
            return this.addUnconditionalHolder(String.valueOf(value));
        }

        @CanIgnoreReturnValue
        public ToStringHelper addValue(float value) {
            return this.addUnconditionalHolder(String.valueOf(value));
        }

        @CanIgnoreReturnValue
        public ToStringHelper addValue(int value) {
            return this.addUnconditionalHolder(String.valueOf(value));
        }

        @CanIgnoreReturnValue
        public ToStringHelper addValue(long value) {
            return this.addUnconditionalHolder(String.valueOf(value));
        }

        private static boolean isEmpty(Object value) {
            if (value instanceof CharSequence) {
                return ((CharSequence)value).length() == 0;
            }
            if (value instanceof Collection) {
                return ((Collection)value).isEmpty();
            }
            if (value instanceof Map) {
                return ((Map)value).isEmpty();
            }
            if (value instanceof java.util.Optional) {
                return !((java.util.Optional)value).isPresent();
            }
            if (value instanceof OptionalInt) {
                return !((OptionalInt)value).isPresent();
            }
            if (value instanceof OptionalLong) {
                return !((OptionalLong)value).isPresent();
            }
            if (value instanceof OptionalDouble) {
                return !((OptionalDouble)value).isPresent();
            }
            if (value instanceof Optional) {
                return !((Optional)value).isPresent();
            }
            if (value.getClass().isArray()) {
                return Array.getLength(value) == 0;
            }
            return false;
        }

        public String toString() {
            boolean omitNullValuesSnapshot = this.omitNullValues;
            boolean omitEmptyValuesSnapshot = this.omitEmptyValues;
            String nextSeparator = "";
            StringBuilder builder = new StringBuilder(32).append(this.className).append('{');
            ValueHolder valueHolder = this.holderHead.next;
            while (valueHolder != null) {
                Object value = valueHolder.value;
                if (valueHolder instanceof UnconditionalValueHolder || (value == null ? !omitNullValuesSnapshot : !omitEmptyValuesSnapshot || !ToStringHelper.isEmpty(value))) {
                    builder.append(nextSeparator);
                    nextSeparator = ", ";
                    if (valueHolder.name != null) {
                        builder.append(valueHolder.name).append('=');
                    }
                    if (value != null && value.getClass().isArray()) {
                        Object[] objectArray = new Object[]{value};
                        String arrayString = Arrays.deepToString(objectArray);
                        builder.append(arrayString, 1, arrayString.length() - 1);
                    } else {
                        builder.append(value);
                    }
                }
                valueHolder = valueHolder.next;
            }
            return builder.append('}').toString();
        }

        private ValueHolder addHolder() {
            ValueHolder valueHolder;
            this.holderTail = this.holderTail.next = (valueHolder = new ValueHolder());
            return valueHolder;
        }

        @CanIgnoreReturnValue
        private ToStringHelper addHolder(@Nullable Object value) {
            ValueHolder valueHolder = this.addHolder();
            valueHolder.value = value;
            return this;
        }

        @CanIgnoreReturnValue
        private ToStringHelper addHolder(String name, @Nullable Object value) {
            ValueHolder valueHolder = this.addHolder();
            valueHolder.value = value;
            valueHolder.name = Preconditions.checkNotNull(name);
            return this;
        }

        private UnconditionalValueHolder addUnconditionalHolder() {
            UnconditionalValueHolder valueHolder = new UnconditionalValueHolder();
            this.holderTail = this.holderTail.next = valueHolder;
            return valueHolder;
        }

        @CanIgnoreReturnValue
        private ToStringHelper addUnconditionalHolder(Object value) {
            UnconditionalValueHolder valueHolder = this.addUnconditionalHolder();
            valueHolder.value = value;
            return this;
        }

        @CanIgnoreReturnValue
        private ToStringHelper addUnconditionalHolder(String name, Object value) {
            UnconditionalValueHolder valueHolder = this.addUnconditionalHolder();
            valueHolder.value = value;
            valueHolder.name = Preconditions.checkNotNull(name);
            return this;
        }

        static class ValueHolder {
            @Nullable String name;
            @Nullable Object value;
            @Nullable ValueHolder next;

            ValueHolder() {
            }
        }

        private static final class UnconditionalValueHolder
        extends ValueHolder {
            private UnconditionalValueHolder() {
            }
        }
    }
}

