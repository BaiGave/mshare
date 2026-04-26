/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.ExtraObjectsMethodsForWeb;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public final class Objects
extends ExtraObjectsMethodsForWeb {
    public static boolean equal(@Nullable Object a, @Nullable Object b) {
        return java.util.Objects.equals(a, b);
    }

    public static int hashCode(Object ... objects) {
        return java.util.Objects.hash(objects);
    }

    private Objects() {
    }
}

