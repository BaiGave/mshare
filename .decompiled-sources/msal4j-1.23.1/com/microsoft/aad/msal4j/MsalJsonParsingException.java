/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.ManagedIdentitySourceType;
import com.microsoft.aad.msal4j.MsalServiceException;

public class MsalJsonParsingException
extends MsalServiceException {
    MsalJsonParsingException(String message, String error) {
        super(message, error);
    }

    MsalJsonParsingException(String message, String error, ManagedIdentitySourceType managedIdentitySource) {
        super(message, error, managedIdentitySource);
    }
}

