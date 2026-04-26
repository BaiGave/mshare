/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.DeviceCodeFlowParameters;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientApplicationBase;
import com.microsoft.aad.msal4j.IntegratedWindowsAuthenticationParameters;
import com.microsoft.aad.msal4j.InteractiveRequestParameters;
import com.microsoft.aad.msal4j.UserNamePasswordParameters;
import java.util.concurrent.CompletableFuture;

public interface IPublicClientApplication
extends IClientApplicationBase {
    public CompletableFuture<IAuthenticationResult> acquireToken(UserNamePasswordParameters var1);

    public CompletableFuture<IAuthenticationResult> acquireToken(IntegratedWindowsAuthenticationParameters var1);

    public CompletableFuture<IAuthenticationResult> acquireToken(DeviceCodeFlowParameters var1);

    public CompletableFuture<IAuthenticationResult> acquireToken(InteractiveRequestParameters var1);
}

