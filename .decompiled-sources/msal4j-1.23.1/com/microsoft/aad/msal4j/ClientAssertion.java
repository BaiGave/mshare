/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.IClientAssertion;
import com.microsoft.aad.msal4j.MsalClientException;
import com.microsoft.aad.msal4j.StringHelper;
import java.util.Objects;
import java.util.concurrent.Callable;

final class ClientAssertion
implements IClientAssertion {
    static final String ASSERTION_TYPE_JWT_BEARER = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
    private final String assertion;
    private final Callable<String> assertionProvider;

    ClientAssertion(String assertion) {
        if (StringHelper.isBlank(assertion)) {
            throw new NullPointerException("assertion");
        }
        this.assertion = assertion;
        this.assertionProvider = null;
    }

    ClientAssertion(Callable<String> assertionProvider) {
        if (assertionProvider == null) {
            throw new NullPointerException("assertionProvider");
        }
        this.assertion = null;
        this.assertionProvider = assertionProvider;
    }

    @Override
    public String assertion() {
        if (this.assertionProvider != null) {
            try {
                String generatedAssertion = this.assertionProvider.call();
                if (StringHelper.isBlank(generatedAssertion)) {
                    throw new MsalClientException("Assertion provider returned null or empty assertion", "invalid_jwt");
                }
                return generatedAssertion;
            }
            catch (MsalClientException ex) {
                throw ex;
            }
            catch (Exception ex) {
                throw new MsalClientException(ex);
            }
        }
        return this.assertion;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClientAssertion)) {
            return false;
        }
        ClientAssertion other = (ClientAssertion)o;
        if (this.assertionProvider != null && other.assertionProvider != null) {
            return this.assertionProvider == other.assertionProvider;
        }
        return Objects.equals(this.assertion(), other.assertion());
    }

    public int hashCode() {
        if (this.assertionProvider != null) {
            return System.identityHashCode(this.assertionProvider);
        }
        int result = 1;
        result = result * 59 + (this.assertion == null ? 43 : this.assertion.hashCode());
        return result;
    }
}

