/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl.breakiter;

enum ModelIndex {
    kUWStart(0),
    kBWStart(6),
    kTWStart(9);

    private final int value;

    private ModelIndex(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}

