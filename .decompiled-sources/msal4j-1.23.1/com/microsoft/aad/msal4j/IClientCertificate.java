/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.IClientCredential;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.util.List;

public interface IClientCertificate
extends IClientCredential {
    public PrivateKey privateKey();

    default public String publicCertificateHash256() throws CertificateEncodingException, NoSuchAlgorithmException {
        return null;
    }

    public String publicCertificateHash() throws CertificateEncodingException, NoSuchAlgorithmException;

    public List<String> getEncodedPublicKeyCertificateChain() throws CertificateEncodingException;
}

