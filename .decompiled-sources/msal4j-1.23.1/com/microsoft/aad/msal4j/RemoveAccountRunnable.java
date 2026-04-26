/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.AbstractApplicationBase;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.LogHelper;
import com.microsoft.aad.msal4j.MsalRequest;
import com.microsoft.aad.msal4j.RequestContext;
import java.util.concurrent.CompletionException;

class RemoveAccountRunnable
implements Runnable {
    private RequestContext requestContext;
    private AbstractApplicationBase clientApplication;
    IAccount account;

    RemoveAccountRunnable(MsalRequest msalRequest, IAccount account) {
        this.clientApplication = msalRequest.application();
        this.requestContext = msalRequest.requestContext();
        this.account = account;
    }

    @Override
    public void run() {
        try {
            this.clientApplication.tokenCache.removeAccount(this.clientApplication.clientId(), this.account);
        }
        catch (Exception ex) {
            this.clientApplication.log.warn(LogHelper.createMessage(String.format("Execution of %s failed: %s", this.getClass(), ex.getMessage()), this.requestContext.correlationId()));
            throw new CompletionException(ex);
        }
    }
}

