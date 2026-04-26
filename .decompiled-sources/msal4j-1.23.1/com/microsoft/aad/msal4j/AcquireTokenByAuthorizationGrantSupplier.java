/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.AbstractApplicationBase;
import com.microsoft.aad.msal4j.AbstractMsalAuthorizationGrant;
import com.microsoft.aad.msal4j.AuthenticationResult;
import com.microsoft.aad.msal4j.AuthenticationResultSupplier;
import com.microsoft.aad.msal4j.Authority;
import com.microsoft.aad.msal4j.AuthorityType;
import com.microsoft.aad.msal4j.IntegratedWindowsAuthorizationGrant;
import com.microsoft.aad.msal4j.InteractionRequiredCache;
import com.microsoft.aad.msal4j.MsalClientException;
import com.microsoft.aad.msal4j.MsalInteractionRequiredException;
import com.microsoft.aad.msal4j.MsalRequest;
import com.microsoft.aad.msal4j.OAuthAuthorizationGrant;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.RefreshTokenRequest;
import com.microsoft.aad.msal4j.UserDiscoveryRequest;
import com.microsoft.aad.msal4j.UserDiscoveryResponse;
import com.microsoft.aad.msal4j.WSTrustRequest;
import com.microsoft.aad.msal4j.WSTrustResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

class AcquireTokenByAuthorizationGrantSupplier
extends AuthenticationResultSupplier {
    private Authority requestAuthority;
    private MsalRequest msalRequest;

    AcquireTokenByAuthorizationGrantSupplier(AbstractApplicationBase clientApplication, MsalRequest msalRequest, Authority authority) {
        super(clientApplication, msalRequest);
        this.msalRequest = msalRequest;
        this.requestAuthority = authority;
    }

    @Override
    AuthenticationResult execute() throws Exception {
        MsalInteractionRequiredException cachedEx;
        AbstractMsalAuthorizationGrant authGrant = this.msalRequest.msalAuthorizationGrant();
        if (this.IsUiRequiredCacheSupported() && (cachedEx = InteractionRequiredCache.getCachedInteractionRequiredException(((RefreshTokenRequest)this.msalRequest).getFullThumbprint())) != null) {
            throw cachedEx;
        }
        if (authGrant instanceof OAuthAuthorizationGrant) {
            this.processPasswordGrant((OAuthAuthorizationGrant)authGrant);
        }
        if (authGrant instanceof IntegratedWindowsAuthorizationGrant) {
            IntegratedWindowsAuthorizationGrant integratedAuthGrant = (IntegratedWindowsAuthorizationGrant)authGrant;
            this.msalRequest.msalAuthorizationGrant = new OAuthAuthorizationGrant(this.getAuthorizationGrantIntegrated(integratedAuthGrant.getUserName()), integratedAuthGrant.getScopes(), integratedAuthGrant.getClaims());
        }
        if (this.requestAuthority == null) {
            this.requestAuthority = this.clientApplication.authenticationAuthority;
        }
        this.requestAuthority = this.getAuthorityWithPrefNetworkHost(this.requestAuthority.authority());
        try {
            return this.clientApplication.acquireTokenCommon(this.msalRequest, this.requestAuthority);
        }
        catch (MsalInteractionRequiredException ex) {
            if (this.IsUiRequiredCacheSupported()) {
                InteractionRequiredCache.set(((RefreshTokenRequest)this.msalRequest).getFullThumbprint(), ex);
            }
            throw ex;
        }
    }

    private boolean IsUiRequiredCacheSupported() {
        return this.msalRequest instanceof RefreshTokenRequest && this.clientApplication instanceof PublicClientApplication;
    }

    private void processPasswordGrant(OAuthAuthorizationGrant authGrant) throws Exception {
        if (!authGrant.getParamValue("grant_type").equals("password") || this.msalRequest.application().authenticationAuthority.authorityType != AuthorityType.AAD) {
            return;
        }
        UserDiscoveryResponse userDiscoveryResponse = UserDiscoveryRequest.execute(this.clientApplication.authenticationAuthority.getUserRealmEndpoint(authGrant.getParamValue("username")), this.msalRequest.headers().getReadonlyHeaderMap(), this.msalRequest.requestContext(), this.clientApplication.serviceBundle());
        if (userDiscoveryResponse.isAccountFederated()) {
            WSTrustResponse response = WSTrustRequest.execute(userDiscoveryResponse.federationMetadataUrl(), authGrant.getParamValue("username"), authGrant.getParamValue("password"), userDiscoveryResponse.cloudAudienceUrn(), this.msalRequest.requestContext(), this.clientApplication.serviceBundle(), this.clientApplication.logPii());
            authGrant.addAndReplaceParams(this.getSAMLAuthGrantParameters(response));
        }
    }

    private Map<String, String> getSAMLAuthGrantParameters(WSTrustResponse response) {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        if (response.isTokenSaml2()) {
            params.put("grant_type", "urn:ietf:params:oauth:grant-type:saml2-bearer");
        } else {
            params.put("grant_type", "urn:ietf:params:oauth:grant-type:saml1_1-bearer");
        }
        params.put("assertion", Base64.getUrlEncoder().encodeToString(response.getToken().getBytes(StandardCharsets.UTF_8)));
        return params;
    }

    private Map<String, String> getAuthorizationGrantIntegrated(String userName) throws Exception {
        String userRealmEndpoint = this.clientApplication.authenticationAuthority.getUserRealmEndpoint(URLEncoder.encode(userName, StandardCharsets.UTF_8.name()));
        UserDiscoveryResponse userRealmResponse = UserDiscoveryRequest.execute(userRealmEndpoint, this.msalRequest.headers().getReadonlyHeaderMap(), this.msalRequest.requestContext(), this.clientApplication.serviceBundle());
        if (!userRealmResponse.isAccountFederated() || !"WSTrust".equalsIgnoreCase(userRealmResponse.federationProtocol())) {
            if (userRealmResponse.isAccountManaged()) {
                throw new MsalClientException("Password is required for managed user", "password_required_for_managed_user");
            }
            throw new MsalClientException("User Realm request failed", "user_realm_discovery_failed");
        }
        String mexURL = userRealmResponse.federationMetadataUrl();
        String cloudAudienceUrn = userRealmResponse.cloudAudienceUrn();
        WSTrustResponse wsTrustResponse = WSTrustRequest.execute(mexURL, cloudAudienceUrn, this.msalRequest.requestContext(), this.clientApplication.serviceBundle(), this.clientApplication.logPii());
        Map<String, String> params = this.getSAMLAuthGrantParameters(wsTrustResponse);
        return params;
    }
}

