/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.Event;
import java.util.Objects;

class EventKey {
    private String requestId;
    private String eventName;

    EventKey(String requestId, Event event) {
        this.requestId = requestId;
        this.eventName = (String)event.get("event_name");
    }

    public String getRequestId() {
        return this.requestId;
    }

    public String getEventName() {
        return this.eventName;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof EventKey)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        EventKey eventKey = (EventKey)obj;
        return Objects.equals(this.requestId, eventKey.getRequestId()) && Objects.equals(this.eventName, eventKey.getEventName());
    }

    public int hashCode() {
        return Objects.hash(this.requestId, this.eventName);
    }
}

