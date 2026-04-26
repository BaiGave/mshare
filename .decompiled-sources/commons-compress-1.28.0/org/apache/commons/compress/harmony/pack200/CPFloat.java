/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.pack200;

import org.apache.commons.compress.harmony.pack200.CPConstant;

public class CPFloat
extends CPConstant<CPFloat> {
    private final float value;

    public CPFloat(float value) {
        this.value = value;
    }

    @Override
    public int compareTo(CPFloat obj) {
        return Float.compare(this.value, obj.value);
    }

    public float getFloat() {
        return this.value;
    }
}

