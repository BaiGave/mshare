/*
 * Decompiled with CFR 0.152.
 */
package com.llamalad7.mixinextras.sugar.impl.ref;

public class LocalRefRuntime {
    static final byte UNINITIALIZED = 1;
    static final byte DISPOSED = 2;

    public static void checkState(byte state) {
        switch (state) {
            case 0: {
                return;
            }
            case 1: {
                throw new IllegalStateException("Use of an uninitialized LocalRef! This should never happen! Please report to LlamaLad7!");
            }
            case 2: {
                throw new IllegalStateException("Use of a disposed LocalRef! You cannot retain these objects past the handler method they were passed to. If you don't think this applies to you then please report your issue to LlamaLad7 as it may be a bug.");
            }
        }
        throw new IllegalStateException(String.format("Unknown LocalRef state %s?", state));
    }

    public static String localRefToString(String type, String value, byte state) {
        switch (state) {
            case 0: {
                return String.format("%s[value=%s]", type, value);
            }
            case 1: {
                return String.format("%s[state=UNINITIALIZED]", type);
            }
            case 2: {
                return String.format("%s[state=DISPOSED]", type);
            }
        }
        throw new IllegalStateException(String.format("Unknown LocalRef state %s?", state));
    }
}

