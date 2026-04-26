/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.util;

import com.ibm.icu.util.ICUException;

public class ICUInputTooLongException
extends ICUException {
    private static final long serialVersionUID = -2602876786689338226L;

    public ICUInputTooLongException() {
    }

    public ICUInputTooLongException(String message) {
        super(message);
    }

    public ICUInputTooLongException(Throwable cause) {
        super(cause);
    }

    public ICUInputTooLongException(String message, Throwable cause) {
        super(message, cause);
    }
}

