/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.hard.data;

public final class SoftInterface {
    private String target;
    private String prefix;
    private Remap remap;

    public String getTarget() {
        return this.target;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public Remap getRemap() {
        return this.remap;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setRemap(Remap remap) {
        this.remap = remap;
    }

    public String toString() {
        return "Interface{target='" + this.target + '\'' + ", prefix='" + this.prefix + '\'' + ", remap=" + (Object)((Object)this.remap) + '}';
    }

    public static enum Remap {
        NONE,
        ONLY_PREFIX,
        ALL,
        FORCE;

    }
}

