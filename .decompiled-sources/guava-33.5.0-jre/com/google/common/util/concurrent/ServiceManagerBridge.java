/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.util.concurrent.Service;

@J2ktIncompatible
@GwtIncompatible
interface ServiceManagerBridge {
    public ImmutableMultimap<Service.State, Service> servicesByState();
}

