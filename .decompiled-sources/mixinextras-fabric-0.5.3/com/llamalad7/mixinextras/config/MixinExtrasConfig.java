/*
 * Decompiled with CFR 0.152.
 */
package com.llamalad7.mixinextras.config;

import com.llamalad7.mixinextras.lib.gson.annotations.SerializedName;
import com.llamalad7.mixinextras.lib.semver.Version;
import com.llamalad7.mixinextras.service.MixinExtrasVersion;

public class MixinExtrasConfig {
    @SerializedName(value="minVersion")
    private final String minVersionString;
    private final transient String configName;
    public final transient MixinExtrasConfig parent;
    public final transient MixinExtrasVersion minVersion;

    public MixinExtrasConfig(String configName, MixinExtrasConfig parent, String minVersion) {
        this.configName = configName;
        this.parent = parent;
        this.minVersionString = minVersion;
        this.minVersion = minVersion != null ? this.determineMinVersion() : (parent != null ? parent.minVersion : null);
    }

    private MixinExtrasVersion determineMinVersion() {
        if (this.minVersionString == null) {
            return null;
        }
        Version min = Version.tryParse(this.minVersionString).orElseThrow(() -> new IllegalArgumentException(String.format("'%s' is not valid SemVer!", this.minVersionString)));
        MixinExtrasVersion[] versions = MixinExtrasVersion.values();
        if (min.isHigherThan(MixinExtrasVersion.LATEST.getSemver())) {
            throw new IllegalArgumentException(String.format("Mixin Config %s requires MixinExtras >=%s but %s is present!", new Object[]{this.configName, min, MixinExtrasVersion.LATEST}));
        }
        MixinExtrasVersion result = versions[0];
        for (MixinExtrasVersion version : versions) {
            if (version.getSemver().isHigherThan(min)) break;
            result = version;
        }
        return result;
    }
}

