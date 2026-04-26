/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation.jackson.core.util;

import com.azure.json.implementation.jackson.core.util.JacksonFeature;

public final class JacksonFeatureSet<F extends JacksonFeature> {
    private final int _enabled;

    private JacksonFeatureSet(int bitmask) {
        this._enabled = bitmask;
    }

    public static <F extends JacksonFeature> JacksonFeatureSet<F> fromDefaults(F[] allFeatures) {
        if (allFeatures.length > 31) {
            String desc = allFeatures[0].getClass().getName();
            throw new IllegalArgumentException(String.format("Can not use type `%s` with JacksonFeatureSet: too many entries (%d > 31)", desc, allFeatures.length));
        }
        int flags = 0;
        for (F f : allFeatures) {
            if (!f.enabledByDefault()) continue;
            flags |= f.getMask();
        }
        return new JacksonFeatureSet<F>(flags);
    }

    public JacksonFeatureSet<F> with(F feature) {
        int newMask = this._enabled | feature.getMask();
        return newMask == this._enabled ? this : new JacksonFeatureSet<F>(newMask);
    }

    public JacksonFeatureSet<F> without(F feature) {
        int newMask = this._enabled & ~feature.getMask();
        return newMask == this._enabled ? this : new JacksonFeatureSet<F>(newMask);
    }

    public boolean isEnabled(F feature) {
        return (feature.getMask() & this._enabled) != 0;
    }
}

