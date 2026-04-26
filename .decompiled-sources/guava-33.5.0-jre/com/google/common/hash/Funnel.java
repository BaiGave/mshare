/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.hash;

import com.google.common.annotations.Beta;
import com.google.common.hash.ParametricNullness;
import com.google.common.hash.PrimitiveSink;
import com.google.errorprone.annotations.DoNotMock;
import java.io.Serializable;

@DoNotMock(value="Implement with a lambda")
@Beta
public interface Funnel<T>
extends Serializable {
    public void funnel(@ParametricNullness T var1, PrimitiveSink var2);
}

