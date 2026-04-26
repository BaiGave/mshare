/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.IClientCredential;

public interface IClientAssertion
extends IClientCredential {
    public String assertion();
}

