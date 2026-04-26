/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.escape;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Function;
import com.google.errorprone.annotations.DoNotMock;

@DoNotMock(value="Use Escapers.nullEscaper() or another methods from the *Escapers classes")
@GwtCompatible
public abstract class Escaper {
    private final Function<String, String> asFunction = this::escape;

    protected Escaper() {
    }

    public abstract String escape(String var1);

    public final Function<String, String> asFunction() {
        return this.asFunction;
    }
}

