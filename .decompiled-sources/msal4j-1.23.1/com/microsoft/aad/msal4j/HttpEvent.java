/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.Event;
import com.microsoft.aad.msal4j.StringHelper;
import com.microsoft.aad.msal4j.XmsClientTelemetryInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.Locale;

class HttpEvent
extends Event {
    private static final String HTTP_PATH_KEY = "msal.http_path";
    private static final String USER_AGENT_KEY = "msal.user_agent";
    private static final String QUERY_PARAMETERS_KEY = "msal.query_parameters";
    private static final String API_VERSION_KEY = "msal.api_version";
    private static final String RESPONSE_CODE_KEY = "msal.response_code";
    private static final String OAUTH_ERROR_CODE_KEY = "msal.oauth_error_code";
    private static final String HTTP_METHOD_KEY = "msal.http_method";
    private static final String REQUEST_ID_HEADER_KEY = "msal.request_id_header";
    private static final String TOKEN_AGEN_KEY = "msal.token_age";
    private static final String SPE_INFO_KEY = "msal.spe_info";
    private static final String SERVER_ERROR_CODE_KEY = "msal.server_error_code";
    private static final String SERVER_SUB_ERROR_CODE_KEY = "msal.server_sub_error_code";

    HttpEvent() {
        super("msal.http_event");
    }

    void setHttpPath(URI httpPath) {
        this.put(HTTP_PATH_KEY, HttpEvent.scrubTenant(httpPath));
    }

    void setUserAgent(String userAgent) {
        this.put(USER_AGENT_KEY, userAgent.toLowerCase(Locale.ROOT));
    }

    void setQueryParameters(String queryParameters) {
        this.put(QUERY_PARAMETERS_KEY, String.join((CharSequence)"&", this.parseQueryParametersAndReturnKeys(queryParameters)));
    }

    void setApiVersion(String apiVersion) {
        this.put(API_VERSION_KEY, apiVersion.toLowerCase());
    }

    void setHttpResponseStatus(Integer httpResponseStatus) {
        this.put(RESPONSE_CODE_KEY, httpResponseStatus.toString().toLowerCase());
    }

    void setHttpMethod(String httpMethod) {
        this.put(HTTP_METHOD_KEY, httpMethod);
    }

    void setOauthErrorCode(String oauthErrorCode) {
        this.put(OAUTH_ERROR_CODE_KEY, oauthErrorCode.toLowerCase());
    }

    void setRequestIdHeader(String requestIdHeader) {
        this.put(REQUEST_ID_HEADER_KEY, requestIdHeader.toLowerCase());
    }

    private void setTokenAge(String tokenAge) {
        this.put(TOKEN_AGEN_KEY, tokenAge.toLowerCase());
    }

    private void setSpeInfo(String speInfo) {
        this.put(SPE_INFO_KEY, speInfo.toLowerCase());
    }

    private void setServerErrorCode(String serverErrorCode) {
        this.put(SERVER_ERROR_CODE_KEY, serverErrorCode.toLowerCase());
    }

    private void setSubServerErrorCode(String subServerErrorCode) {
        this.put(SERVER_SUB_ERROR_CODE_KEY, subServerErrorCode.toLowerCase());
    }

    void setXmsClientTelemetryInfo(XmsClientTelemetryInfo xmsClientTelemetryInfo) {
        this.setTokenAge(xmsClientTelemetryInfo.getTokenAge());
        this.setSpeInfo(xmsClientTelemetryInfo.getSpeInfo());
        this.setServerErrorCode(xmsClientTelemetryInfo.getServerErrorCode());
        this.setSubServerErrorCode(xmsClientTelemetryInfo.getServerSubErrorCode());
    }

    private ArrayList<String> parseQueryParametersAndReturnKeys(String queryParams) {
        String[] queryStrings;
        ArrayList<String> queryKeys = new ArrayList<String>();
        for (String queryString : queryStrings = queryParams.split("&")) {
            String[] queryPairs = queryString.split("=");
            if (queryPairs.length != 2 || StringHelper.isBlank(queryPairs[0]) || StringHelper.isBlank(queryPairs[1])) continue;
            queryKeys.add(queryPairs[0].toLowerCase(Locale.ROOT));
        }
        return queryKeys;
    }
}

