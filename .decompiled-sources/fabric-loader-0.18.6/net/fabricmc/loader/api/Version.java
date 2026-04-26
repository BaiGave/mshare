/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.api;

import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.impl.util.version.VersionParser;

public interface Version
extends Comparable<Version> {
    public String getFriendlyString();

    public static Version parse(String string) throws VersionParsingException {
        return VersionParser.parse(string, false);
    }
}

