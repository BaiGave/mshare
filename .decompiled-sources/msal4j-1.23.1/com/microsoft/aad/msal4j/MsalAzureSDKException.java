/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.MsalException;

public class MsalAzureSDKException
extends MsalException {
    public MsalAzureSDKException(Throwable throwable) {
        super(throwable);
    }

    public MsalAzureSDKException(String message, String errorCode) {
        super(message, errorCode);
    }
}

