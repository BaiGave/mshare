/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.pack200;

import org.apache.commons.compress.harmony.pack200.CPConstant;

public class CPDouble
extends CPConstant<CPDouble> {
    private final double value;

    public CPDouble(double value) {
        this.value = value;
    }

    @Override
    public int compareTo(CPDouble obj) {
        return Double.compare(this.value, obj.value);
    }

    public double getDouble() {
        return this.value;
    }
}

