/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.AbstractClientApplicationBase;
import com.microsoft.aad.msal4j.AppTokenProviderParameters;
import com.microsoft.aad.msal4j.AuthenticationResult;
import com.microsoft.aad.msal4j.AuthenticationResultMetadata;
import com.microsoft.aad.msal4j.AuthenticationResultSupplier;
import com.microsoft.aad.msal4j.ClientCredentialRequest;
import com.microsoft.aad.msal4j.MsalAzureSDKException;
import com.microsoft.aad.msal4j.MsalClientException;
import com.microsoft.aad.msal4j.TokenProviderResult;
import com.microsoft.aad.msal4j.TokenRequestExecutor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

class AcquireTokenByAppProviderSupplier
extends AuthenticationResultSupplier {
    private static final int TWO_HOURS = 7200;
    private AppTokenProviderParameters appTokenProviderParameters;
    private ClientCredentialRequest clientCredentialRequest;

    AcquireTokenByAppProviderSupplier(AbstractClientApplicationBase clientApplication, ClientCredentialRequest clientCredentialRequest, AppTokenProviderParameters appTokenProviderParameters) {
        super(clientApplication, clientCredentialRequest);
        this.clientCredentialRequest = clientCredentialRequest;
        this.appTokenProviderParameters = appTokenProviderParameters;
    }

    private static void validateAndUpdateTokenProviderResult(TokenProviderResult tokenProviderResult) {
        long expireInSeconds;
        if (null == tokenProviderResult.getAccessToken() || tokenProviderResult.getAccessToken().isEmpty()) {
            AcquireTokenByAppProviderSupplier.handleInvalidExternalValueError(tokenProviderResult.getAccessToken());
        }
        if (tokenProviderResult.getExpiresInSeconds() == 0L || tokenProviderResult.getExpiresInSeconds() < 0L) {
            AcquireTokenByAppProviderSupplier.handleInvalidExternalValueError(Long.valueOf(tokenProviderResult.getExpiresInSeconds()).toString());
        }
        if (null == tokenProviderResult.getTenantId() || tokenProviderResult.getTenantId().isEmpty()) {
            AcquireTokenByAppProviderSupplier.handleInvalidExternalValueError(tokenProviderResult.getTenantId());
        }
        if (0L == tokenProviderResult.getRefreshInSeconds() && (expireInSeconds = tokenProviderResult.getExpiresInSeconds()) >= 7200L) {
            tokenProviderResult.setRefreshInSeconds(expireInSeconds / 2L);
        }
    }

    private static void handleInvalidExternalValueError(String nameOfValue) {
        throw new MsalClientException("The following token provider result value is invalid" + nameOfValue, "Invalid_TokenProviderResult_Input");
    }

    @Override
    AuthenticationResult execute() throws Exception {
        AuthenticationResult authenticationResult = this.fetchTokenUsingAppTokenProvider(this.appTokenProviderParameters);
        TokenRequestExecutor tokenRequestExecutor = new TokenRequestExecutor(this.clientCredentialRequest.application().authenticationAuthority, this.msalRequest, this.clientApplication.serviceBundle());
        this.clientApplication.tokenCache.saveTokens(tokenRequestExecutor, authenticationResult, this.clientCredentialRequest.application().authenticationAuthority.host);
        return authenticationResult;
    }

    public AuthenticationResult fetchTokenUsingAppTokenProvider(AppTokenProviderParameters appTokenProviderParameters) throws ExecutionException, InterruptedException {
        TokenProviderResult tokenProviderResult;
        try {
            CompletableFuture<TokenProviderResult> completableFuture = this.clientCredentialRequest.appTokenProvider.apply(appTokenProviderParameters);
            tokenProviderResult = completableFuture.get();
        }
        catch (Exception ex) {
            throw new MsalAzureSDKException(ex);
        }
        AcquireTokenByAppProviderSupplier.validateAndUpdateTokenProviderResult(tokenProviderResult);
        return AuthenticationResult.builder().accessToken(tokenProviderResult.getAccessToken()).refreshToken(null).idToken(null).expiresOn(tokenProviderResult.getExpiresInSeconds()).refreshOn(tokenProviderResult.getRefreshInSeconds()).metadata(AuthenticationResultMetadata.builder().refreshOn(tokenProviderResult.getRefreshInSeconds() > 0L ? tokenProviderResult.getRefreshInSeconds() : 0L).build()).build();
    }
}

