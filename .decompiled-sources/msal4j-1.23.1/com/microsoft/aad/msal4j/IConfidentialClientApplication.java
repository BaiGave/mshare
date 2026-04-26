/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientApplicationBase;
import com.microsoft.aad.msal4j.OnBehalfOfParameters;
import java.util.concurrent.CompletableFuture;

public interface IConfidentialClientApplication
extends IClientApplicationBase {
    public boolean sendX5c();

    public CompletableFuture<IAuthenticationResult> acquireToken(ClientCredentialParameters var1);

    public CompletableFuture<IAuthenticationResult> acquireToken(OnBehalfOfParameters var1);
}

