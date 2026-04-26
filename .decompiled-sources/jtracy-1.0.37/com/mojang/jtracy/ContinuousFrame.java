/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.jtracy;

import com.mojang.jtracy.TracyBindings;

public class ContinuousFrame {
    static final ContinuousFrame UNAVAILABLE = new ContinuousFrame(0L);
    private final long id;

    ContinuousFrame(long id) {
        this.id = id;
    }

    public void mark() {
        if (this != UNAVAILABLE) {
            TracyBindings.markFrame(this.id);
        }
    }
}

