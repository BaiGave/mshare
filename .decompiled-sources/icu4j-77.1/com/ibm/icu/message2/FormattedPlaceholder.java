/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.message2;

import com.ibm.icu.message2.Directionality;
import com.ibm.icu.text.FormattedValue;

@Deprecated
public class FormattedPlaceholder {
    private final FormattedValue formattedValue;
    private final Object inputValue;
    private final Directionality directionality;
    private final boolean isolate;

    @Deprecated
    public FormattedPlaceholder(Object inputValue, FormattedValue formattedValue, Directionality directionality, boolean isolate) {
        if (formattedValue == null) {
            throw new IllegalAccessError("Should not try to wrap a null formatted value");
        }
        this.inputValue = inputValue;
        this.formattedValue = formattedValue;
        this.directionality = directionality;
        this.isolate = isolate;
    }

    @Deprecated
    public FormattedPlaceholder(Object inputValue, FormattedValue formattedValue) {
        this(inputValue, formattedValue, Directionality.LTR, false);
    }

    @Deprecated
    public Object getInput() {
        return this.inputValue;
    }

    @Deprecated
    public FormattedValue getFormattedValue() {
        return this.formattedValue;
    }

    @Deprecated
    public Directionality getDirectionality() {
        return this.directionality;
    }

    @Deprecated
    public boolean getIsolate() {
        return this.isolate;
    }

    @Deprecated
    public String toString() {
        return this.formattedValue.toString();
    }
}

