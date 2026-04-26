/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.realmsclient.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.FIELD})
@Environment(value=EnvType.CLIENT)
public @interface Exclude {
}

