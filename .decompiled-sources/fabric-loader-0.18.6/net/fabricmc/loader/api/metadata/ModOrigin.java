/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.api.metadata;

import java.nio.file.Path;
import java.util.List;

public interface ModOrigin {
    public Kind getKind();

    public List<Path> getPaths();

    public String getParentModId();

    public String getParentSubLocation();

    public static enum Kind {
        PATH,
        NESTED,
        UNKNOWN;

    }
}

