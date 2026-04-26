/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.MsalException;

public class MsalClientException
extends MsalException {
    public MsalClientException(Throwable throwable) {
        super(throwable);
    }

    public MsalClientException(String message, String errorCode) {
        super(message, errorCode);
    }
}

