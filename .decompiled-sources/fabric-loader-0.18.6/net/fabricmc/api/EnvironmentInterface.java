/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.EnvironmentInterfaces;

@Retention(value=RetentionPolicy.CLASS)
@Repeatable(value=EnvironmentInterfaces.class)
@Target(value={ElementType.TYPE})
@Documented
public @interface EnvironmentInterface {
    public EnvType value();

    public Class<?> itf();
}

