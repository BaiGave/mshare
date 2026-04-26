/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;

@GwtCompatible
final class EmptyImmutableListMultimap
extends ImmutableListMultimap<Object, Object> {
    static final EmptyImmutableListMultimap INSTANCE = new EmptyImmutableListMultimap();
    @GwtIncompatible
    @J2ktIncompatible
    private static final long serialVersionUID = 0L;

    private EmptyImmutableListMultimap() {
        super(ImmutableMap.of(), 0);
    }

    @Override
    public ImmutableMap<Object, Collection<Object>> asMap() {
        return super.asMap();
    }

    private Object readResolve() {
        return INSTANCE;
    }
}

