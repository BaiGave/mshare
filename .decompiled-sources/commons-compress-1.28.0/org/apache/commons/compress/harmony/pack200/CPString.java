/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.pack200;

import org.apache.commons.compress.harmony.pack200.CPConstant;
import org.apache.commons.compress.harmony.pack200.CPUTF8;

public class CPString
extends CPConstant<CPString> {
    private final String string;
    private final CPUTF8 value;

    public CPString(CPUTF8 value) {
        this.value = value;
        this.string = value.getUnderlyingString();
    }

    @Override
    public int compareTo(CPString arg0) {
        return this.string.compareTo(arg0.string);
    }

    public int getIndexInCpUtf8() {
        return this.value.getIndex();
    }

    public String toString() {
        return this.string;
    }
}

