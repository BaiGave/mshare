/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.HttpMethod;
import com.microsoft.aad.msal4j.HttpRequest;
import com.microsoft.aad.msal4j.HttpResponse;
import com.microsoft.aad.msal4j.IHttpClient;
import com.microsoft.aad.msal4j.IHttpResponse;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DefaultHttpClient
implements IHttpClient {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultHttpClient.class);
    final Proxy proxy;
    final SSLSocketFactory sslSocketFactory;
    int connectTimeout = 0;
    int readTimeout = 0;

    DefaultHttpClient(Proxy proxy, SSLSocketFactory sslSocketFactory, Integer connectTimeout, Integer readTimeout) {
        this.proxy = proxy;
        this.sslSocketFactory = sslSocketFactory;
        if (connectTimeout != null) {
            this.connectTimeout = connectTimeout;
        }
        if (readTimeout != null) {
            this.readTimeout = readTimeout;
        }
    }

    @Override
    public IHttpResponse send(HttpRequest httpRequest) throws Exception {
        HttpResponse response = null;
        if (httpRequest.httpMethod() == HttpMethod.GET) {
            response = this.executeHttpGet(httpRequest);
        } else if (httpRequest.httpMethod() == HttpMethod.POST) {
            response = this.executeHttpPost(httpRequest);
        }
        return response;
    }

    private HttpResponse executeHttpGet(HttpRequest httpRequest) throws Exception {
        HttpURLConnection conn = this.openConnection(httpRequest.url());
        this.configureAdditionalHeaders(conn, httpRequest);
        return this.readResponseFromConnection(conn);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private HttpResponse executeHttpPost(HttpRequest httpRequest) throws Exception {
        HttpURLConnection conn = this.openConnection(httpRequest.url());
        this.configureAdditionalHeaders(conn, httpRequest);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        try (DataOutputStream wr = null;){
            wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(httpRequest.body());
            wr.flush();
            HttpResponse httpResponse = this.readResponseFromConnection(conn);
            return httpResponse;
        }
    }

    HttpURLConnection openConnection(URL finalURL) throws IOException {
        URLConnection connection = this.proxy != null ? finalURL.openConnection(this.proxy) : finalURL.openConnection();
        connection.setConnectTimeout(this.connectTimeout);
        connection.setReadTimeout(this.readTimeout);
        if (connection instanceof HttpsURLConnection) {
            HttpsURLConnection httpsConnection = (HttpsURLConnection)connection;
            if (this.sslSocketFactory != null) {
                httpsConnection.setSSLSocketFactory(this.sslSocketFactory);
            }
            return httpsConnection;
        }
        return (HttpURLConnection)connection;
    }

    private void configureAdditionalHeaders(HttpURLConnection conn, HttpRequest httpRequest) {
        if (httpRequest.headers() != null) {
            for (Map.Entry<String, String> entry : httpRequest.headers().entrySet()) {
                if (entry.getValue() == null) continue;
                conn.addRequestProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    private HttpResponse readResponseFromConnection(HttpURLConnection conn) throws IOException {
        try (InputStream is = null;){
            HttpResponse httpResponse = new HttpResponse();
            int responseCode = conn.getResponseCode();
            httpResponse.statusCode(responseCode);
            if (responseCode != 200) {
                is = conn.getErrorStream();
                if (is != null) {
                    httpResponse.addHeaders(conn.getHeaderFields());
                    httpResponse.body(this.inputStreamToString(is));
                }
                HttpResponse httpResponse2 = httpResponse;
                return httpResponse2;
            }
            is = conn.getInputStream();
            httpResponse.addHeaders(conn.getHeaderFields());
            httpResponse.body(this.inputStreamToString(is));
            HttpResponse httpResponse3 = httpResponse;
            return httpResponse3;
        }
    }

    private String inputStreamToString(InputStream is) {
        Scanner s = new Scanner(is, StandardCharsets.UTF_8.name()).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}

