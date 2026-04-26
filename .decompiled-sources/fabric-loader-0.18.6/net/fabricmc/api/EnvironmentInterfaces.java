/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.fabricmc.api.EnvironmentInterface;

@Retention(value=RetentionPolicy.CLASS)
@Target(value={ElementType.TYPE})
@Documented
public @interface EnvironmentInterfaces {
    public EnvironmentInterface[] value();
}

