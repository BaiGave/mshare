/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.Event;
import java.util.Map;

class DefaultEvent
extends Event {
    private static final String CLIENT_ID_KEY = "msal.client_id";
    private static final String SDK_PLATFORM_KEY = "msal.sdk_platform";
    private static final String SDK_VERSION_KEY = "msal.sdk_version";
    private static final String HTTP_EVENT_COUNT_KEY = "msal.http_event_count";
    private static final String CACHE_EVENT_COUNT_KEY = "msal.cache_event_count";
    private Map<String, Integer> eventCount;

    public DefaultEvent(String clientId, Map<String, Integer> eventCount) {
        super("msal.default_event");
        this.setClientId(clientId);
        this.setSdkPlatform();
        this.setSdkVersion();
        this.eventCount = eventCount;
        this.setHttpEventCount();
        this.setCacheEventCount();
    }

    private void setClientId(String clientId) {
        this.put(CLIENT_ID_KEY, clientId);
    }

    private void setSdkPlatform() {
        this.put(SDK_PLATFORM_KEY, System.getProperty("os.name"));
    }

    private void setSdkVersion() {
        this.put(SDK_VERSION_KEY, this.getClass().getPackage().getImplementationVersion());
    }

    private void setHttpEventCount() {
        this.put(HTTP_EVENT_COUNT_KEY, this.getEventCount("msal.http_event"));
    }

    private void setCacheEventCount() {
        this.put(CACHE_EVENT_COUNT_KEY, this.getEventCount("msal.cache_event"));
    }

    private String getEventCount(String eventName) {
        return this.eventCount.getOrDefault(eventName, 0).toString();
    }
}

