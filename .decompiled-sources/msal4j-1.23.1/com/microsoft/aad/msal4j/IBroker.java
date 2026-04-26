/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.AccountCacheEntity;
import com.microsoft.aad.msal4j.AuthenticationResult;
import com.microsoft.aad.msal4j.Authority;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IdToken;
import com.microsoft.aad.msal4j.InteractiveRequestParameters;
import com.microsoft.aad.msal4j.JsonHelper;
import com.microsoft.aad.msal4j.MsalClientException;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.SilentParameters;
import com.microsoft.aad.msal4j.UserNamePasswordParameters;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public interface IBroker {
    default public CompletableFuture<IAuthenticationResult> acquireToken(PublicClientApplication application, SilentParameters requestParameters) {
        throw new MsalClientException("Broker implementation missing", "missing_broker");
    }

    default public CompletableFuture<IAuthenticationResult> acquireToken(PublicClientApplication application, InteractiveRequestParameters parameters) {
        throw new MsalClientException("Broker implementation missing", "missing_broker");
    }

    default public CompletableFuture<IAuthenticationResult> acquireToken(PublicClientApplication application, UserNamePasswordParameters parameters) {
        throw new MsalClientException("Broker implementation missing", "missing_broker");
    }

    default public void removeAccount(PublicClientApplication application, IAccount account) throws MsalClientException {
        throw new MsalClientException("Broker implementation missing", "missing_broker");
    }

    default public boolean isBrokerAvailable() {
        throw new MsalClientException("Broker implementation missing", "missing_broker");
    }

    default public IAuthenticationResult parseBrokerAuthResult(String authority, String idToken, String accessToken, String accountId, String clientInfo, long accessTokenExpirationTime, boolean isPopAuthorization) {
        AuthenticationResult.AuthenticationResultBuilder builder = AuthenticationResult.builder();
        try {
            if (idToken != null) {
                builder.idToken(idToken);
                if (accountId != null) {
                    IdToken idTokenObj = JsonHelper.createIdTokenFromEncodedTokenString(idToken);
                    builder.accountCacheEntity(AccountCacheEntity.create(clientInfo, Authority.createAuthority(new URL(authority)), idTokenObj, null));
                }
            }
            if (accessToken != null) {
                builder.accessToken(accessToken);
                builder.expiresOn(accessTokenExpirationTime);
            }
            builder.isPopAuthorization(isPopAuthorization);
        }
        catch (Exception e) {
            throw new MsalClientException(String.format("Exception when converting broker result to MSAL Java AuthenticationResult: %s", e.getMessage()), "brokers_package_error");
        }
        return builder.build();
    }
}

