/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.base;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.errorprone.annotations.DoNotMock;

@DoNotMock(value="Use an instance of one of the Finalizable*Reference classes")
@J2ktIncompatible
@GwtIncompatible
public interface FinalizableReference {
    public void finalizeReferent();
}

