/*
 * Decompiled with CFR 0.152.
 */
package org.spongepowered.asm.mixin.injection.callback;

public class CancellationException
extends RuntimeException {
    public CancellationException() {
    }

    public CancellationException(String message) {
        super(message);
    }

    public CancellationException(Throwable cause) {
        super(cause);
    }

    public CancellationException(String message, Throwable cause) {
        super(message, cause);
    }
}

