/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.base;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.CommonPattern;
import com.google.errorprone.annotations.RestrictedApi;

@GwtIncompatible
interface PatternCompiler {
    @RestrictedApi(explanation="PatternCompiler is an implementation detail of com.google.common.base", allowedOnPath=".*/com/google/common/base/.*")
    public CommonPattern compile(String var1);

    @RestrictedApi(explanation="PatternCompiler is an implementation detail of com.google.common.base", allowedOnPath=".*/com/google/common/base/.*")
    public boolean isPcreLike();
}

