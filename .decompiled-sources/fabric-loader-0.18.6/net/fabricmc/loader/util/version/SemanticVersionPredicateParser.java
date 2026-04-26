/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.util.version;

import java.util.function.Predicate;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import net.fabricmc.loader.util.version.SemanticVersionImpl;

@Deprecated
public final class SemanticVersionPredicateParser {
    public static Predicate<SemanticVersionImpl> create(String text) throws VersionParsingException {
        VersionPredicate predicate = VersionPredicate.parse(text);
        return v -> predicate.test(v);
    }
}

