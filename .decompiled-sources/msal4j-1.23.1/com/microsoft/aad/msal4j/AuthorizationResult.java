/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.MsalClientException;
import com.microsoft.aad.msal4j.StringHelper;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

class AuthorizationResult {
    private String code;
    private String state;
    private AuthorizationStatus status;
    private String error;
    private String errorDescription;
    private String environment;

    String code() {
        return this.code;
    }

    String state() {
        return this.state;
    }

    AuthorizationStatus status() {
        return this.status;
    }

    String error() {
        return this.error;
    }

    String errorDescription() {
        return this.errorDescription;
    }

    String environment() {
        return this.environment;
    }

    void code(String code) {
        this.code = code;
    }

    void state(String state) {
        this.state = state;
    }

    void status(AuthorizationStatus status) {
        this.status = status;
    }

    void error(String error) {
        this.error = error;
    }

    void errorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    void environment(String environment) {
        this.environment = environment;
    }

    static AuthorizationResult fromResponseBody(String responseBody) {
        if (StringHelper.isBlank(responseBody)) {
            return new AuthorizationResult(AuthorizationStatus.UnknownError, "invalid_authorization_result", "The authorization server returned an invalid response: response is null or empty");
        }
        Map<String, String> queryParameters = AuthorizationResult.parseParameters(responseBody);
        if (queryParameters.containsKey("error")) {
            return new AuthorizationResult(AuthorizationStatus.ProtocolError, queryParameters.get("error"), !StringHelper.isBlank(queryParameters.get("error_description")) ? queryParameters.get("error_description") : null);
        }
        if (!queryParameters.containsKey("code")) {
            return new AuthorizationResult(AuthorizationStatus.UnknownError, "invalid_authorization_result", "Authorization result response does not contain authorization code");
        }
        AuthorizationResult result = new AuthorizationResult();
        result.code = queryParameters.get("code");
        result.status = AuthorizationStatus.Success;
        if (queryParameters.containsKey("cloud_instance_host_name")) {
            result.environment = queryParameters.get("cloud_instance_host_name");
        }
        if (queryParameters.containsKey("state")) {
            result.state = queryParameters.get("state");
        }
        return result;
    }

    private AuthorizationResult() {
    }

    private AuthorizationResult(AuthorizationStatus status, String error, String errorDescription) {
        this.status = status;
        this.error = error;
        this.errorDescription = errorDescription;
    }

    private static Map<String, String> parseParameters(String serverResponse) {
        LinkedHashMap<String, String> query_pairs = new LinkedHashMap<String, String>();
        try {
            String[] pairs;
            for (String pair : pairs = serverResponse.split("&")) {
                int idx = pair.indexOf("=");
                String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                query_pairs.put(key, value);
            }
        }
        catch (Exception ex) {
            throw new MsalClientException("invalid_authorization_result", String.format("Error parsing authorization result:  %s", ex.getMessage()));
        }
        return query_pairs;
    }

    static enum AuthorizationStatus {
        Success,
        ProtocolError,
        UnknownError;

    }
}

