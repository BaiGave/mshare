/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.DefaultHttpClient;
import com.microsoft.aad.msal4j.MsalClientException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

class DefaultHttpClientManagedIdentity
extends DefaultHttpClient {
    public static final HostnameVerifier ALL_HOSTS_ACCEPT_HOSTNAME_VERIFIER = new HostnameVerifier(){

        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    DefaultHttpClientManagedIdentity(Proxy proxy, SSLSocketFactory sslSocketFactory, Integer connectTimeout, Integer readTimeout) {
        super(proxy, sslSocketFactory, connectTimeout, readTimeout);
    }

    @Override
    HttpURLConnection openConnection(URL finalURL) throws IOException {
        URLConnection connection = this.proxy != null ? finalURL.openConnection(this.proxy) : finalURL.openConnection();
        connection.setConnectTimeout(this.connectTimeout);
        connection.setReadTimeout(this.readTimeout);
        if (connection instanceof HttpURLConnection) {
            return (HttpURLConnection)connection;
        }
        HttpsURLConnection httpsConnection = (HttpsURLConnection)connection;
        if (this.sslSocketFactory != null) {
            httpsConnection.setSSLSocketFactory(this.sslSocketFactory);
        }
        if (System.getenv("IDENTITY_SERVER_THUMBPRINT") != null) {
            DefaultHttpClientManagedIdentity.addTrustedCertificateThumbprint(httpsConnection, System.getenv("IDENTITY_SERVER_THUMBPRINT"));
        }
        return httpsConnection;
    }

    public static void addTrustedCertificateThumbprint(HttpsURLConnection httpsUrlConnection, final String certificateThumbprint) {
        SSLSocketFactory sslSocketFactory;
        if (httpsUrlConnection.getHostnameVerifier() != ALL_HOSTS_ACCEPT_HOSTNAME_VERIFIER) {
            httpsUrlConnection.setHostnameVerifier(ALL_HOSTS_ACCEPT_HOSTNAME_VERIFIER);
        }
        TrustManager[] certificateTrust = new TrustManager[]{new X509TrustManager(){

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certificates, String authenticationType) throws CertificateException {
                throw new CertificateException("No client side certificate configured.");
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certificates, String authenticationType) throws CertificateException {
                if (certificates == null || certificates.length == 0) {
                    throw new CertificateException("Did not receive any certificate from the server.");
                }
                for (X509Certificate x509Certificate : certificates) {
                    String sslCertificateThumbprint = DefaultHttpClientManagedIdentity.extractCertificateThumbprint(x509Certificate);
                    if (!certificateThumbprint.equalsIgnoreCase(sslCertificateThumbprint)) continue;
                    return;
                }
                throw new RuntimeException("Thumbprint of certificates received did not match the expected thumbprint.");
            }
        }};
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, certificateTrust, null);
            sslSocketFactory = sslContext.getSocketFactory();
        }
        catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Error Creating SSL Context", e);
        }
        if (httpsUrlConnection.getSSLSocketFactory() != sslSocketFactory) {
            httpsUrlConnection.setSSLSocketFactory(sslSocketFactory);
        }
    }

    private static String extractCertificateThumbprint(Certificate certificate) {
        try {
            byte[] updatedDigest;
            byte[] encodedCertificate;
            StringBuilder thumbprint = new StringBuilder();
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            try {
                encodedCertificate = certificate.getEncoded();
            }
            catch (CertificateEncodingException e) {
                throw new RuntimeException(e);
            }
            for (byte b : updatedDigest = messageDigest.digest(encodedCertificate)) {
                int unsignedByte = b & 0xFF;
                if (unsignedByte < 16) {
                    thumbprint.append("0");
                }
                thumbprint.append(Integer.toHexString(unsignedByte));
            }
            return thumbprint.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new MsalClientException("NoSuchAlgorithmException when extracting certificate thumbprint: ", e.getMessage());
        }
    }
}

