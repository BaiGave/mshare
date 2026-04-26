/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.number;

import com.ibm.icu.impl.number.MacroProps;
import com.ibm.icu.impl.number.Padder;
import com.ibm.icu.number.IntegerWidth;
import com.ibm.icu.number.Notation;
import com.ibm.icu.number.NumberFormatter;
import com.ibm.icu.number.NumberSkeletonImpl;
import com.ibm.icu.number.Precision;
import com.ibm.icu.number.Scale;
import com.ibm.icu.text.DecimalFormatSymbols;
import com.ibm.icu.text.DisplayOptions;
import com.ibm.icu.text.NumberingSystem;
import com.ibm.icu.util.MeasureUnit;
import com.ibm.icu.util.ULocale;
import java.math.RoundingMode;

public abstract class NumberFormatterSettings<T extends NumberFormatterSettings<?>> {
    static final int KEY_MACROS = 0;
    static final int KEY_LOCALE = 1;
    static final int KEY_NOTATION = 2;
    static final int KEY_UNIT = 3;
    static final int KEY_PRECISION = 4;
    static final int KEY_ROUNDING_MODE = 5;
    static final int KEY_GROUPING = 6;
    static final int KEY_PADDER = 7;
    static final int KEY_INTEGER = 8;
    static final int KEY_SYMBOLS = 9;
    static final int KEY_UNIT_WIDTH = 10;
    static final int KEY_SIGN = 11;
    static final int KEY_DECIMAL = 12;
    static final int KEY_SCALE = 13;
    static final int KEY_THRESHOLD = 14;
    static final int KEY_PER_UNIT = 15;
    static final int KEY_USAGE = 16;
    static final int KEY_UNIT_DISPLAY_CASE = 17;
    static final int KEY_MAX = 18;
    private final NumberFormatterSettings<?> parent;
    private final int key;
    private final Object value;
    private volatile MacroProps resolvedMacros;

    NumberFormatterSettings(NumberFormatterSettings<?> parent, int key, Object value) {
        this.parent = parent;
        this.key = key;
        this.value = value;
    }

    public T notation(Notation notation) {
        return this.create(2, notation);
    }

    public T unit(MeasureUnit unit) {
        return this.create(3, unit);
    }

    public T perUnit(MeasureUnit perUnit) {
        return this.create(15, perUnit);
    }

    public T precision(Precision precision) {
        return this.create(4, precision);
    }

    public T roundingMode(RoundingMode roundingMode) {
        return this.create(5, (Object)roundingMode);
    }

    public T grouping(NumberFormatter.GroupingStrategy strategy) {
        return this.create(6, (Object)strategy);
    }

    public T integerWidth(IntegerWidth style) {
        return this.create(8, style);
    }

    public T symbols(DecimalFormatSymbols symbols) {
        symbols = (DecimalFormatSymbols)symbols.clone();
        return this.create(9, symbols);
    }

    public T symbols(NumberingSystem ns) {
        return this.create(9, ns);
    }

    public T unitWidth(NumberFormatter.UnitWidth style) {
        return this.create(10, (Object)style);
    }

    public T sign(NumberFormatter.SignDisplay style) {
        return this.create(11, (Object)style);
    }

    public T decimal(NumberFormatter.DecimalSeparatorDisplay style) {
        return this.create(12, (Object)style);
    }

    public T scale(Scale scale) {
        return this.create(13, scale);
    }

    public T usage(String usage) {
        if (usage != null && usage.isEmpty()) {
            return this.create(16, null);
        }
        return this.create(16, usage);
    }

    public T displayOptions(DisplayOptions displayOptions) {
        if (displayOptions.getGrammaticalCase() == DisplayOptions.GrammaticalCase.UNDEFINED) {
            return this.create(17, null);
        }
        return this.create(17, displayOptions.getGrammaticalCase().getIdentifier());
    }

    @Deprecated
    public T unitDisplayCase(String unitDisplayCase) {
        return this.create(17, unitDisplayCase);
    }

    @Deprecated
    public T macros(MacroProps macros) {
        return this.create(0, macros);
    }

    @Deprecated
    public T padding(Padder padder) {
        return this.create(7, padder);
    }

    @Deprecated
    public T threshold(Long threshold) {
        return this.create(14, threshold);
    }

    public String toSkeleton() {
        return NumberSkeletonImpl.generate(this.resolve());
    }

    abstract T create(int var1, Object var2);

    MacroProps resolve() {
        if (this.resolvedMacros != null) {
            return this.resolvedMacros;
        }
        MacroProps macros = new MacroProps();
        long seen = 0L;
        NumberFormatterSettings<?> current = this;
        while (current != null) {
            long keyBitmask = 1L << current.key;
            if (0L != (seen & keyBitmask)) {
                current = current.parent;
                continue;
            }
            seen |= keyBitmask;
            switch (current.key) {
                case 0: {
                    macros.fallback((MacroProps)current.value);
                    break;
                }
                case 1: {
                    macros.loc = (ULocale)current.value;
                    break;
                }
                case 2: {
                    macros.notation = (Notation)current.value;
                    break;
                }
                case 3: {
                    macros.unit = (MeasureUnit)current.value;
                    break;
                }
                case 4: {
                    macros.precision = (Precision)current.value;
                    break;
                }
                case 5: {
                    macros.roundingMode = (RoundingMode)((Object)current.value);
                    break;
                }
                case 6: {
                    macros.grouping = current.value;
                    break;
                }
                case 7: {
                    macros.padder = (Padder)current.value;
                    break;
                }
                case 8: {
                    macros.integerWidth = (IntegerWidth)current.value;
                    break;
                }
                case 9: {
                    macros.symbols = current.value;
                    break;
                }
                case 10: {
                    macros.unitWidth = (NumberFormatter.UnitWidth)((Object)current.value);
                    break;
                }
                case 11: {
                    macros.sign = (NumberFormatter.SignDisplay)((Object)current.value);
                    break;
                }
                case 12: {
                    macros.decimal = (NumberFormatter.DecimalSeparatorDisplay)((Object)current.value);
                    break;
                }
                case 13: {
                    macros.scale = (Scale)current.value;
                    break;
                }
                case 14: {
                    macros.threshold = (Long)current.value;
                    break;
                }
                case 15: {
                    macros.perUnit = (MeasureUnit)current.value;
                    break;
                }
                case 16: {
                    macros.usage = (String)current.value;
                    break;
                }
                case 17: {
                    macros.unitDisplayCase = (String)current.value;
                    break;
                }
                default: {
                    throw new AssertionError((Object)("Unknown key: " + current.key));
                }
            }
            current = current.parent;
        }
        this.resolvedMacros = macros;
        return macros;
    }

    public int hashCode() {
        return this.resolve().hashCode();
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof NumberFormatterSettings)) {
            return false;
        }
        return this.resolve().equals(((NumberFormatterSettings)other).resolve());
    }
}

