/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.ClaimsRequest;
import com.microsoft.aad.msal4j.IAcquireTokenParameters;
import com.microsoft.aad.msal4j.ParameterValidationUtils;
import java.util.Map;
import java.util.Set;

public class RefreshTokenParameters
implements IAcquireTokenParameters {
    private Set<String> scopes;
    private String refreshToken;
    private ClaimsRequest claims;
    private Map<String, String> extraHttpHeaders;
    private Map<String, String> extraQueryParameters;
    private String tenant;

    private RefreshTokenParameters(Set<String> scopes, String refreshToken, ClaimsRequest claims, Map<String, String> extraHttpHeaders, Map<String, String> extraQueryParameters, String tenant) {
        this.scopes = scopes;
        this.refreshToken = refreshToken;
        this.claims = claims;
        this.extraHttpHeaders = extraHttpHeaders;
        this.extraQueryParameters = extraQueryParameters;
        this.tenant = tenant;
    }

    private static RefreshTokenParametersBuilder builder() {
        return new RefreshTokenParametersBuilder();
    }

    public static RefreshTokenParametersBuilder builder(Set<String> scopes, String refreshToken) {
        ParameterValidationUtils.validateNotBlank("refreshToken", refreshToken);
        return RefreshTokenParameters.builder().scopes(scopes).refreshToken(refreshToken);
    }

    @Override
    public Set<String> scopes() {
        return this.scopes;
    }

    public String refreshToken() {
        return this.refreshToken;
    }

    @Override
    public ClaimsRequest claims() {
        return this.claims;
    }

    @Override
    public Map<String, String> extraHttpHeaders() {
        return this.extraHttpHeaders;
    }

    @Override
    public Map<String, String> extraQueryParameters() {
        return this.extraQueryParameters;
    }

    @Override
    public String tenant() {
        return this.tenant;
    }

    public static class RefreshTokenParametersBuilder {
        private Set<String> scopes;
        private String refreshToken;
        private ClaimsRequest claims;
        private Map<String, String> extraHttpHeaders;
        private Map<String, String> extraQueryParameters;
        private String tenant;

        RefreshTokenParametersBuilder() {
        }

        public RefreshTokenParametersBuilder scopes(Set<String> scopes) {
            ParameterValidationUtils.validateNotNull("scopes", scopes);
            this.scopes = scopes;
            return this;
        }

        public RefreshTokenParametersBuilder refreshToken(String refreshToken) {
            ParameterValidationUtils.validateNotNull("refreshToken", this.scopes);
            this.refreshToken = refreshToken;
            return this;
        }

        public RefreshTokenParametersBuilder claims(ClaimsRequest claims) {
            this.claims = claims;
            return this;
        }

        public RefreshTokenParametersBuilder extraHttpHeaders(Map<String, String> extraHttpHeaders) {
            this.extraHttpHeaders = extraHttpHeaders;
            return this;
        }

        public RefreshTokenParametersBuilder extraQueryParameters(Map<String, String> extraQueryParameters) {
            this.extraQueryParameters = extraQueryParameters;
            return this;
        }

        public RefreshTokenParametersBuilder tenant(String tenant) {
            this.tenant = tenant;
            return this;
        }

        public RefreshTokenParameters build() {
            return new RefreshTokenParameters(this.scopes, this.refreshToken, this.claims, this.extraHttpHeaders, this.extraQueryParameters, this.tenant);
        }

        public String toString() {
            return "RefreshTokenParameters.RefreshTokenParametersBuilder(scopes=" + this.scopes + ", refreshToken=" + this.refreshToken + ", claims=" + this.claims + ", extraHttpHeaders=" + this.extraHttpHeaders + ", extraQueryParameters=" + this.extraQueryParameters + ", tenant=" + this.tenant + ")";
        }
    }
}

