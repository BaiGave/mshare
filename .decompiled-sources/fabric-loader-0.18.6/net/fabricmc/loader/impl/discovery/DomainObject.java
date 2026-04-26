/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.discovery;

import net.fabricmc.loader.api.Version;

interface DomainObject {
    public String getId();

    public static interface Mod
    extends DomainObject {
        public Version getVersion();
    }
}

