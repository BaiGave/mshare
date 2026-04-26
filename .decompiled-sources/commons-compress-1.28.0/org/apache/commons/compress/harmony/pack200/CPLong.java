/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.pack200;

import org.apache.commons.compress.harmony.pack200.CPConstant;

public class CPLong
extends CPConstant<CPLong> {
    private final long value;

    public CPLong(long value) {
        this.value = value;
    }

    @Override
    public int compareTo(CPLong obj) {
        return Long.compare(this.value, obj.value);
    }

    public long getLong() {
        return this.value;
    }

    public String toString() {
        return "" + this.value;
    }
}

