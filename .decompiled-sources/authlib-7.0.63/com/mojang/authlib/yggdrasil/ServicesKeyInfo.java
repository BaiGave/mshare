/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib.yggdrasil;

import com.mojang.authlib.properties.Property;
import java.security.Signature;

public interface ServicesKeyInfo {
    public int keyBitCount();

    default public int signatureBitCount() {
        return this.keyBitCount();
    }

    public Signature signature();

    public boolean validateProperty(Property var1);
}

