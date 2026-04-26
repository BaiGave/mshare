/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.ClaimsRequest;
import com.microsoft.aad.msal4j.HttpMethod;
import com.microsoft.aad.msal4j.IAcquireTokenParameters;
import com.microsoft.aad.msal4j.ParameterValidationUtils;
import com.microsoft.aad.msal4j.PopParameters;
import com.microsoft.aad.msal4j.Prompt;
import com.microsoft.aad.msal4j.SystemBrowserOptions;
import java.net.URI;
import java.util.Map;
import java.util.Set;

public class InteractiveRequestParameters
implements IAcquireTokenParameters {
    private URI redirectUri;
    private ClaimsRequest claims;
    private Set<String> scopes;
    private Prompt prompt;
    private String loginHint;
    private String domainHint;
    private SystemBrowserOptions systemBrowserOptions;
    private String claimsChallenge;
    private Map<String, String> extraHttpHeaders;
    private Map<String, String> extraQueryParameters;
    private String tenant;
    private int httpPollingTimeoutInSeconds;
    private boolean instanceAware;
    private long windowHandle;
    private PopParameters proofOfPossession;

    private InteractiveRequestParameters(URI redirectUri, ClaimsRequest claims, Set<String> scopes, Prompt prompt, String loginHint, String domainHint, SystemBrowserOptions systemBrowserOptions, String claimsChallenge, Map<String, String> extraHttpHeaders, Map<String, String> extraQueryParameters, String tenant, int httpPollingTimeoutInSeconds, boolean instanceAware, long windowHandle, PopParameters proofOfPossession) {
        this.redirectUri = redirectUri;
        this.claims = claims;
        this.scopes = scopes;
        this.prompt = prompt;
        this.loginHint = loginHint;
        this.domainHint = domainHint;
        this.systemBrowserOptions = systemBrowserOptions;
        this.claimsChallenge = claimsChallenge;
        this.extraHttpHeaders = extraHttpHeaders;
        this.extraQueryParameters = extraQueryParameters;
        this.tenant = tenant;
        this.httpPollingTimeoutInSeconds = httpPollingTimeoutInSeconds;
        this.instanceAware = instanceAware;
        this.windowHandle = windowHandle;
        this.proofOfPossession = proofOfPossession;
    }

    private static InteractiveRequestParametersBuilder builder() {
        return new InteractiveRequestParametersBuilder();
    }

    public static InteractiveRequestParametersBuilder builder(URI redirectUri) {
        ParameterValidationUtils.validateNotNull("redirect_uri", redirectUri);
        return InteractiveRequestParameters.builder().redirectUri(redirectUri);
    }

    public URI redirectUri() {
        return this.redirectUri;
    }

    @Override
    public ClaimsRequest claims() {
        return this.claims;
    }

    @Override
    public Set<String> scopes() {
        return this.scopes;
    }

    public Prompt prompt() {
        return this.prompt;
    }

    public String loginHint() {
        return this.loginHint;
    }

    public String domainHint() {
        return this.domainHint;
    }

    public SystemBrowserOptions systemBrowserOptions() {
        return this.systemBrowserOptions;
    }

    public String claimsChallenge() {
        return this.claimsChallenge;
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

    public int httpPollingTimeoutInSeconds() {
        return this.httpPollingTimeoutInSeconds;
    }

    public boolean instanceAware() {
        return this.instanceAware;
    }

    public long windowHandle() {
        return this.windowHandle;
    }

    public PopParameters proofOfPossession() {
        return this.proofOfPossession;
    }

    void redirectUri(URI redirectUri) {
        this.redirectUri = redirectUri;
    }

    public static class InteractiveRequestParametersBuilder {
        private URI redirectUri;
        private ClaimsRequest claims;
        private Set<String> scopes;
        private Prompt prompt;
        private String loginHint;
        private String domainHint;
        private SystemBrowserOptions systemBrowserOptions;
        private String claimsChallenge;
        private Map<String, String> extraHttpHeaders;
        private Map<String, String> extraQueryParameters;
        private String tenant;
        private int httpPollingTimeoutInSeconds = 120;
        private boolean instanceAware;
        private long windowHandle;
        private PopParameters proofOfPossession;

        InteractiveRequestParametersBuilder() {
        }

        public InteractiveRequestParametersBuilder proofOfPossession(HttpMethod httpMethod, URI uri, String nonce) {
            this.proofOfPossession = new PopParameters(httpMethod, uri, nonce);
            return this;
        }

        public InteractiveRequestParametersBuilder redirectUri(URI redirectUri) {
            ParameterValidationUtils.validateNotNull("redirectUri", redirectUri);
            this.redirectUri = redirectUri;
            return this;
        }

        public InteractiveRequestParametersBuilder claims(ClaimsRequest claims) {
            this.claims = claims;
            return this;
        }

        public InteractiveRequestParametersBuilder scopes(Set<String> scopes) {
            this.scopes = scopes;
            return this;
        }

        public InteractiveRequestParametersBuilder prompt(Prompt prompt) {
            this.prompt = prompt;
            return this;
        }

        public InteractiveRequestParametersBuilder loginHint(String loginHint) {
            this.loginHint = loginHint;
            return this;
        }

        public InteractiveRequestParametersBuilder domainHint(String domainHint) {
            this.domainHint = domainHint;
            return this;
        }

        public InteractiveRequestParametersBuilder systemBrowserOptions(SystemBrowserOptions systemBrowserOptions) {
            this.systemBrowserOptions = systemBrowserOptions;
            return this;
        }

        public InteractiveRequestParametersBuilder claimsChallenge(String claimsChallenge) {
            this.claimsChallenge = claimsChallenge;
            return this;
        }

        public InteractiveRequestParametersBuilder extraHttpHeaders(Map<String, String> extraHttpHeaders) {
            this.extraHttpHeaders = extraHttpHeaders;
            return this;
        }

        public InteractiveRequestParametersBuilder extraQueryParameters(Map<String, String> extraQueryParameters) {
            this.extraQueryParameters = extraQueryParameters;
            return this;
        }

        public InteractiveRequestParametersBuilder tenant(String tenant) {
            this.tenant = tenant;
            return this;
        }

        public InteractiveRequestParametersBuilder httpPollingTimeoutInSeconds(int httpPollingTimeoutInSeconds) {
            this.httpPollingTimeoutInSeconds = httpPollingTimeoutInSeconds;
            return this;
        }

        public InteractiveRequestParametersBuilder instanceAware(boolean instanceAware) {
            this.instanceAware = instanceAware;
            return this;
        }

        public InteractiveRequestParametersBuilder windowHandle(long windowHandle) {
            this.windowHandle = windowHandle;
            return this;
        }

        public InteractiveRequestParameters build() {
            return new InteractiveRequestParameters(this.redirectUri, this.claims, this.scopes, this.prompt, this.loginHint, this.domainHint, this.systemBrowserOptions, this.claimsChallenge, this.extraHttpHeaders, this.extraQueryParameters, this.tenant, this.httpPollingTimeoutInSeconds, this.instanceAware, this.windowHandle, this.proofOfPossession);
        }

        public String toString() {
            return "InteractiveRequestParameters.InteractiveRequestParametersBuilder(redirectUri=" + this.redirectUri + ", claims=" + this.claims + ", scopes=" + this.scopes + ", prompt=" + (Object)((Object)this.prompt) + ", loginHint=" + this.loginHint + ", domainHint=" + this.domainHint + ", systemBrowserOptions=" + this.systemBrowserOptions + ", claimsChallenge=" + this.claimsChallenge + ", extraHttpHeaders=" + this.extraHttpHeaders + ", extraQueryParameters=" + this.extraQueryParameters + ", tenant=" + this.tenant + ", httpPollingTimeoutInSeconds=" + this.httpPollingTimeoutInSeconds + ", instanceAware=" + this.instanceAware + ", windowHandle=" + this.windowHandle + ", proofOfPossession=" + this.proofOfPossession + ")";
        }
    }
}

