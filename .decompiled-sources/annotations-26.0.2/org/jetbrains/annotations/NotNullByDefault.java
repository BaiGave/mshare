/*
 * Decompiled with CFR 0.152.
 */
package org.jetbrains.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jetbrains.annotations.ApiStatus;

@Documented
@Retention(value=RetentionPolicy.CLASS)
@Target(value={ElementType.TYPE, ElementType.PACKAGE})
@ApiStatus.Experimental
public @interface NotNullByDefault {
}

