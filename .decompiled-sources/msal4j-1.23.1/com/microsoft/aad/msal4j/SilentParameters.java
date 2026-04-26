/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.ClaimsRequest;
import com.microsoft.aad.msal4j.HttpMethod;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.IAcquireTokenParameters;
import com.microsoft.aad.msal4j.ParameterValidationUtils;
import com.microsoft.aad.msal4j.PopParameters;
import com.microsoft.aad.msal4j.StringHelper;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SilentParameters
implements IAcquireTokenParameters {
    private Set<String> scopes;
    private IAccount account;
    private ClaimsRequest claims;
    private String authorityUrl;
    private boolean forceRefresh;
    private Map<String, String> extraHttpHeaders;
    private Map<String, String> extraQueryParameters;
    private String tenant;
    private PopParameters proofOfPossession;

    private SilentParameters(Set<String> scopes, IAccount account, ClaimsRequest claims, String authorityUrl, boolean forceRefresh, Map<String, String> extraHttpHeaders, Map<String, String> extraQueryParameters, String tenant, PopParameters proofOfPossession) {
        this.scopes = scopes;
        this.account = account;
        this.claims = claims;
        this.authorityUrl = authorityUrl;
        this.forceRefresh = forceRefresh;
        this.extraHttpHeaders = extraHttpHeaders;
        this.extraQueryParameters = extraQueryParameters;
        this.tenant = tenant;
        this.proofOfPossession = proofOfPossession;
    }

    private static SilentParametersBuilder builder() {
        return new SilentParametersBuilder();
    }

    public static SilentParametersBuilder builder(Set<String> scopes, IAccount account) {
        ParameterValidationUtils.validateNotNull("account", account);
        ParameterValidationUtils.validateNotNull("scopes", scopes);
        return SilentParameters.builder().scopes(SilentParameters.removeEmptyScope(scopes)).account(account);
    }

    public static SilentParametersBuilder builder(Set<String> scopes) {
        ParameterValidationUtils.validateNotNull("scopes", scopes);
        return SilentParameters.builder().scopes(SilentParameters.removeEmptyScope(scopes));
    }

    private static Set<String> removeEmptyScope(Set<String> scopes) {
        HashSet<String> updatedScopes = new HashSet<String>();
        for (String scope : scopes) {
            if (scope.equalsIgnoreCase(StringHelper.EMPTY_STRING)) continue;
            updatedScopes.add(scope.trim());
        }
        return updatedScopes;
    }

    @Override
    public Set<String> scopes() {
        return this.scopes;
    }

    public IAccount account() {
        return this.account;
    }

    @Override
    public ClaimsRequest claims() {
        return this.claims;
    }

    public String authorityUrl() {
        return this.authorityUrl;
    }

    public boolean forceRefresh() {
        return this.forceRefresh;
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

    public PopParameters proofOfPossession() {
        return this.proofOfPossession;
    }

    public static class SilentParametersBuilder {
        private Set<String> scopes;
        private IAccount account;
        private ClaimsRequest claims;
        private String authorityUrl;
        private boolean forceRefresh;
        private Map<String, String> extraHttpHeaders;
        private Map<String, String> extraQueryParameters;
        private String tenant;
        private PopParameters proofOfPossession;

        SilentParametersBuilder() {
        }

        public SilentParametersBuilder proofOfPossession(HttpMethod httpMethod, URI uri, String nonce) {
            this.proofOfPossession = new PopParameters(httpMethod, uri, nonce);
            return this;
        }

        public SilentParametersBuilder scopes(Set<String> scopes) {
            ParameterValidationUtils.validateNotNull("scopes", scopes);
            this.scopes = scopes;
            return this;
        }

        public SilentParametersBuilder account(IAccount account) {
            this.account = account;
            return this;
        }

        public SilentParametersBuilder claims(ClaimsRequest claims) {
            this.claims = claims;
            return this;
        }

        public SilentParametersBuilder authorityUrl(String authorityUrl) {
            this.authorityUrl = authorityUrl;
            return this;
        }

        public SilentParametersBuilder forceRefresh(boolean forceRefresh) {
            this.forceRefresh = forceRefresh;
            return this;
        }

        public SilentParametersBuilder extraHttpHeaders(Map<String, String> extraHttpHeaders) {
            this.extraHttpHeaders = extraHttpHeaders;
            return this;
        }

        public SilentParametersBuilder extraQueryParameters(Map<String, String> extraQueryParameters) {
            this.extraQueryParameters = extraQueryParameters;
            return this;
        }

        public SilentParametersBuilder tenant(String tenant) {
            this.tenant = tenant;
            return this;
        }

        public SilentParameters build() {
            return new SilentParameters(this.scopes, this.account, this.claims, this.authorityUrl, this.forceRefresh, this.extraHttpHeaders, this.extraQueryParameters, this.tenant, this.proofOfPossession);
        }

        public String toString() {
            return "SilentParameters.SilentParametersBuilder(scopes=" + this.scopes + ", account=" + this.account + ", claims=" + this.claims + ", authorityUrl=" + this.authorityUrl + ", forceRefresh=" + this.forceRefresh + ", extraHttpHeaders=" + this.extraHttpHeaders + ", extraQueryParameters=" + this.extraQueryParameters + ", tenant=" + this.tenant + ", proofOfPossession=" + this.proofOfPossession + ")";
        }
    }
}

