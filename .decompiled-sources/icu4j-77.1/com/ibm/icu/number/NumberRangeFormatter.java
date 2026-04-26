/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.number;

import com.ibm.icu.number.LocalizedNumberRangeFormatter;
import com.ibm.icu.number.UnlocalizedNumberRangeFormatter;
import com.ibm.icu.text.UFormat;
import com.ibm.icu.util.ULocale;
import java.io.InvalidObjectException;
import java.util.Locale;

public abstract class NumberRangeFormatter {
    private static final UnlocalizedNumberRangeFormatter BASE = new UnlocalizedNumberRangeFormatter();

    public static UnlocalizedNumberRangeFormatter with() {
        return BASE;
    }

    public static LocalizedNumberRangeFormatter withLocale(Locale locale) {
        return BASE.locale(locale);
    }

    public static LocalizedNumberRangeFormatter withLocale(ULocale locale) {
        return BASE.locale(locale);
    }

    private NumberRangeFormatter() {
    }

    public static final class SpanField
    extends UFormat.SpanField {
        private static final long serialVersionUID = 8750397196515368729L;
        public static final SpanField NUMBER_RANGE_SPAN = new SpanField("number-range-span");

        private SpanField(String name) {
            super(name);
        }

        @Override
        @Deprecated
        protected Object readResolve() throws InvalidObjectException {
            if (this.getName().equals(NUMBER_RANGE_SPAN.getName())) {
                return NUMBER_RANGE_SPAN;
            }
            throw new InvalidObjectException("An invalid object.");
        }
    }

    public static enum RangeIdentityResult {
        EQUAL_BEFORE_ROUNDING,
        EQUAL_AFTER_ROUNDING,
        NOT_EQUAL;

    }

    public static enum RangeIdentityFallback {
        SINGLE_VALUE,
        APPROXIMATELY_OR_SINGLE_VALUE,
        APPROXIMATELY,
        RANGE;

    }

    public static enum RangeCollapse {
        AUTO,
        NONE,
        UNIT,
        ALL;

    }
}

