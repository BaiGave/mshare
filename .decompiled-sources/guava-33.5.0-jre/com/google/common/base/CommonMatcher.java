/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.base;

import com.google.common.annotations.GwtCompatible;

@GwtCompatible
abstract class CommonMatcher {
    CommonMatcher() {
    }

    public abstract boolean matches();

    public abstract boolean find();

    public abstract boolean find(int var1);

    public abstract String replaceAll(String var1);

    public abstract int end();

    public abstract int start();
}

