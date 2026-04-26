/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.message2;

import com.ibm.icu.text.ConstrainedFieldPosition;
import com.ibm.icu.text.FormattedValue;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.AttributedCharacterIterator;

@Deprecated
public class PlainStringFormattedValue
implements FormattedValue {
    private final String value;

    @Deprecated
    public PlainStringFormattedValue(String value) {
        if (value == null) {
            throw new IllegalAccessError("Should not try to wrap a null in a formatted value");
        }
        this.value = value;
    }

    @Override
    @Deprecated
    public int length() {
        return this.value == null ? 0 : this.value.length();
    }

    @Override
    @Deprecated
    public char charAt(int index) {
        return this.value.charAt(index);
    }

    @Override
    @Deprecated
    public CharSequence subSequence(int start, int end) {
        return this.value.subSequence(start, end);
    }

    @Override
    @Deprecated
    public <A extends Appendable> A appendTo(A appendable) {
        try {
            appendable.append(this.value);
        }
        catch (IOException e) {
            throw new UncheckedIOException("problem appending", e);
        }
        return appendable;
    }

    @Override
    @Deprecated
    public boolean nextPosition(ConstrainedFieldPosition cfpos) {
        throw new RuntimeException("nextPosition not yet implemented");
    }

    @Override
    @Deprecated
    public AttributedCharacterIterator toCharacterIterator() {
        throw new RuntimeException("toCharacterIterator not yet implemented");
    }

    @Override
    @Deprecated
    public String toString() {
        return this.value;
    }
}

