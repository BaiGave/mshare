/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.jtracy;

import com.mojang.jtracy.TracyBindings;

public class DiscontinuousFrame {
    static final DiscontinuousFrame UNAVAILABLE = new DiscontinuousFrame(0L);
    private final long id;

    DiscontinuousFrame(long id) {
        this.id = id;
    }

    public void start() {
        if (this != UNAVAILABLE) {
            TracyBindings.markFrameStart(this.id);
        }
    }

    public void end() {
        if (this != UNAVAILABLE) {
            TracyBindings.markFrameEnd(this.id);
        }
    }
}

