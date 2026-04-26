/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.Event;
import com.microsoft.aad.msal4j.TelemetryHelper;

interface ITelemetryManager {
    public String generateRequestId();

    public TelemetryHelper createTelemetryHelper(String var1, String var2, Event var3, Boolean var4);
}

