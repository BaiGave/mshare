/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.CurrentRequest;
import com.microsoft.aad.msal4j.StringHelper;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ServerSideTelemetry {
    private static final Logger log = LoggerFactory.getLogger(ServerSideTelemetry.class);
    private static final String SCHEMA_VERSION = "5";
    private static final String SCHEMA_PIPE_DELIMITER = "|";
    private static final String SCHEMA_COMMA_DELIMITER = ",";
    private static final String CURRENT_REQUEST_HEADER_NAME = "x-client-current-telemetry";
    private static final String LAST_REQUEST_HEADER_NAME = "x-client-last-telemetry";
    private static final int CURRENT_REQUEST_MAX_SIZE = 100;
    private static final int LAST_REQUEST_MAX_SIZE = 350;
    private CurrentRequest currentRequest;
    private AtomicInteger silentSuccessfulCount = new AtomicInteger(0);
    ConcurrentMap<String, String[]> previousRequests = new ConcurrentHashMap<String, String[]>();
    ConcurrentMap<String, String[]> previousRequestInProgress = new ConcurrentHashMap<String, String[]>();

    ServerSideTelemetry() {
    }

    synchronized Map<String, String> getServerTelemetryHeaderMap() {
        HashMap<String, String> headerMap = new HashMap<String, String>();
        headerMap.put(CURRENT_REQUEST_HEADER_NAME, this.buildCurrentRequestHeader());
        headerMap.put(LAST_REQUEST_HEADER_NAME, this.buildLastRequestHeader());
        return headerMap;
    }

    void addFailedRequestTelemetry(String publicApiId, String correlationId, String error) {
        String[] previousRequest = new String[]{publicApiId, error};
        this.previousRequests.put(correlationId, previousRequest);
    }

    void incrementSilentSuccessfulCount() {
        this.silentSuccessfulCount.incrementAndGet();
    }

    synchronized CurrentRequest getCurrentRequest() {
        return this.currentRequest;
    }

    synchronized void setCurrentRequest(CurrentRequest currentRequest) {
        this.currentRequest = currentRequest;
    }

    private synchronized String buildCurrentRequestHeader() {
        if (this.currentRequest == null) {
            return StringHelper.EMPTY_STRING;
        }
        String currentRequestHeader = "5|" + this.currentRequest.publicApi().getApiId() + SCHEMA_COMMA_DELIMITER + this.currentRequest.cacheInfo().telemetryValue + SCHEMA_COMMA_DELIMITER + this.currentRequest.regionUsed() + SCHEMA_COMMA_DELIMITER + this.currentRequest.regionSource() + SCHEMA_COMMA_DELIMITER + this.currentRequest.regionOutcome() + SCHEMA_PIPE_DELIMITER;
        if (currentRequestHeader.getBytes(StandardCharsets.UTF_8).length > 100) {
            log.warn("Current request telemetry header greater than 100 bytes");
        }
        return currentRequestHeader;
    }

    private synchronized String buildLastRequestHeader() {
        StringBuilder lastRequestBuilder = new StringBuilder();
        lastRequestBuilder.append(SCHEMA_VERSION).append(SCHEMA_PIPE_DELIMITER).append(this.silentSuccessfulCount.getAndSet(0));
        int baseLength = lastRequestBuilder.toString().getBytes(StandardCharsets.UTF_8).length;
        if (this.previousRequests.isEmpty()) {
            return lastRequestBuilder.append(SCHEMA_PIPE_DELIMITER).append(SCHEMA_PIPE_DELIMITER).append(SCHEMA_PIPE_DELIMITER).toString();
        }
        StringBuilder middleSegmentBuilder = new StringBuilder(SCHEMA_PIPE_DELIMITER);
        StringBuilder errorSegmentBuilder = new StringBuilder(SCHEMA_PIPE_DELIMITER);
        Iterator it = this.previousRequests.keySet().iterator();
        String lastRequest = lastRequestBuilder.toString() + SCHEMA_PIPE_DELIMITER + SCHEMA_PIPE_DELIMITER;
        while (it.hasNext()) {
            String correlationId = (String)it.next();
            String[] previousRequest = (String[])this.previousRequests.get(correlationId);
            String apiId = (String)Array.get(previousRequest, 0);
            String error = (String)Array.get(previousRequest, 1);
            middleSegmentBuilder.append(apiId).append(SCHEMA_COMMA_DELIMITER).append(correlationId);
            errorSegmentBuilder.append(error);
            int lastRequestLength = baseLength + middleSegmentBuilder.toString().getBytes(StandardCharsets.UTF_8).length + errorSegmentBuilder.toString().getBytes(StandardCharsets.UTF_8).length;
            if (lastRequestLength >= 349) break;
            lastRequest = lastRequestBuilder.toString() + middleSegmentBuilder.toString() + errorSegmentBuilder.toString();
            this.previousRequestInProgress.put(correlationId, previousRequest);
            it.remove();
            if (!it.hasNext()) continue;
            middleSegmentBuilder.append(SCHEMA_COMMA_DELIMITER);
            errorSegmentBuilder.append(SCHEMA_COMMA_DELIMITER);
        }
        return lastRequest + SCHEMA_PIPE_DELIMITER;
    }
}

