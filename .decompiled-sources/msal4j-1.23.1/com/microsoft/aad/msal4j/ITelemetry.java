/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.Event;

interface ITelemetry {
    public void startEvent(String var1, Event var2);

    public void stopEvent(String var1, Event var2);

    public void flush(String var1, String var2);
}

