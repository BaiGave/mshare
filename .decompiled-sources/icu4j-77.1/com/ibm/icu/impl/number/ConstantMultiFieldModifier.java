/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl.number;

import com.ibm.icu.impl.FormattedStringBuilder;
import com.ibm.icu.impl.number.Modifier;
import java.text.Format;
import java.util.Arrays;

public class ConstantMultiFieldModifier
implements Modifier {
    protected final char[] prefixChars;
    protected final char[] suffixChars;
    protected final Object[] prefixFields;
    protected final Object[] suffixFields;
    private final boolean overwrite;
    private final boolean strong;
    private final Modifier.Parameters parameters;

    public ConstantMultiFieldModifier(FormattedStringBuilder prefix, FormattedStringBuilder suffix, boolean overwrite, boolean strong) {
        this(prefix, suffix, overwrite, strong, null);
    }

    public ConstantMultiFieldModifier(FormattedStringBuilder prefix, FormattedStringBuilder suffix, boolean overwrite, boolean strong, Modifier.Parameters parameters) {
        this.prefixChars = prefix.toCharArray();
        this.suffixChars = suffix.toCharArray();
        this.prefixFields = prefix.toFieldArray();
        this.suffixFields = suffix.toFieldArray();
        this.overwrite = overwrite;
        this.strong = strong;
        this.parameters = parameters;
    }

    @Override
    public int apply(FormattedStringBuilder output, int leftIndex, int rightIndex) {
        int length = output.insert(leftIndex, this.prefixChars, this.prefixFields);
        if (this.overwrite) {
            length += output.splice(leftIndex + length, rightIndex + length, "", 0, 0, null);
        }
        length += output.insert(rightIndex + length, this.suffixChars, this.suffixFields);
        return length;
    }

    @Override
    public int getPrefixLength() {
        return this.prefixChars.length;
    }

    @Override
    public int getCodePointCount() {
        return Character.codePointCount(this.prefixChars, 0, this.prefixChars.length) + Character.codePointCount(this.suffixChars, 0, this.suffixChars.length);
    }

    @Override
    public boolean isStrong() {
        return this.strong;
    }

    @Override
    public boolean containsField(Format.Field field) {
        int i;
        for (i = 0; i < this.prefixFields.length; ++i) {
            if (this.prefixFields[i] != field) continue;
            return true;
        }
        for (i = 0; i < this.suffixFields.length; ++i) {
            if (this.suffixFields[i] != field) continue;
            return true;
        }
        return false;
    }

    @Override
    public Modifier.Parameters getParameters() {
        return this.parameters;
    }

    @Override
    public boolean strictEquals(Modifier other) {
        if (!(other instanceof ConstantMultiFieldModifier)) {
            return false;
        }
        ConstantMultiFieldModifier _other = (ConstantMultiFieldModifier)other;
        return Arrays.equals(this.prefixChars, _other.prefixChars) && Arrays.equals(this.prefixFields, _other.prefixFields) && Arrays.equals(this.suffixChars, _other.suffixChars) && Arrays.equals(this.suffixFields, _other.suffixFields) && this.overwrite == _other.overwrite && this.strong == _other.strong;
    }

    public String toString() {
        FormattedStringBuilder temp = new FormattedStringBuilder();
        this.apply(temp, 0, 0);
        int prefixLength = this.getPrefixLength();
        return String.format("<ConstantMultiFieldModifier prefix:'%s' suffix:'%s'>", temp.subSequence(0, prefixLength), temp.subSequence(prefixLength, temp.length()));
    }
}

