/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.AadInstanceDiscoveryProvider;
import com.microsoft.aad.msal4j.AbstractApplicationBase;
import com.microsoft.aad.msal4j.ApiEvent;
import com.microsoft.aad.msal4j.AuthenticationResult;
import com.microsoft.aad.msal4j.Authority;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.HttpHeaders;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.InstanceDiscoveryMetadataEntry;
import com.microsoft.aad.msal4j.LogHelper;
import com.microsoft.aad.msal4j.MsalClientException;
import com.microsoft.aad.msal4j.MsalException;
import com.microsoft.aad.msal4j.MsalRequest;
import com.microsoft.aad.msal4j.StringHelper;
import com.microsoft.aad.msal4j.TelemetryHelper;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

abstract class AuthenticationResultSupplier
implements Supplier<IAuthenticationResult> {
    AbstractApplicationBase clientApplication;
    MsalRequest msalRequest;

    AuthenticationResultSupplier(AbstractApplicationBase clientApplication, MsalRequest msalRequest) {
        this.clientApplication = clientApplication;
        this.msalRequest = msalRequest;
    }

    Authority getAuthorityWithPrefNetworkHost(String authority) throws MalformedURLException {
        URL authorityUrl = new URL(authority);
        if (this.msalRequest.requestContext().apiParameters().tenant() != null) {
            authorityUrl = new URL(authority.replace(Authority.getTenant(authorityUrl, Authority.detectAuthorityType(authorityUrl)), this.msalRequest.requestContext().apiParameters().tenant()));
        }
        InstanceDiscoveryMetadataEntry discoveryMetadataEntry = AadInstanceDiscoveryProvider.getMetadataEntry(authorityUrl, this.clientApplication.validateAuthority(), this.msalRequest, this.clientApplication.serviceBundle());
        URL updatedAuthorityUrl = new URL(authorityUrl.getProtocol(), discoveryMetadataEntry.preferredNetwork, authorityUrl.getPort(), authorityUrl.getFile());
        return Authority.createAuthority(updatedAuthorityUrl);
    }

    abstract AuthenticationResult execute() throws Exception;

    @Override
    public IAuthenticationResult get() {
        AuthenticationResult result;
        ApiEvent apiEvent = this.initializeApiEvent(this.msalRequest);
        try (TelemetryHelper telemetryHelper = this.clientApplication.serviceBundle().getTelemetryManager().createTelemetryHelper(this.msalRequest.requestContext().telemetryRequestId(), this.msalRequest.application().clientId(), apiEvent, true);){
            try {
                result = this.execute();
                apiEvent.setWasSuccessful(true);
                if (result != null) {
                    this.logResult(result, this.msalRequest.headers());
                    if (result.account() != null) {
                        apiEvent.setTenantId(result.accountCacheEntity().realm());
                    }
                }
            }
            catch (Exception ex) {
                String error = StringHelper.EMPTY_STRING;
                if (ex instanceof MsalException) {
                    MsalException exception = (MsalException)ex;
                    if (exception.errorCode() != null) {
                        apiEvent.setApiErrorCode(exception.errorCode());
                    }
                } else if (ex.getCause() != null) {
                    error = ex.getCause().toString();
                }
                this.clientApplication.serviceBundle().getServerSideTelemetry().addFailedRequestTelemetry(String.valueOf(this.msalRequest.requestContext().publicApi().getApiId()), this.msalRequest.requestContext().correlationId(), error);
                String logMessage = LogHelper.createMessage(String.format("Execution of %s failed: %s", this.getClass(), ex.getMessage()), this.msalRequest.headers().getHeaderCorrelationIdValue());
                if (ex instanceof MsalClientException) {
                    MsalClientException exception = (MsalClientException)ex;
                    if (exception.errorCode() != null && exception.errorCode().equalsIgnoreCase("cache_miss")) {
                        this.clientApplication.log.debug(logMessage);
                    }
                } else {
                    this.clientApplication.log.warn(logMessage);
                }
                throw new CompletionException(ex);
            }
        }
        return result;
    }

    private void logResult(AuthenticationResult result, HttpHeaders headers) {
        if (!StringHelper.isBlank(result.accessToken())) {
            String accessTokenHash = this.computeSha256Hash(result.accessToken());
            if (!StringHelper.isBlank(result.refreshToken())) {
                String refreshTokenHash = this.computeSha256Hash(result.refreshToken());
                if (this.clientApplication.logPii()) {
                    this.clientApplication.log.debug(LogHelper.createMessage(String.format("Access Token with hash '%s' and Refresh Token with hash '%s' returned", accessTokenHash, refreshTokenHash), headers.getHeaderCorrelationIdValue()));
                } else {
                    this.clientApplication.log.debug(LogHelper.createMessage("Access Token and Refresh Token were returned", headers.getHeaderCorrelationIdValue()));
                }
            } else if (this.clientApplication.logPii()) {
                this.clientApplication.log.debug(LogHelper.createMessage(String.format("Access Token with hash '%s' returned", accessTokenHash), headers.getHeaderCorrelationIdValue()));
            } else {
                this.clientApplication.log.debug(LogHelper.createMessage("Access Token was returned", headers.getHeaderCorrelationIdValue()));
            }
        }
    }

    private ApiEvent initializeApiEvent(MsalRequest msalRequest) {
        ApiEvent apiEvent = new ApiEvent(this.clientApplication.logPii());
        msalRequest.requestContext().telemetryRequestId(this.clientApplication.serviceBundle().getTelemetryManager().generateRequestId());
        apiEvent.setApiId(msalRequest.requestContext().publicApi().getApiId());
        apiEvent.setCorrelationId(msalRequest.requestContext().correlationId());
        apiEvent.setRequestId(msalRequest.requestContext().telemetryRequestId());
        apiEvent.setWasSuccessful(false);
        apiEvent.setIsConfidentialClient(this.clientApplication instanceof ConfidentialClientApplication);
        try {
            Authority authenticationAuthority = this.clientApplication.authenticationAuthority;
            if (authenticationAuthority != null) {
                apiEvent.setAuthority(new URI(authenticationAuthority.authority()));
                apiEvent.setAuthorityType(authenticationAuthority.authorityType().toString());
            }
        }
        catch (URISyntaxException ex) {
            this.clientApplication.log.warn(LogHelper.createMessage("Setting URL telemetry fields failed: " + LogHelper.getPiiScrubbedDetails(ex), msalRequest.headers().getHeaderCorrelationIdValue()));
        }
        return apiEvent;
    }

    private String computeSha256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(input.getBytes(StandardCharsets.UTF_8));
            byte[] hash = digest.digest();
            return Base64.getUrlEncoder().encodeToString(hash);
        }
        catch (NoSuchAlgorithmException ex) {
            this.clientApplication.log.warn(LogHelper.createMessage("Failed to compute SHA-256 hash due to exception - ", LogHelper.getPiiScrubbedDetails(ex)));
            return "Failed to compute SHA-256 hash";
        }
    }
}

