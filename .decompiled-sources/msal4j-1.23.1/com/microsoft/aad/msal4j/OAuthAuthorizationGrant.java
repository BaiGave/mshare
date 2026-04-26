/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.AbstractMsalAuthorizationGrant;
import com.microsoft.aad.msal4j.ClaimsRequest;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

class OAuthAuthorizationGrant
extends AbstractMsalAuthorizationGrant {
    private final Map<String, String> params = new LinkedHashMap<String, String>();

    OAuthAuthorizationGrant(Map<String, String> params, Set<String> scopes) {
        this.scopes = new HashSet<String>(AbstractMsalAuthorizationGrant.COMMON_SCOPES);
        if (scopes != null) {
            this.scopes.addAll(scopes);
        }
        this.params.put("scope", String.join((CharSequence)" ", this.scopes));
        this.params.put("client_info", "1");
        if (params != null) {
            this.params.putAll(params);
        }
    }

    OAuthAuthorizationGrant(Map<String, String> params, Set<String> scopes, ClaimsRequest claims) {
        this(params, scopes);
        if (claims != null) {
            this.claims = claims;
            this.params.put("claims", claims.formatAsJSONString());
        }
    }

    void addAndReplaceParams(Map<String, String> params) {
        if (params != null) {
            this.params.putAll(params);
        }
    }

    String getParamValue(String paramKey) {
        return this.params.get(paramKey);
    }

    @Override
    public Map<String, String> toParameters() {
        return Collections.unmodifiableMap(new LinkedHashMap<String, String>(this.params));
    }
}

