/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.AbstractApplicationBase;
import com.microsoft.aad.msal4j.DefaultRetryPolicy;
import com.microsoft.aad.msal4j.HttpEvent;
import com.microsoft.aad.msal4j.HttpRequest;
import com.microsoft.aad.msal4j.HttpUtils;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.IAcquireTokenParameters;
import com.microsoft.aad.msal4j.IHttpClient;
import com.microsoft.aad.msal4j.IHttpHelper;
import com.microsoft.aad.msal4j.IHttpResponse;
import com.microsoft.aad.msal4j.IRetryPolicy;
import com.microsoft.aad.msal4j.LogHelper;
import com.microsoft.aad.msal4j.MsalClientException;
import com.microsoft.aad.msal4j.MsalThrottlingException;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.RequestContext;
import com.microsoft.aad.msal4j.ServiceBundle;
import com.microsoft.aad.msal4j.SilentParameters;
import com.microsoft.aad.msal4j.StringHelper;
import com.microsoft.aad.msal4j.TelemetryHelper;
import com.microsoft.aad.msal4j.TelemetryManager;
import com.microsoft.aad.msal4j.ThrottlingCache;
import com.microsoft.aad.msal4j.XmsClientTelemetryInfo;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class HttpHelper
implements IHttpHelper {
    private static final Logger log = LoggerFactory.getLogger(HttpHelper.class);
    public static final String RETRY_AFTER_HEADER = "Retry-After";
    private IHttpClient httpClient;
    private IRetryPolicy retryPolicy;
    private boolean retryDisabled;

    HttpHelper(IHttpClient httpClient, IRetryPolicy retryPolicy) {
        this.httpClient = httpClient;
        this.retryPolicy = retryPolicy != null ? retryPolicy : new DefaultRetryPolicy();
    }

    HttpHelper(AbstractApplicationBase application, IRetryPolicy retryPolicy) {
        this.httpClient = application.httpClient();
        this.retryDisabled = application.isRetryDisabled();
        this.retryPolicy = retryPolicy != null ? retryPolicy : new DefaultRetryPolicy();
    }

    @Override
    public IHttpResponse executeHttpRequest(HttpRequest httpRequest, RequestContext requestContext, ServiceBundle serviceBundle) {
        IHttpResponse httpResponse;
        this.checkForThrottling(requestContext);
        HttpEvent httpEvent = new HttpEvent();
        try (TelemetryHelper telemetryHelper = serviceBundle.getTelemetryManager().createTelemetryHelper(requestContext.telemetryRequestId(), requestContext.clientId(), httpEvent, false);){
            this.addRequestInfoToTelemetry(httpRequest, httpEvent);
            try {
                httpResponse = this.executeHttpRequestWithRetries(httpRequest, this.httpClient);
            }
            catch (Exception e) {
                httpEvent.setOauthErrorCode("unknown");
                throw new MsalClientException(e);
            }
            this.addResponseInfoToTelemetry(httpResponse, httpEvent);
            if (httpResponse.headers() != null) {
                HttpHelper.verifyReturnedCorrelationId(httpRequest, httpResponse);
            }
        }
        this.processThrottlingInstructions(httpResponse, requestContext);
        return httpResponse;
    }

    IHttpResponse executeHttpRequest(HttpRequest httpRequest, RequestContext requestContext, TelemetryManager telemetryManager, IHttpClient httpClient) {
        IHttpResponse httpResponse;
        this.checkForThrottling(requestContext);
        HttpEvent httpEvent = new HttpEvent();
        try (TelemetryHelper telemetryHelper = telemetryManager.createTelemetryHelper(requestContext.telemetryRequestId(), requestContext.clientId(), httpEvent, false);){
            this.addRequestInfoToTelemetry(httpRequest, httpEvent);
            try {
                httpResponse = this.executeHttpRequestWithRetries(httpRequest, httpClient);
            }
            catch (Exception e) {
                httpEvent.setOauthErrorCode("unknown");
                throw new MsalClientException(e);
            }
            this.addResponseInfoToTelemetry(httpResponse, httpEvent);
            if (httpResponse.headers() != null) {
                HttpHelper.verifyReturnedCorrelationId(httpRequest, httpResponse);
            }
        }
        this.processThrottlingInstructions(httpResponse, requestContext);
        return httpResponse;
    }

    IHttpResponse executeHttpRequest(HttpRequest httpRequest) {
        IHttpResponse httpResponse;
        try {
            httpResponse = this.executeHttpRequestWithRetries(httpRequest, this.httpClient);
        }
        catch (Exception e) {
            throw new MsalClientException(e);
        }
        if (httpResponse.headers() != null) {
            HttpHelper.verifyReturnedCorrelationId(httpRequest, httpResponse);
        }
        return httpResponse;
    }

    private String getRequestThumbprint(RequestContext requestContext) {
        IAccount account;
        StringBuilder sb = new StringBuilder();
        sb.append(requestContext.clientId() + ".");
        sb.append(requestContext.authority() + ".");
        IAcquireTokenParameters apiParameters = requestContext.apiParameters();
        if (apiParameters instanceof SilentParameters && (account = ((SilentParameters)apiParameters).account()) != null) {
            sb.append(account.homeAccountId() + ".");
        }
        TreeSet<String> sortedScopes = new TreeSet<String>(apiParameters.scopes());
        sb.append(String.join((CharSequence)" ", sortedScopes));
        return StringHelper.createSha256Hash(sb.toString());
    }

    IHttpResponse executeHttpRequestWithRetries(HttpRequest httpRequest, IHttpClient httpClient) throws Exception {
        IHttpResponse httpResponse = httpClient.send(httpRequest);
        if (this.retryDisabled) {
            return httpResponse;
        }
        int maxRetries = this.retryPolicy.getMaxRetryCount(httpResponse);
        for (int retryCount = 0; this.retryPolicy.isRetryable(httpResponse) && retryCount < maxRetries; ++retryCount) {
            Thread.sleep(this.retryPolicy.getRetryDelayMs(httpResponse));
            httpResponse = httpClient.send(httpRequest);
        }
        return httpResponse;
    }

    private void checkForThrottling(RequestContext requestContext) {
        String requestThumbprint;
        long retryInMs;
        if (requestContext.clientApplication() instanceof PublicClientApplication && requestContext.apiParameters() != null && (retryInMs = ThrottlingCache.retryInMs(requestThumbprint = this.getRequestThumbprint(requestContext))) > 0L) {
            throw new MsalThrottlingException(retryInMs);
        }
    }

    private void processThrottlingInstructions(IHttpResponse httpResponse, RequestContext requestContext) {
        if (requestContext.clientApplication() instanceof PublicClientApplication) {
            Long expirationTimestamp = null;
            Integer retryAfterHeaderVal = HttpHelper.getRetryAfterHeader(httpResponse);
            if (retryAfterHeaderVal != null) {
                expirationTimestamp = System.currentTimeMillis() + (long)(retryAfterHeaderVal * 1000);
            } else if (httpResponse.statusCode() == 429 || httpResponse.statusCode() >= 500) {
                expirationTimestamp = System.currentTimeMillis() + (long)(ThrottlingCache.DEFAULT_THROTTLING_TIME_SEC * 1000);
            }
            if (expirationTimestamp != null) {
                ThrottlingCache.set(this.getRequestThumbprint(requestContext), expirationTimestamp);
            }
        }
    }

    static Integer getRetryAfterHeader(IHttpResponse httpResponse) {
        if (httpResponse.headers() != null) {
            TreeMap<String, List<String>> headers = new TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER);
            headers.putAll(httpResponse.headers());
            if (headers.containsKey(RETRY_AFTER_HEADER) && ((List)headers.get(RETRY_AFTER_HEADER)).size() == 1) {
                try {
                    int headerValue = Integer.parseInt((String)((List)headers.get(RETRY_AFTER_HEADER)).get(0));
                    if (headerValue > 0 && headerValue <= 3600) {
                        return headerValue;
                    }
                }
                catch (NumberFormatException ex) {
                    log.warn("Failed to parse value of Retry-After header - NumberFormatException");
                }
            }
        }
        return null;
    }

    private void addRequestInfoToTelemetry(HttpRequest httpRequest, HttpEvent httpEvent) {
        try {
            httpEvent.setHttpPath(httpRequest.url().toURI());
            httpEvent.setHttpMethod(httpRequest.httpMethod().toString());
            if (!StringHelper.isBlank(httpRequest.url().getQuery())) {
                httpEvent.setQueryParameters(httpRequest.url().getQuery());
            }
        }
        catch (Exception ex) {
            String correlationId = httpRequest.headerValue("client-request-id");
            log.warn(LogHelper.createMessage("Setting URL telemetry fields failed: " + LogHelper.getPiiScrubbedDetails(ex), correlationId != null ? correlationId : ""));
        }
    }

    private void addResponseInfoToTelemetry(IHttpResponse httpResponse, HttpEvent httpEvent) {
        XmsClientTelemetryInfo xmsClientTelemetryInfo;
        String xMsClientTelemetry;
        String xMsRequestId;
        httpEvent.setHttpResponseStatus(httpResponse.statusCode());
        Map<String, List<String>> headers = httpResponse.headers();
        String userAgent = HttpUtils.headerValue(headers, "User-Agent");
        if (!StringHelper.isBlank(userAgent)) {
            httpEvent.setUserAgent(userAgent);
        }
        if (!StringHelper.isBlank(xMsRequestId = HttpUtils.headerValue(headers, "x-ms-request-id"))) {
            httpEvent.setRequestIdHeader(xMsRequestId);
        }
        if ((xMsClientTelemetry = HttpUtils.headerValue(headers, "x-ms-clitelem")) != null && (xmsClientTelemetryInfo = XmsClientTelemetryInfo.parseXmsTelemetryInfo(xMsClientTelemetry)) != null) {
            httpEvent.setXmsClientTelemetryInfo(xmsClientTelemetryInfo);
        }
    }

    private static void verifyReturnedCorrelationId(HttpRequest httpRequest, IHttpResponse httpResponse) {
        String sentCorrelationId = httpRequest.headerValue("client-request-id");
        String returnedCorrelationId = HttpUtils.headerValue(httpResponse.headers(), "client-request-id");
        if (StringHelper.isBlank(returnedCorrelationId) || !returnedCorrelationId.equals(sentCorrelationId)) {
            String msg = LogHelper.createMessage(String.format("Sent (%s) Correlation Id is not same as received (%s).", sentCorrelationId, returnedCorrelationId), sentCorrelationId);
            log.info(msg);
        }
    }

    void setRetryPolicy(IRetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }
}

