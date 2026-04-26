/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.api.metadata.version;

import java.util.Collection;
import java.util.function.Predicate;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionComparisonOperator;
import net.fabricmc.loader.api.metadata.version.VersionInterval;
import net.fabricmc.loader.impl.util.version.VersionPredicateParser;

public interface VersionPredicate
extends Predicate<Version> {
    public Collection<? extends PredicateTerm> getTerms();

    public VersionInterval getInterval();

    public static VersionPredicate parse(String predicate) throws VersionParsingException {
        return VersionPredicateParser.parse(predicate);
    }

    public static Collection<VersionPredicate> parse(Collection<String> predicates) throws VersionParsingException {
        return VersionPredicateParser.parse(predicates);
    }

    public static interface PredicateTerm {
        public VersionComparisonOperator getOperator();

        public Version getReferenceVersion();
    }
}

