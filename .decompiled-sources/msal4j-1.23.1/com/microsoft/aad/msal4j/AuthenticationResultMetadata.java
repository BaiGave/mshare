/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.CacheRefreshReason;
import com.microsoft.aad.msal4j.TokenSource;
import java.io.Serializable;

public class AuthenticationResultMetadata
implements Serializable {
    private TokenSource tokenSource;
    private Long refreshOn;
    private CacheRefreshReason cacheRefreshReason = CacheRefreshReason.NOT_APPLICABLE;

    AuthenticationResultMetadata(TokenSource tokenSource, Long refreshOn, CacheRefreshReason cacheRefreshReason) {
        this.tokenSource = tokenSource;
        this.refreshOn = refreshOn;
        this.cacheRefreshReason = cacheRefreshReason == null ? CacheRefreshReason.NOT_APPLICABLE : cacheRefreshReason;
    }

    public static AuthenticationResultMetadataBuilder builder() {
        return new AuthenticationResultMetadataBuilder();
    }

    public TokenSource tokenSource() {
        return this.tokenSource;
    }

    public Long refreshOn() {
        return this.refreshOn;
    }

    public CacheRefreshReason cacheRefreshReason() {
        return this.cacheRefreshReason;
    }

    void tokenSource(TokenSource tokenSource) {
        this.tokenSource = tokenSource;
    }

    void refreshOn(Long refreshOn) {
        this.refreshOn = refreshOn;
    }

    void cacheRefreshReason(CacheRefreshReason cacheRefreshReason) {
        this.cacheRefreshReason = cacheRefreshReason;
    }

    public static class AuthenticationResultMetadataBuilder {
        private TokenSource tokenSource;
        private Long refreshOn;
        private CacheRefreshReason cacheRefreshReason;

        AuthenticationResultMetadataBuilder() {
        }

        public AuthenticationResultMetadataBuilder tokenSource(TokenSource tokenSource) {
            this.tokenSource = tokenSource;
            return this;
        }

        public AuthenticationResultMetadataBuilder refreshOn(Long refreshOn) {
            this.refreshOn = refreshOn;
            return this;
        }

        public AuthenticationResultMetadataBuilder cacheRefreshReason(CacheRefreshReason cacheRefreshReason) {
            this.cacheRefreshReason = cacheRefreshReason;
            return this;
        }

        public AuthenticationResultMetadata build() {
            return new AuthenticationResultMetadata(this.tokenSource, this.refreshOn, this.cacheRefreshReason);
        }

        public String toString() {
            return "AuthenticationResultMetadata.AuthenticationResultMetadataBuilder(tokenSource=" + (Object)((Object)this.tokenSource) + ", refreshOn=" + this.refreshOn + ", cacheRefreshReason$value=" + (Object)((Object)this.cacheRefreshReason) + ")";
        }
    }
}

