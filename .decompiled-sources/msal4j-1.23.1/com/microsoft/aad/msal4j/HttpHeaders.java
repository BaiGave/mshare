/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.RequestContext;
import com.microsoft.aad.msal4j.StringHelper;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

final class HttpHeaders {
    static final String PRODUCT_HEADER_NAME = "x-client-SKU";
    static final String PRODUCT_HEADER_VALUE = "MSAL.Java";
    static final String PRODUCT_VERSION_HEADER_NAME = "x-client-VER";
    static final String PRODUCT_VERSION_HEADER_VALUE = HttpHeaders.getProductVersion();
    static final String OS_HEADER_NAME = "x-client-OS";
    static final String OS_HEADER_VALUE = System.getProperty("os.name");
    static final String APPLICATION_NAME_HEADER_NAME = "x-app-name";
    private final String applicationNameHeaderValue;
    static final String APPLICATION_VERSION_HEADER_NAME = "x-app-ver";
    private final String applicationVersionHeaderValue;
    static final String CORRELATION_ID_HEADER_NAME = "client-request-id";
    private final String correlationIdHeaderValue;
    private static final String REQUEST_CORRELATION_ID_IN_RESPONSE_HEADER_NAME = "return-client-request-id";
    private static final String REQUEST_CORRELATION_ID_IN_RESPONSE_HEADER_VALUE = "true";
    private static final String X_MS_LIB_CAPABILITY_NAME = "x-ms-lib-capability";
    private static final String X_MS_LIB_CAPABILITY_VALUE = "retry-after, h429";
    static final String X_ANCHOR_MAILBOX = "X-AnchorMailbox";
    static final String X_ANCHOR_MAILBOX_OID_FORMAT = "oid:%s";
    static final String X_ANCHOR_MAILBOX_UPN_FORMAT = "upn:%s";
    private String anchorMailboxHeaderValue = null;
    private String headerValues;
    private Map<String, String> headerMap = new HashMap<String, String>();

    HttpHeaders(RequestContext requestContext) {
        this.correlationIdHeaderValue = requestContext.correlationId();
        this.applicationNameHeaderValue = requestContext.applicationName();
        this.applicationVersionHeaderValue = requestContext.applicationVersion();
        if (requestContext.userIdentifier() != null) {
            String upn = requestContext.userIdentifier().upn();
            String oid = requestContext.userIdentifier().oid();
            if (!StringHelper.isBlank(upn)) {
                this.anchorMailboxHeaderValue = String.format(X_ANCHOR_MAILBOX_UPN_FORMAT, upn);
            } else if (!StringHelper.isBlank(oid)) {
                this.anchorMailboxHeaderValue = String.format(X_ANCHOR_MAILBOX_OID_FORMAT, oid);
            }
        }
        Map<String, String> extraHttpHeaders = requestContext.apiParameters() == null ? null : requestContext.apiParameters().extraHttpHeaders();
        this.initializeHeaders(extraHttpHeaders);
    }

    private void initializeHeaders(Map<String, String> extraHttpHeaders) {
        StringBuilder sb = new StringBuilder();
        BiConsumer<String, String> init = (key, val) -> {
            this.headerMap.put((String)key, (String)val);
            sb.append((String)key).append("=").append((String)val).append(";");
        };
        init.accept(PRODUCT_HEADER_NAME, PRODUCT_HEADER_VALUE);
        init.accept(PRODUCT_VERSION_HEADER_NAME, PRODUCT_VERSION_HEADER_VALUE);
        init.accept(OS_HEADER_NAME, OS_HEADER_VALUE);
        init.accept(REQUEST_CORRELATION_ID_IN_RESPONSE_HEADER_NAME, REQUEST_CORRELATION_ID_IN_RESPONSE_HEADER_VALUE);
        init.accept(CORRELATION_ID_HEADER_NAME, this.correlationIdHeaderValue);
        if (!StringHelper.isBlank(this.applicationNameHeaderValue)) {
            init.accept(APPLICATION_NAME_HEADER_NAME, this.applicationNameHeaderValue);
        }
        if (!StringHelper.isBlank(this.applicationVersionHeaderValue)) {
            init.accept(APPLICATION_VERSION_HEADER_NAME, this.applicationVersionHeaderValue);
        }
        if (!StringHelper.isBlank(this.anchorMailboxHeaderValue)) {
            init.accept(X_ANCHOR_MAILBOX, this.anchorMailboxHeaderValue);
        }
        init.accept(X_MS_LIB_CAPABILITY_NAME, X_MS_LIB_CAPABILITY_VALUE);
        if (extraHttpHeaders != null) {
            extraHttpHeaders.forEach(init);
        }
        this.headerValues = sb.toString();
    }

    Map<String, String> getReadonlyHeaderMap() {
        return Collections.unmodifiableMap(this.headerMap);
    }

    String getHeaderCorrelationIdValue() {
        return this.correlationIdHeaderValue;
    }

    public String toString() {
        return this.headerValues;
    }

    private static String getProductVersion() {
        if (HttpHeaders.class.getPackage().getImplementationVersion() == null) {
            return "1.0";
        }
        return HttpHeaders.class.getPackage().getImplementationVersion();
    }
}

