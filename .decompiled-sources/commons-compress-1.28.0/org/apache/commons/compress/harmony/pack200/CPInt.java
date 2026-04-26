/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.pack200;

import org.apache.commons.compress.harmony.pack200.CPConstant;

public class CPInt
extends CPConstant<CPInt> {
    private final int value;

    public CPInt(int value) {
        this.value = value;
    }

    @Override
    public int compareTo(CPInt obj) {
        return Integer.compare(this.value, obj.value);
    }

    public int getInt() {
        return this.value;
    }
}

