/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.util.version;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionComparisonOperator;
import net.fabricmc.loader.api.metadata.version.VersionInterval;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import net.fabricmc.loader.impl.util.version.SemanticVersionImpl;
import net.fabricmc.loader.impl.util.version.VersionIntervalImpl;
import net.fabricmc.loader.impl.util.version.VersionParser;

public final class VersionPredicateParser {
    private static final VersionComparisonOperator[] OPERATORS = VersionComparisonOperator.values();

    public static VersionPredicate parse(String predicate) throws VersionParsingException {
        ArrayList<SingleVersionPredicate> predicateList = new ArrayList<SingleVersionPredicate>();
        for (String s : predicate.split(" ")) {
            Version version;
            if ((s = s.trim()).isEmpty() || s.equals("*")) continue;
            VersionComparisonOperator operator = VersionComparisonOperator.EQUAL;
            for (VersionComparisonOperator op : OPERATORS) {
                if (!s.startsWith(op.getSerialized())) continue;
                operator = op;
                s = s.substring(op.getSerialized().length());
                break;
            }
            if ((version = VersionParser.parse(s, true)) instanceof SemanticVersion) {
                SemanticVersion semVer = (SemanticVersion)version;
                if (semVer.hasWildcard()) {
                    if (operator != VersionComparisonOperator.EQUAL) {
                        throw new VersionParsingException("Invalid predicate: " + predicate + ", version ranges with wildcards (.X) require using the equality operator or no operator at all!");
                    }
                    assert (!semVer.getPrereleaseKey().isPresent());
                    int compCount = semVer.getVersionComponentCount();
                    assert (compCount == 2 || compCount == 3);
                    operator = compCount == 2 ? VersionComparisonOperator.SAME_TO_NEXT_MAJOR : VersionComparisonOperator.SAME_TO_NEXT_MINOR;
                    int[] newComponents = new int[semVer.getVersionComponentCount() - 1];
                    for (int i = 0; i < semVer.getVersionComponentCount() - 1; ++i) {
                        newComponents[i] = semVer.getVersionComponent(i);
                    }
                    version = new SemanticVersionImpl(newComponents, "", semVer.getBuildKey().orElse(null));
                }
            } else {
                if (!operator.isMinInclusive() && !operator.isMaxInclusive()) {
                    throw new VersionParsingException("Invalid predicate: " + predicate + ", version ranges need to be semantic version compatible to use operators that exclude the bound!");
                }
                operator = VersionComparisonOperator.EQUAL;
            }
            predicateList.add(new SingleVersionPredicate(operator, version));
        }
        if (predicateList.isEmpty()) {
            return AnyVersionPredicate.INSTANCE;
        }
        if (predicateList.size() == 1) {
            return (VersionPredicate)predicateList.get(0);
        }
        return new MultiVersionPredicate(predicateList);
    }

    public static Set<VersionPredicate> parse(Collection<String> predicates) throws VersionParsingException {
        HashSet<VersionPredicate> ret = new HashSet<VersionPredicate>(predicates.size());
        for (String version : predicates) {
            ret.add(VersionPredicateParser.parse(version));
        }
        return ret;
    }

    public static VersionPredicate getAny() {
        return AnyVersionPredicate.INSTANCE;
    }

    static class SingleVersionPredicate
    implements VersionPredicate,
    VersionPredicate.PredicateTerm {
        private final VersionComparisonOperator operator;
        private final Version refVersion;

        SingleVersionPredicate(VersionComparisonOperator operator, Version refVersion) {
            this.operator = operator;
            this.refVersion = refVersion;
        }

        @Override
        public boolean test(Version version) {
            Objects.requireNonNull(version, "null version");
            return this.operator.test(version, this.refVersion);
        }

        public List<VersionPredicate.PredicateTerm> getTerms() {
            return Collections.singletonList(this);
        }

        @Override
        public VersionInterval getInterval() {
            if (this.refVersion instanceof SemanticVersion) {
                SemanticVersion version = (SemanticVersion)this.refVersion;
                return new VersionIntervalImpl(this.operator.minVersion(version), this.operator.isMinInclusive(), this.operator.maxVersion(version), this.operator.isMaxInclusive());
            }
            return new VersionIntervalImpl(this.refVersion, true, this.refVersion, true);
        }

        @Override
        public VersionComparisonOperator getOperator() {
            return this.operator;
        }

        @Override
        public Version getReferenceVersion() {
            return this.refVersion;
        }

        public boolean equals(Object obj) {
            if (obj instanceof SingleVersionPredicate) {
                SingleVersionPredicate o = (SingleVersionPredicate)obj;
                return this.operator == o.operator && this.refVersion.equals(o.refVersion);
            }
            return false;
        }

        public int hashCode() {
            return this.operator.ordinal() * 31 + this.refVersion.hashCode();
        }

        public String toString() {
            return this.operator.getSerialized().concat(this.refVersion.toString());
        }
    }

    static class AnyVersionPredicate
    implements VersionPredicate {
        static final VersionPredicate INSTANCE = new AnyVersionPredicate();

        private AnyVersionPredicate() {
        }

        @Override
        public boolean test(Version t) {
            return true;
        }

        public List<? extends VersionPredicate.PredicateTerm> getTerms() {
            return Collections.emptyList();
        }

        @Override
        public VersionInterval getInterval() {
            return VersionIntervalImpl.INFINITE;
        }

        public String toString() {
            return "*";
        }
    }

    static class MultiVersionPredicate
    implements VersionPredicate {
        private final List<SingleVersionPredicate> predicates;

        MultiVersionPredicate(List<SingleVersionPredicate> predicates) {
            this.predicates = predicates;
        }

        @Override
        public boolean test(Version version) {
            Objects.requireNonNull(version, "null version");
            for (SingleVersionPredicate predicate : this.predicates) {
                if (predicate.test(version)) continue;
                return false;
            }
            return true;
        }

        public List<? extends VersionPredicate.PredicateTerm> getTerms() {
            return this.predicates;
        }

        @Override
        public VersionInterval getInterval() {
            if (this.predicates.isEmpty()) {
                return AnyVersionPredicate.INSTANCE.getInterval();
            }
            VersionInterval ret = this.predicates.get(0).getInterval();
            for (int i = 1; i < this.predicates.size(); ++i) {
                ret = VersionIntervalImpl.and(ret, this.predicates.get(i).getInterval());
            }
            return ret;
        }

        public boolean equals(Object obj) {
            if (obj instanceof MultiVersionPredicate) {
                MultiVersionPredicate o = (MultiVersionPredicate)obj;
                return this.predicates.equals(o.predicates);
            }
            return false;
        }

        public int hashCode() {
            return this.predicates.hashCode();
        }

        public String toString() {
            StringBuilder ret = new StringBuilder();
            for (SingleVersionPredicate predicate : this.predicates) {
                if (ret.length() > 0) {
                    ret.append(' ');
                }
                ret.append(predicate.toString());
            }
            return ret.toString();
        }
    }
}

