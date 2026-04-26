/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.errorprone.annotations.DoNotMock;

@DoNotMock(value="Use Interners.new*Interner")
@J2ktIncompatible
@GwtIncompatible
public interface Interner<E> {
    public E intern(E var1);
}

