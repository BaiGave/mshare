/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.util.version;

import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.impl.util.version.SemanticVersionImpl;
import net.fabricmc.loader.impl.util.version.StringVersion;

public final class VersionParser {
    public static Version parse(String s, boolean storeX) throws VersionParsingException {
        Version version;
        if (s == null || s.isEmpty()) {
            throw new VersionParsingException("Version must be a non-empty string!");
        }
        try {
            version = new SemanticVersionImpl(s, storeX);
        }
        catch (VersionParsingException e) {
            version = new StringVersion(s);
        }
        return version;
    }

    public static SemanticVersion parseSemantic(String s) throws VersionParsingException {
        if (s == null || s.isEmpty()) {
            throw new VersionParsingException("Version must be a non-empty string!");
        }
        return new SemanticVersionImpl(s, false);
    }
}

