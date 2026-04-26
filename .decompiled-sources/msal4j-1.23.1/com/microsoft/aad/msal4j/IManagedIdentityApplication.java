/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.IApplicationBase;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.ManagedIdentityParameters;
import java.util.concurrent.CompletableFuture;

public interface IManagedIdentityApplication
extends IApplicationBase {
    public CompletableFuture<IAuthenticationResult> acquireTokenForManagedIdentity(ManagedIdentityParameters var1) throws Exception;
}

