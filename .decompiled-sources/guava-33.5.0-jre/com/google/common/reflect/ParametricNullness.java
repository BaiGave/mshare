/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.reflect;

import com.google.common.annotations.GwtCompatible;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value=RetentionPolicy.CLASS)
@Target(value={ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@GwtCompatible
@interface ParametricNullness {
}

