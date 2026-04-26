/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib;

import java.util.UUID;

public interface ProfileLookupCallback {
    public void onProfileLookupSucceeded(String var1, UUID var2);

    public void onProfileLookupFailed(String var1, Exception var2);
}

