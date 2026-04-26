/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.ClaimsRequest;
import com.microsoft.aad.msal4j.IAcquireTokenParameters;
import com.microsoft.aad.msal4j.IUserAssertion;
import com.microsoft.aad.msal4j.ParameterValidationUtils;
import com.microsoft.aad.msal4j.UserAssertion;
import java.util.Map;
import java.util.Set;

public class OnBehalfOfParameters
implements IAcquireTokenParameters {
    private Set<String> scopes;
    private boolean skipCache;
    private IUserAssertion userAssertion;
    private ClaimsRequest claims;
    private Map<String, String> extraHttpHeaders;
    private Map<String, String> extraQueryParameters;
    private String tenant;

    private OnBehalfOfParameters(Set<String> scopes, Boolean skipCache, IUserAssertion userAssertion, ClaimsRequest claims, Map<String, String> extraHttpHeaders, Map<String, String> extraQueryParameters, String tenant) {
        this.scopes = scopes;
        this.skipCache = skipCache != null && skipCache != false;
        this.userAssertion = userAssertion;
        this.claims = claims;
        this.extraHttpHeaders = extraHttpHeaders;
        this.extraQueryParameters = extraQueryParameters;
        this.tenant = tenant;
    }

    private static OnBehalfOfParametersBuilder builder() {
        return new OnBehalfOfParametersBuilder();
    }

    public static OnBehalfOfParametersBuilder builder(Set<String> scopes, UserAssertion userAssertion) {
        ParameterValidationUtils.validateNotNull("scopes", scopes);
        return OnBehalfOfParameters.builder().scopes(scopes).userAssertion(userAssertion);
    }

    @Override
    public Set<String> scopes() {
        return this.scopes;
    }

    public Boolean skipCache() {
        return this.skipCache;
    }

    public IUserAssertion userAssertion() {
        return this.userAssertion;
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

    public static class OnBehalfOfParametersBuilder {
        private Set<String> scopes;
        private Boolean skipCache;
        private IUserAssertion userAssertion;
        private ClaimsRequest claims;
        private Map<String, String> extraHttpHeaders;
        private Map<String, String> extraQueryParameters;
        private String tenant;

        OnBehalfOfParametersBuilder() {
        }

        public OnBehalfOfParametersBuilder scopes(Set<String> scopes) {
            ParameterValidationUtils.validateNotNull("scopes", scopes);
            this.scopes = scopes;
            return this;
        }

        public OnBehalfOfParametersBuilder skipCache(Boolean skipCache) {
            this.skipCache = skipCache;
            return this;
        }

        public OnBehalfOfParametersBuilder userAssertion(IUserAssertion userAssertion) {
            ParameterValidationUtils.validateNotNull("userAssertion", userAssertion);
            this.userAssertion = userAssertion;
            return this;
        }

        public OnBehalfOfParametersBuilder claims(ClaimsRequest claims) {
            this.claims = claims;
            return this;
        }

        public OnBehalfOfParametersBuilder extraHttpHeaders(Map<String, String> extraHttpHeaders) {
            this.extraHttpHeaders = extraHttpHeaders;
            return this;
        }

        public OnBehalfOfParametersBuilder extraQueryParameters(Map<String, String> extraQueryParameters) {
            this.extraQueryParameters = extraQueryParameters;
            return this;
        }

        public OnBehalfOfParametersBuilder tenant(String tenant) {
            this.tenant = tenant;
            return this;
        }

        public OnBehalfOfParameters build() {
            return new OnBehalfOfParameters(this.scopes, this.skipCache, this.userAssertion, this.claims, this.extraHttpHeaders, this.extraQueryParameters, this.tenant);
        }

        public String toString() {
            return "OnBehalfOfParameters.OnBehalfOfParametersBuilder(scopes=" + this.scopes + ", skipCache$value=" + this.skipCache + ", userAssertion=" + this.userAssertion + ", claims=" + this.claims + ", extraHttpHeaders=" + this.extraHttpHeaders + ", extraQueryParameters=" + this.extraQueryParameters + ", tenant=" + this.tenant + ")";
        }
    }
}

