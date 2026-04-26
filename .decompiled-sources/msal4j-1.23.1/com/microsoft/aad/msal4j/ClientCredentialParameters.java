/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.ClaimsRequest;
import com.microsoft.aad.msal4j.IAcquireTokenParameters;
import com.microsoft.aad.msal4j.IClientCredential;
import com.microsoft.aad.msal4j.ParameterValidationUtils;
import java.util.Map;
import java.util.Set;

public class ClientCredentialParameters
implements IAcquireTokenParameters {
    private Set<String> scopes;
    private Boolean skipCache = false;
    private ClaimsRequest claims;
    private Map<String, String> extraHttpHeaders;
    private Map<String, String> extraQueryParameters;
    private String tenant;
    private IClientCredential clientCredential;

    private ClientCredentialParameters(Set<String> scopes, Boolean skipCache, ClaimsRequest claims, Map<String, String> extraHttpHeaders, Map<String, String> extraQueryParameters, String tenant, IClientCredential clientCredential) {
        this.scopes = scopes;
        this.skipCache = skipCache;
        this.claims = claims;
        this.extraHttpHeaders = extraHttpHeaders;
        this.extraQueryParameters = extraQueryParameters;
        this.tenant = tenant;
        this.clientCredential = clientCredential;
    }

    private static ClientCredentialParametersBuilder builder() {
        return new ClientCredentialParametersBuilder();
    }

    public static ClientCredentialParametersBuilder builder(Set<String> scopes) {
        ParameterValidationUtils.validateNotNull("scopes", scopes);
        return ClientCredentialParameters.builder().scopes(scopes);
    }

    @Override
    public Set<String> scopes() {
        return this.scopes;
    }

    public Boolean skipCache() {
        return this.skipCache;
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

    public IClientCredential clientCredential() {
        return this.clientCredential;
    }

    public static class ClientCredentialParametersBuilder {
        private Set<String> scopes;
        private Boolean skipCache = false;
        private ClaimsRequest claims;
        private Map<String, String> extraHttpHeaders;
        private Map<String, String> extraQueryParameters;
        private String tenant;
        private IClientCredential clientCredential;

        ClientCredentialParametersBuilder() {
        }

        public ClientCredentialParametersBuilder scopes(Set<String> scopes) {
            ParameterValidationUtils.validateNotNull("scopes", scopes);
            this.scopes = scopes;
            return this;
        }

        public ClientCredentialParametersBuilder skipCache(Boolean skipCache) {
            this.skipCache = skipCache;
            return this;
        }

        public ClientCredentialParametersBuilder claims(ClaimsRequest claims) {
            this.claims = claims;
            return this;
        }

        public ClientCredentialParametersBuilder extraHttpHeaders(Map<String, String> extraHttpHeaders) {
            this.extraHttpHeaders = extraHttpHeaders;
            return this;
        }

        public ClientCredentialParametersBuilder extraQueryParameters(Map<String, String> extraQueryParameters) {
            this.extraQueryParameters = extraQueryParameters;
            return this;
        }

        public ClientCredentialParametersBuilder tenant(String tenant) {
            this.tenant = tenant;
            return this;
        }

        public ClientCredentialParametersBuilder clientCredential(IClientCredential clientCredential) {
            this.clientCredential = clientCredential;
            return this;
        }

        public ClientCredentialParameters build() {
            return new ClientCredentialParameters(this.scopes, this.skipCache, this.claims, this.extraHttpHeaders, this.extraQueryParameters, this.tenant, this.clientCredential);
        }

        public String toString() {
            return "ClientCredentialParameters.ClientCredentialParametersBuilder(scopes=" + this.scopes + ", skipCache=" + this.skipCache + ", claims=" + this.claims + ", extraHttpHeaders=" + this.extraHttpHeaders + ", extraQueryParameters=" + this.extraQueryParameters + ", tenant=" + this.tenant + ", clientCredential=" + this.clientCredential + ")";
        }
    }
}

