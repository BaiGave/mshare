/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.IClientSecret;
import com.microsoft.aad.msal4j.StringHelper;
import java.util.Objects;

final class ClientSecret
implements IClientSecret {
    private final String clientSecret;

    ClientSecret(String clientSecret) {
        if (StringHelper.isBlank(clientSecret)) {
            throw new IllegalArgumentException("clientSecret is null or empty");
        }
        this.clientSecret = clientSecret;
    }

    @Override
    public String clientSecret() {
        return this.clientSecret;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClientSecret)) {
            return false;
        }
        ClientSecret other = (ClientSecret)o;
        return Objects.equals(this.clientSecret, other.clientSecret);
    }

    public int hashCode() {
        int result = 1;
        result = result * 59 + (this.clientSecret == null ? 43 : this.clientSecret.hashCode());
        return result;
    }
}

