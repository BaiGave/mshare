/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.text;

import com.ibm.icu.impl.PluralRulesLoader;
import com.ibm.icu.impl.StandardPlural;
import com.ibm.icu.impl.number.DecimalQuantity;
import com.ibm.icu.impl.number.DecimalQuantity_DualStorageBCD;
import com.ibm.icu.impl.number.range.StandardPluralRanges;
import com.ibm.icu.number.FormattedNumber;
import com.ibm.icu.number.FormattedNumberRange;
import com.ibm.icu.text.PluralRulesSerialProxy;
import com.ibm.icu.text.UnicodeSet;
import com.ibm.icu.util.Output;
import com.ibm.icu.util.ULocale;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class PluralRules
implements Serializable {
    static final UnicodeSet ALLOWED_ID = new UnicodeSet("[a-z]").freeze();
    private static final String CATEGORY_SEPARATOR = ";  ";
    private static final long serialVersionUID = 1L;
    private final RuleList rules;
    private final transient Set<String> keywords;
    private final transient StandardPluralRanges standardPluralRanges;
    public static final String KEYWORD_ZERO = "zero";
    public static final String KEYWORD_ONE = "one";
    public static final String KEYWORD_TWO = "two";
    public static final String KEYWORD_FEW = "few";
    public static final String KEYWORD_MANY = "many";
    public static final String KEYWORD_OTHER = "other";
    public static final double NO_UNIQUE_VALUE = -0.00123456777;
    @Deprecated
    public static final DecimalQuantity NO_UNIQUE_VALUE_DECIMAL_QUANTITY = new DecimalQuantity_DualStorageBCD(-0.00123456777);
    private static final Constraint NO_CONSTRAINT = new Constraint(){
        private static final long serialVersionUID = 9163464945387899416L;

        @Override
        public boolean isFulfilled(IFixedDecimal n) {
            return true;
        }

        @Override
        public boolean isLimited(SampleType sampleType) {
            return false;
        }

        public String toString() {
            return "";
        }
    };
    private static final Rule DEFAULT_RULE = new Rule("other", NO_CONSTRAINT, null, null);
    public static final PluralRules DEFAULT = new PluralRules(new RuleList().addRule(DEFAULT_RULE), StandardPluralRanges.DEFAULT);
    static final Pattern AT_SEPARATED = Pattern.compile("\\s*\\Q\\E@\\s*");
    static final Pattern OR_SEPARATED = Pattern.compile("\\s*or\\s*");
    static final Pattern AND_SEPARATED = Pattern.compile("\\s*and\\s*");
    static final Pattern COMMA_SEPARATED = Pattern.compile("\\s*,\\s*");
    static final Pattern DOTDOT_SEPARATED = Pattern.compile("\\s*\\Q..\\E\\s*");
    static final Pattern TILDE_SEPARATED = Pattern.compile("\\s*~\\s*");
    static final Pattern SEMI_SEPARATED = Pattern.compile("\\s*;\\s*");

    public static PluralRules parseDescription(String description) throws ParseException {
        return PluralRules.newInternal(description, null);
    }

    public static PluralRules createRules(String description) {
        try {
            return PluralRules.parseDescription(description);
        }
        catch (Exception e) {
            return null;
        }
    }

    @Deprecated
    public static PluralRules newInternal(String description, StandardPluralRanges ranges) throws ParseException {
        return (description = description.trim()).length() == 0 ? DEFAULT : new PluralRules(PluralRules.parseRuleChain(description), ranges);
    }

    private static Constraint parseConstraint(String description) throws ParseException {
        Constraint result = null;
        String[] or_together = OR_SEPARATED.split(description, 0);
        for (int i = 0; i < or_together.length; ++i) {
            Constraint andConstraint = null;
            String[] and_together = AND_SEPARATED.split(or_together[i], 0);
            for (int j = 0; j < and_together.length; ++j) {
                Operand operand;
                Constraint newConstraint = NO_CONSTRAINT;
                String condition = and_together[j].trim();
                String[] tokens = SimpleTokenizer.split(condition);
                int mod = 0;
                boolean inRange = true;
                boolean integersOnly = true;
                double lowBound = 9.223372036854776E18;
                double highBound = -9.223372036854776E18;
                long[] vals = null;
                int x = 0;
                String t = tokens[x++];
                boolean hackForCompatibility = false;
                try {
                    operand = FixedDecimal.getOperand(t);
                }
                catch (Exception e) {
                    throw PluralRules.unexpected(t, condition);
                }
                if (x < tokens.length) {
                    if ("mod".equals(t = tokens[x++]) || "%".equals(t)) {
                        mod = Integer.parseInt(tokens[x++]);
                        t = PluralRules.nextToken(tokens, x++, condition);
                    }
                    if ("not".equals(t)) {
                        inRange = !inRange;
                        if ("=".equals(t = PluralRules.nextToken(tokens, x++, condition))) {
                            throw PluralRules.unexpected(t, condition);
                        }
                    } else if ("!".equals(t)) {
                        inRange = !inRange;
                        if (!"=".equals(t = PluralRules.nextToken(tokens, x++, condition))) {
                            throw PluralRules.unexpected(t, condition);
                        }
                    }
                    if ("is".equals(t) || "in".equals(t) || "=".equals(t)) {
                        hackForCompatibility = "is".equals(t);
                        if (hackForCompatibility && !inRange) {
                            throw PluralRules.unexpected(t, condition);
                        }
                        t = PluralRules.nextToken(tokens, x++, condition);
                    } else if ("within".equals(t)) {
                        integersOnly = false;
                        t = PluralRules.nextToken(tokens, x++, condition);
                    } else {
                        throw PluralRules.unexpected(t, condition);
                    }
                    if ("not".equals(t)) {
                        if (!hackForCompatibility && !inRange) {
                            throw PluralRules.unexpected(t, condition);
                        }
                        inRange = !inRange;
                        t = PluralRules.nextToken(tokens, x++, condition);
                    }
                    ArrayList<Long> valueList = new ArrayList<Long>();
                    while (true) {
                        long low;
                        long high = low = Long.parseLong(t);
                        if (x < tokens.length) {
                            if ((t = PluralRules.nextToken(tokens, x++, condition)).equals(".")) {
                                if (!(t = PluralRules.nextToken(tokens, x++, condition)).equals(".")) {
                                    throw PluralRules.unexpected(t, condition);
                                }
                                t = PluralRules.nextToken(tokens, x++, condition);
                                high = Long.parseLong(t);
                                if (x < tokens.length && !(t = PluralRules.nextToken(tokens, x++, condition)).equals(",")) {
                                    throw PluralRules.unexpected(t, condition);
                                }
                            } else if (!t.equals(",")) {
                                throw PluralRules.unexpected(t, condition);
                            }
                        }
                        if (low > high) {
                            throw PluralRules.unexpected(low + "~" + high, condition);
                        }
                        if (mod != 0 && high >= (long)mod) {
                            throw PluralRules.unexpected(high + ">mod=" + mod, condition);
                        }
                        valueList.add(low);
                        valueList.add(high);
                        lowBound = Math.min(lowBound, (double)low);
                        highBound = Math.max(highBound, (double)high);
                        if (x >= tokens.length) break;
                        t = PluralRules.nextToken(tokens, x++, condition);
                    }
                    if (t.equals(",")) {
                        throw PluralRules.unexpected(t, condition);
                    }
                    if (valueList.size() == 2) {
                        vals = null;
                    } else {
                        vals = new long[valueList.size()];
                        for (int k = 0; k < vals.length; ++k) {
                            vals[k] = (Long)valueList.get(k);
                        }
                    }
                    if (lowBound != highBound && hackForCompatibility && !inRange) {
                        throw PluralRules.unexpected("is not <range>", condition);
                    }
                    newConstraint = new RangeConstraint(mod, inRange, operand, integersOnly, lowBound, highBound, vals);
                }
                andConstraint = andConstraint == null ? newConstraint : new AndConstraint(andConstraint, newConstraint);
            }
            result = result == null ? andConstraint : new OrConstraint(result, andConstraint);
        }
        return result;
    }

    private static ParseException unexpected(String token, String context) {
        return new ParseException("unexpected token '" + token + "' in '" + context + "'", -1);
    }

    private static String nextToken(String[] tokens, int x, String context) throws ParseException {
        if (x < tokens.length) {
            return tokens[x];
        }
        throw new ParseException("missing token at end of '" + context + "'", -1);
    }

    private static Rule parseRule(String description) throws ParseException {
        if (description.length() == 0) {
            return DEFAULT_RULE;
        }
        int x = (description = description.toLowerCase(Locale.ENGLISH)).indexOf(58);
        if (x == -1) {
            throw new ParseException("missing ':' in rule description '" + description + "'", 0);
        }
        String keyword = description.substring(0, x).trim();
        if (!PluralRules.isValidKeyword(keyword)) {
            throw new ParseException("keyword '" + keyword + " is not valid", 0);
        }
        description = description.substring(x + 1).trim();
        String[] constraintOrSamples = AT_SEPARATED.split(description, 0);
        boolean sampleFailure = false;
        DecimalQuantitySamples integerSamples = null;
        DecimalQuantitySamples decimalSamples = null;
        switch (constraintOrSamples.length) {
            case 1: {
                break;
            }
            case 2: {
                integerSamples = DecimalQuantitySamples.parse(constraintOrSamples[1]);
                if (integerSamples.sampleType != SampleType.DECIMAL) break;
                decimalSamples = integerSamples;
                integerSamples = null;
                break;
            }
            case 3: {
                integerSamples = DecimalQuantitySamples.parse(constraintOrSamples[1]);
                decimalSamples = DecimalQuantitySamples.parse(constraintOrSamples[2]);
                if (integerSamples.sampleType == SampleType.INTEGER && decimalSamples.sampleType == SampleType.DECIMAL) break;
                throw new IllegalArgumentException("Must have @integer then @decimal in " + description);
            }
            default: {
                throw new IllegalArgumentException("Too many samples in " + description);
            }
        }
        if (sampleFailure) {
            throw new IllegalArgumentException("Ill-formed samples\u2014'@' characters.");
        }
        boolean isOther = keyword.equals(KEYWORD_OTHER);
        if (isOther != (constraintOrSamples[0].length() == 0)) {
            throw new IllegalArgumentException("The keyword 'other' must have no constraints, just samples.");
        }
        Constraint constraint = isOther ? NO_CONSTRAINT : PluralRules.parseConstraint(constraintOrSamples[0]);
        return new Rule(keyword, constraint, integerSamples, decimalSamples);
    }

    private static RuleList parseRuleChain(String description) throws ParseException {
        RuleList result = new RuleList();
        if (description.endsWith(";")) {
            description = description.substring(0, description.length() - 1);
        }
        String[] rules = SEMI_SEPARATED.split(description, 0);
        for (int i = 0; i < rules.length; ++i) {
            Rule rule = PluralRules.parseRule(rules[i].trim());
            RuleList ruleList = result;
            ruleList.hasExplicitBoundingInfo = ruleList.hasExplicitBoundingInfo | (rule.integerSamples != null || rule.decimalSamples != null);
            result.addRule(rule);
        }
        return result.finish();
    }

    private static void addRange(StringBuilder result, double lb, double ub, boolean addSeparator) {
        if (addSeparator) {
            result.append(",");
        }
        if (lb == ub) {
            result.append(PluralRules.format(lb));
        } else {
            result.append(PluralRules.format(lb) + ".." + PluralRules.format(ub));
        }
    }

    private static String format(double lb) {
        long lbi = (long)lb;
        return lb == (double)lbi ? String.valueOf(lbi) : String.valueOf(lb);
    }

    private boolean addConditional(Set<IFixedDecimal> toAddTo, Set<IFixedDecimal> others, double trial) {
        boolean added;
        FixedDecimal toAdd = new FixedDecimal(trial);
        if (!toAddTo.contains(toAdd) && !others.contains(toAdd)) {
            others.add(toAdd);
            added = true;
        } else {
            added = false;
        }
        return added;
    }

    public static PluralRules forLocale(ULocale locale) {
        return Factory.getDefaultFactory().forLocale(locale, PluralType.CARDINAL);
    }

    public static PluralRules forLocale(Locale locale) {
        return PluralRules.forLocale(ULocale.forLocale(locale));
    }

    public static PluralRules forLocale(ULocale locale, PluralType type) {
        return Factory.getDefaultFactory().forLocale(locale, type);
    }

    public static PluralRules forLocale(Locale locale, PluralType type) {
        return PluralRules.forLocale(ULocale.forLocale(locale), type);
    }

    private static boolean isValidKeyword(String token) {
        return ALLOWED_ID.containsAll(token);
    }

    private PluralRules(RuleList rules, StandardPluralRanges standardPluralRanges) {
        this.rules = rules;
        this.keywords = Collections.unmodifiableSet(rules.getKeywords());
        this.standardPluralRanges = standardPluralRanges;
    }

    public int hashCode() {
        return this.rules.hashCode();
    }

    public String select(double number) {
        return this.rules.select(new FixedDecimal(number));
    }

    public String select(FormattedNumber number) {
        return this.rules.select(number.getFixedDecimal());
    }

    public String select(FormattedNumberRange range) {
        if (this.standardPluralRanges == null) {
            throw new UnsupportedOperationException("Plural ranges are unavailable on this instance");
        }
        StandardPlural form1 = StandardPlural.fromString(this.select(range.getFirstFixedDecimal()));
        StandardPlural form2 = StandardPlural.fromString(this.select(range.getSecondFixedDecimal()));
        StandardPlural result = this.standardPluralRanges.resolve(form1, form2);
        return result.getKeyword();
    }

    @Deprecated
    public String select(double number, int countVisibleFractionDigits, long fractionaldigits) {
        return this.rules.select(new FixedDecimal(number, countVisibleFractionDigits, fractionaldigits));
    }

    @Deprecated
    public String select(IFixedDecimal number) {
        return this.rules.select(number);
    }

    @Deprecated
    public boolean matches(FixedDecimal sample, String keyword) {
        return this.rules.select(sample, keyword);
    }

    public Set<String> getKeywords() {
        return this.keywords;
    }

    public double getUniqueKeywordValue(String keyword) {
        DecimalQuantity uniqValDq = this.getUniqueKeywordDecimalQuantityValue(keyword);
        if (uniqValDq.equals(NO_UNIQUE_VALUE_DECIMAL_QUANTITY)) {
            return -0.00123456777;
        }
        return uniqValDq.toDouble();
    }

    @Deprecated
    public DecimalQuantity getUniqueKeywordDecimalQuantityValue(String keyword) {
        Collection<DecimalQuantity> values = this.getAllKeywordDecimalQuantityValues(keyword);
        if (values != null && values.size() == 1) {
            return values.iterator().next();
        }
        return NO_UNIQUE_VALUE_DECIMAL_QUANTITY;
    }

    public Collection<Double> getAllKeywordValues(String keyword) {
        Collection<DecimalQuantity> samples = this.getAllKeywordDecimalQuantityValues(keyword);
        if (samples == null) {
            return null;
        }
        LinkedHashSet<Double> result = new LinkedHashSet<Double>();
        for (DecimalQuantity dq : samples) {
            result.add(dq.toDouble());
        }
        return result;
    }

    @Deprecated
    public Collection<DecimalQuantity> getAllKeywordDecimalQuantityValues(String keyword) {
        return this.getAllKeywordValues(keyword, SampleType.INTEGER);
    }

    @Deprecated
    public Collection<DecimalQuantity> getAllKeywordValues(String keyword, SampleType type) {
        if (!this.isLimited(keyword, type)) {
            return null;
        }
        return this.getDecimalQuantitySamples(keyword, type);
    }

    public Collection<Double> getSamples(String keyword) {
        return this.getSamples(keyword, SampleType.INTEGER);
    }

    @Deprecated
    public Collection<DecimalQuantity> getDecimalQuantitySamples(String keyword) {
        return this.getDecimalQuantitySamples(keyword, SampleType.INTEGER);
    }

    @Deprecated
    public Collection<Double> getSamples(String keyword, SampleType sampleType) {
        Collection<DecimalQuantity> samples = this.getDecimalQuantitySamples(keyword, sampleType);
        if (samples == null) {
            return null;
        }
        LinkedHashSet<Double> result = new LinkedHashSet<Double>();
        for (DecimalQuantity dq : samples) {
            result.add(dq.toDouble());
        }
        return result;
    }

    @Deprecated
    public Collection<DecimalQuantity> getDecimalQuantitySamples(String keyword, SampleType sampleType) {
        if (!this.keywords.contains(keyword)) {
            return null;
        }
        LinkedHashSet<DecimalQuantity> result = new LinkedHashSet<DecimalQuantity>();
        if (this.rules.hasExplicitBoundingInfo) {
            DecimalQuantitySamples samples = this.rules.getDecimalSamples(keyword, sampleType);
            return samples == null ? Collections.unmodifiableSet(result) : Collections.unmodifiableCollection(samples.addDecimalQuantitySamples(result));
        }
        int maxCount = this.isLimited(keyword, sampleType) ? Integer.MAX_VALUE : 20;
        switch (sampleType) {
            case INTEGER: {
                for (int i = 0; i < 200 && this.addSample(keyword, new DecimalQuantity_DualStorageBCD(i), maxCount, result); ++i) {
                }
                this.addSample(keyword, new DecimalQuantity_DualStorageBCD(1000000), maxCount, result);
                break;
            }
            case DECIMAL: {
                for (int i = 0; i < 2000; ++i) {
                    DecimalQuantity_DualStorageBCD nextSample = new DecimalQuantity_DualStorageBCD(i);
                    nextSample.adjustMagnitude(-1);
                    if (!this.addSample(keyword, nextSample, maxCount, result)) break;
                }
                this.addSample(keyword, DecimalQuantity_DualStorageBCD.fromExponentString("1000000.0"), maxCount, result);
            }
        }
        return result.size() == 0 ? null : Collections.unmodifiableSet(result);
    }

    private boolean addSample(String keyword, DecimalQuantity sample, int maxCount, Set<DecimalQuantity> result) {
        String selectedKeyword = this.select(sample);
        if (selectedKeyword.equals(keyword)) {
            result.add(sample);
            if (--maxCount < 0) {
                return false;
            }
        }
        return true;
    }

    @Deprecated
    public DecimalQuantitySamples getDecimalSamples(String keyword, SampleType sampleType) {
        return this.rules.getDecimalSamples(keyword, sampleType);
    }

    public static ULocale[] getAvailableULocales() {
        return Factory.getDefaultFactory().getAvailableULocales();
    }

    public static ULocale getFunctionalEquivalent(ULocale locale, boolean[] isAvailable) {
        return Factory.getDefaultFactory().getFunctionalEquivalent(locale, isAvailable);
    }

    public String toString() {
        return this.rules.toString();
    }

    public boolean equals(Object rhs) {
        return rhs instanceof PluralRules && this.equals((PluralRules)rhs);
    }

    public boolean equals(PluralRules rhs) {
        return rhs != null && this.toString().equals(rhs.toString());
    }

    public KeywordStatus getKeywordStatus(String keyword, int offset, Set<DecimalQuantity> explicits, Output<DecimalQuantity> uniqueValue) {
        return this.getKeywordStatus(keyword, offset, explicits, uniqueValue, SampleType.INTEGER);
    }

    @Deprecated
    public KeywordStatus getKeywordStatus(String keyword, int offset, Set<DecimalQuantity> explicits, Output<DecimalQuantity> uniqueValue, SampleType sampleType) {
        if (uniqueValue != null) {
            uniqueValue.value = null;
        }
        if (!this.keywords.contains(keyword)) {
            return KeywordStatus.INVALID;
        }
        if (!this.isLimited(keyword, sampleType)) {
            return KeywordStatus.UNBOUNDED;
        }
        Collection<DecimalQuantity> values = this.getDecimalQuantitySamples(keyword, sampleType);
        int originalSize = values.size();
        if (explicits == null) {
            explicits = Collections.emptySet();
        }
        if (originalSize > explicits.size()) {
            if (originalSize == 1) {
                if (uniqueValue != null) {
                    uniqueValue.value = values.iterator().next();
                }
                return KeywordStatus.UNIQUE;
            }
            return KeywordStatus.BOUNDED;
        }
        ArrayList<DecimalQuantity> subtractedSet = new ArrayList<DecimalQuantity>(values);
        for (DecimalQuantity explicit : explicits) {
            BigDecimal explicitBd = explicit.toBigDecimal();
            BigDecimal valToRemoveBd = explicitBd.subtract(new BigDecimal(offset));
            DecimalQuantity_DualStorageBCD valToRemove = new DecimalQuantity_DualStorageBCD(valToRemoveBd);
            subtractedSet.remove(valToRemove);
        }
        if (subtractedSet.size() == 0) {
            return KeywordStatus.SUPPRESSED;
        }
        if (uniqueValue != null && subtractedSet.size() == 1) {
            uniqueValue.value = subtractedSet.iterator().next();
        }
        return originalSize == 1 ? KeywordStatus.UNIQUE : KeywordStatus.BOUNDED;
    }

    @Deprecated
    public String getRules(String keyword) {
        return this.rules.getRules(keyword);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        throw new NotSerializableException();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new NotSerializableException();
    }

    private Object writeReplace() throws ObjectStreamException {
        return new PluralRulesSerialProxy(this.toString());
    }

    @Deprecated
    public int compareTo(PluralRules other) {
        return this.toString().compareTo(other.toString());
    }

    Boolean isLimited(String keyword) {
        return this.rules.isLimited(keyword, SampleType.INTEGER);
    }

    @Deprecated
    public boolean isLimited(String keyword, SampleType sampleType) {
        return this.rules.isLimited(keyword, sampleType);
    }

    @Deprecated
    public boolean computeLimited(String keyword, SampleType sampleType) {
        return this.rules.computeLimited(keyword, sampleType);
    }

    public static enum KeywordStatus {
        INVALID,
        SUPPRESSED,
        UNIQUE,
        BOUNDED,
        UNBOUNDED;

    }

    private static class RuleList
    implements Serializable {
        private boolean hasExplicitBoundingInfo = false;
        private static final long serialVersionUID = 1L;
        private final List<Rule> rules = new ArrayList<Rule>();

        private RuleList() {
        }

        public RuleList addRule(Rule nextRule) {
            String keyword = nextRule.getKeyword();
            for (Rule rule : this.rules) {
                if (!keyword.equals(rule.getKeyword())) continue;
                throw new IllegalArgumentException("Duplicate keyword: " + keyword);
            }
            this.rules.add(nextRule);
            return this;
        }

        public RuleList finish() throws ParseException {
            Rule otherRule = null;
            Iterator<Rule> it = this.rules.iterator();
            while (it.hasNext()) {
                Rule rule = it.next();
                if (!PluralRules.KEYWORD_OTHER.equals(rule.getKeyword())) continue;
                otherRule = rule;
                it.remove();
            }
            if (otherRule == null) {
                otherRule = PluralRules.parseRule("other:");
            }
            this.rules.add(otherRule);
            return this;
        }

        private Rule selectRule(IFixedDecimal n) {
            for (Rule rule : this.rules) {
                if (!rule.appliesTo(n)) continue;
                return rule;
            }
            return null;
        }

        public String select(IFixedDecimal n) {
            if (n.isInfinite() || n.isNaN()) {
                return PluralRules.KEYWORD_OTHER;
            }
            Rule r = this.selectRule(n);
            return r.getKeyword();
        }

        public Set<String> getKeywords() {
            LinkedHashSet<String> result = new LinkedHashSet<String>();
            for (Rule rule : this.rules) {
                result.add(rule.getKeyword());
            }
            return result;
        }

        public boolean isLimited(String keyword, SampleType sampleType) {
            if (this.hasExplicitBoundingInfo) {
                DecimalQuantitySamples mySamples = this.getDecimalSamples(keyword, sampleType);
                return mySamples == null ? true : mySamples.bounded;
            }
            return this.computeLimited(keyword, sampleType);
        }

        public boolean computeLimited(String keyword, SampleType sampleType) {
            boolean result = false;
            for (Rule rule : this.rules) {
                if (!keyword.equals(rule.getKeyword())) continue;
                if (!rule.isLimited(sampleType)) {
                    return false;
                }
                result = true;
            }
            return result;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (Rule rule : this.rules) {
                if (builder.length() != 0) {
                    builder.append(PluralRules.CATEGORY_SEPARATOR);
                }
                builder.append(rule);
            }
            return builder.toString();
        }

        public String getRules(String keyword) {
            for (Rule rule : this.rules) {
                if (!rule.getKeyword().equals(keyword)) continue;
                return rule.getConstraint();
            }
            return null;
        }

        public boolean select(IFixedDecimal sample, String keyword) {
            for (Rule rule : this.rules) {
                if (!rule.getKeyword().equals(keyword) || !rule.appliesTo(sample)) continue;
                return true;
            }
            return false;
        }

        public DecimalQuantitySamples getDecimalSamples(String keyword, SampleType sampleType) {
            for (Rule rule : this.rules) {
                if (!rule.getKeyword().equals(keyword)) continue;
                return sampleType == SampleType.INTEGER ? rule.integerSamples : rule.decimalSamples;
            }
            return null;
        }
    }

    private static class Rule
    implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String keyword;
        private final Constraint constraint;
        private final DecimalQuantitySamples integerSamples;
        private final DecimalQuantitySamples decimalSamples;

        public Rule(String keyword, Constraint constraint, DecimalQuantitySamples integerSamples, DecimalQuantitySamples decimalSamples) {
            this.keyword = keyword;
            this.constraint = constraint;
            this.integerSamples = integerSamples;
            this.decimalSamples = decimalSamples;
        }

        public Rule and(Constraint c) {
            return new Rule(this.keyword, new AndConstraint(this.constraint, c), this.integerSamples, this.decimalSamples);
        }

        public Rule or(Constraint c) {
            return new Rule(this.keyword, new OrConstraint(this.constraint, c), this.integerSamples, this.decimalSamples);
        }

        public String getKeyword() {
            return this.keyword;
        }

        public boolean appliesTo(IFixedDecimal n) {
            return this.constraint.isFulfilled(n);
        }

        public boolean isLimited(SampleType sampleType) {
            return this.constraint.isLimited(sampleType);
        }

        public String toString() {
            return this.keyword + ": " + this.constraint.toString() + (this.integerSamples == null ? "" : " " + this.integerSamples.toString()) + (this.decimalSamples == null ? "" : " " + this.decimalSamples.toString());
        }

        public int hashCode() {
            return this.keyword.hashCode() ^ this.constraint.hashCode();
        }

        public String getConstraint() {
            return this.constraint.toString();
        }
    }

    private static class OrConstraint
    extends BinaryConstraint {
        private static final long serialVersionUID = 1405488568664762222L;

        OrConstraint(Constraint a, Constraint b) {
            super(a, b);
        }

        @Override
        public boolean isFulfilled(IFixedDecimal n) {
            return this.a.isFulfilled(n) || this.b.isFulfilled(n);
        }

        @Override
        public boolean isLimited(SampleType sampleType) {
            return this.a.isLimited(sampleType) && this.b.isLimited(sampleType);
        }

        public String toString() {
            return this.a.toString() + " or " + this.b.toString();
        }
    }

    private static class AndConstraint
    extends BinaryConstraint {
        private static final long serialVersionUID = 7766999779862263523L;

        AndConstraint(Constraint a, Constraint b) {
            super(a, b);
        }

        @Override
        public boolean isFulfilled(IFixedDecimal n) {
            return this.a.isFulfilled(n) && this.b.isFulfilled(n);
        }

        @Override
        public boolean isLimited(SampleType sampleType) {
            return this.a.isLimited(sampleType) || this.b.isLimited(sampleType);
        }

        public String toString() {
            return this.a.toString() + " and " + this.b.toString();
        }
    }

    private static abstract class BinaryConstraint
    implements Constraint,
    Serializable {
        private static final long serialVersionUID = 1L;
        protected final Constraint a;
        protected final Constraint b;

        protected BinaryConstraint(Constraint a, Constraint b) {
            this.a = a;
            this.b = b;
        }
    }

    private static class RangeConstraint
    implements Constraint,
    Serializable {
        private static final long serialVersionUID = 1L;
        private final int mod;
        private final boolean inRange;
        private final boolean integersOnly;
        private final double lowerBound;
        private final double upperBound;
        private final long[] range_list;
        private final Operand operand;

        RangeConstraint(int mod, boolean inRange, Operand operand, boolean integersOnly, double lowBound, double highBound, long[] vals) {
            this.mod = mod;
            this.inRange = inRange;
            this.integersOnly = integersOnly;
            this.lowerBound = lowBound;
            this.upperBound = highBound;
            this.range_list = vals;
            this.operand = operand;
        }

        @Override
        public boolean isFulfilled(IFixedDecimal number) {
            boolean test;
            double n = number.getPluralOperand(this.operand);
            if (this.integersOnly && n - (double)((long)n) != 0.0 || this.operand == Operand.j && number.getPluralOperand(Operand.v) != 0.0) {
                return !this.inRange;
            }
            if (this.mod != 0) {
                n %= (double)this.mod;
            }
            boolean bl = test = n >= this.lowerBound && n <= this.upperBound;
            if (test && this.range_list != null) {
                test = false;
                for (int i = 0; !test && i < this.range_list.length; i += 2) {
                    test = n >= (double)this.range_list[i] && n <= (double)this.range_list[i + 1];
                }
            }
            return this.inRange == test;
        }

        @Override
        public boolean isLimited(SampleType sampleType) {
            boolean valueIsZero = this.lowerBound == this.upperBound && this.lowerBound == 0.0;
            boolean hasDecimals = (this.operand == Operand.v || this.operand == Operand.w || this.operand == Operand.f || this.operand == Operand.t) && this.inRange != valueIsZero;
            switch (sampleType) {
                case INTEGER: {
                    return hasDecimals || (this.operand == Operand.n || this.operand == Operand.i || this.operand == Operand.j) && this.mod == 0 && this.inRange;
                }
                case DECIMAL: {
                    return !(hasDecimals && this.operand != Operand.n && this.operand != Operand.j || !this.integersOnly && this.lowerBound != this.upperBound || this.mod != 0 || !this.inRange);
                }
            }
            return false;
        }

        public String toString() {
            boolean isList;
            StringBuilder result = new StringBuilder();
            result.append((Object)this.operand);
            if (this.mod != 0) {
                result.append(" % ").append(this.mod);
            }
            boolean bl = isList = this.lowerBound != this.upperBound;
            result.append(!isList ? (this.inRange ? " = " : " != ") : (this.integersOnly ? (this.inRange ? " = " : " != ") : (this.inRange ? " within " : " not within ")));
            if (this.range_list != null) {
                for (int i = 0; i < this.range_list.length; i += 2) {
                    PluralRules.addRange(result, this.range_list[i], this.range_list[i + 1], i != 0);
                }
            } else {
                PluralRules.addRange(result, this.lowerBound, this.upperBound, false);
            }
            return result.toString();
        }
    }

    static class SimpleTokenizer {
        static final UnicodeSet BREAK_AND_IGNORE = new UnicodeSet(9, 10, 12, 13, 32, 32).freeze();
        static final UnicodeSet BREAK_AND_KEEP = new UnicodeSet(33, 33, 37, 37, 44, 44, 46, 46, 61, 61).freeze();

        SimpleTokenizer() {
        }

        static String[] split(String source) {
            int last = -1;
            ArrayList<String> result = new ArrayList<String>();
            for (int i = 0; i < source.length(); ++i) {
                char ch = source.charAt(i);
                if (BREAK_AND_IGNORE.contains(ch)) {
                    if (last < 0) continue;
                    result.add(source.substring(last, i));
                    last = -1;
                    continue;
                }
                if (BREAK_AND_KEEP.contains(ch)) {
                    if (last >= 0) {
                        result.add(source.substring(last, i));
                    }
                    result.add(source.substring(i, i + 1));
                    last = -1;
                    continue;
                }
                if (last >= 0) continue;
                last = i;
            }
            if (last >= 0) {
                result.add(source.substring(last));
            }
            return result.toArray(new String[result.size()]);
        }
    }

    private static interface Constraint
    extends Serializable {
        public boolean isFulfilled(IFixedDecimal var1);

        public boolean isLimited(SampleType var1);
    }

    @Deprecated
    public static class DecimalQuantitySamples {
        @Deprecated
        public final SampleType sampleType;
        @Deprecated
        public final Set<DecimalQuantitySamplesRange> samples;
        @Deprecated
        public final boolean bounded;

        private DecimalQuantitySamples(SampleType sampleType, Set<DecimalQuantitySamplesRange> samples, boolean bounded) {
            this.sampleType = sampleType;
            this.samples = samples;
            this.bounded = bounded;
        }

        static DecimalQuantitySamples parse(String source) {
            SampleType sampleType2;
            boolean bounded2 = true;
            boolean haveBound = false;
            LinkedHashSet<DecimalQuantitySamplesRange> samples2 = new LinkedHashSet<DecimalQuantitySamplesRange>();
            if (source.startsWith("integer")) {
                sampleType2 = SampleType.INTEGER;
            } else if (source.startsWith("decimal")) {
                sampleType2 = SampleType.DECIMAL;
            } else {
                throw new IllegalArgumentException("Samples must start with 'integer' or 'decimal'");
            }
            source = source.substring(7).trim();
            block4: for (String range : COMMA_SEPARATED.split(source, 0)) {
                if (range.equals("\u2026") || range.equals("...")) {
                    bounded2 = false;
                    haveBound = true;
                    continue;
                }
                if (haveBound) {
                    throw new IllegalArgumentException("Can only have \u2026 at the end of samples: " + range);
                }
                String[] rangeParts = TILDE_SEPARATED.split(range, 0);
                switch (rangeParts.length) {
                    case 1: {
                        DecimalQuantity sample = DecimalQuantity_DualStorageBCD.fromExponentString(rangeParts[0]);
                        DecimalQuantitySamples.checkDecimal(sampleType2, sample);
                        samples2.add(new DecimalQuantitySamplesRange(sample, sample));
                        continue block4;
                    }
                    case 2: {
                        DecimalQuantity start = DecimalQuantity_DualStorageBCD.fromExponentString(rangeParts[0]);
                        DecimalQuantity end = DecimalQuantity_DualStorageBCD.fromExponentString(rangeParts[1]);
                        DecimalQuantitySamples.checkDecimal(sampleType2, start);
                        DecimalQuantitySamples.checkDecimal(sampleType2, end);
                        samples2.add(new DecimalQuantitySamplesRange(start, end));
                        continue block4;
                    }
                    default: {
                        throw new IllegalArgumentException("Ill-formed number range: " + range);
                    }
                }
            }
            return new DecimalQuantitySamples(sampleType2, Collections.unmodifiableSet(samples2), bounded2);
        }

        private static void checkDecimal(SampleType sampleType2, DecimalQuantity sample) {
            if (sampleType2 == SampleType.INTEGER && sample.getPluralOperand(Operand.v) != 0.0 || sampleType2 == SampleType.DECIMAL && sample.getPluralOperand(Operand.v) == 0.0 && sample.getPluralOperand(Operand.e) == 0.0) {
                throw new IllegalArgumentException("Ill-formed number range: " + sample);
            }
        }

        @Deprecated
        public Collection<Double> addSamples(Collection<Double> result) {
            this.addSamples(result, null);
            return result;
        }

        @Deprecated
        public Collection<DecimalQuantity> addDecimalQuantitySamples(Collection<DecimalQuantity> result) {
            this.addSamples(null, result);
            return result;
        }

        @Deprecated
        public void addSamples(Collection<Double> doubleResult, Collection<DecimalQuantity> dqResult) {
            if (doubleResult == null && dqResult == null || doubleResult != null && dqResult != null) {
                return;
            }
            boolean isDouble = doubleResult != null;
            for (DecimalQuantitySamplesRange range : this.samples) {
                DecimalQuantity start = range.start;
                DecimalQuantity end = range.end;
                int lowerDispMag = start.getLowerDisplayMagnitude();
                int exponent = start.getExponent();
                int incrementScale = lowerDispMag + exponent;
                BigDecimal incrementBd = BigDecimal.ONE.movePointRight(incrementScale);
                DecimalQuantity dq = start.createCopy();
                while (dq.toDouble() <= end.toDouble()) {
                    if (isDouble) {
                        double dblValue = dq.toDouble();
                        if (dblValue != Math.floor(dblValue) && dq.getPluralOperand(Operand.v) > 0.0) {
                            doubleResult.add(dblValue);
                        }
                    } else {
                        dqResult.add(dq);
                    }
                    BigDecimal dqBd = dq.toBigDecimal();
                    BigDecimal newDqBd = dqBd.add(incrementBd);
                    dq = new DecimalQuantity_DualStorageBCD(newDqBd);
                    dq.setMinFraction(-lowerDispMag);
                    dq.adjustMagnitude(-exponent);
                    dq.adjustExponent(exponent);
                }
            }
        }

        @Deprecated
        public String toString() {
            StringBuilder b = new StringBuilder("@").append(this.sampleType.toString().toLowerCase(Locale.ENGLISH));
            boolean first = true;
            for (DecimalQuantitySamplesRange item : this.samples) {
                if (first) {
                    first = false;
                } else {
                    b.append(",");
                }
                b.append(' ').append(item);
            }
            if (!this.bounded) {
                b.append(", \u2026");
            }
            return b.toString();
        }

        @Deprecated
        public Set<DecimalQuantitySamplesRange> getSamples() {
            return this.samples;
        }

        @Deprecated
        public void getStartEndSamples(Set<DecimalQuantity> target) {
            for (DecimalQuantitySamplesRange range : this.samples) {
                target.add(range.start);
                target.add(range.end);
            }
        }
    }

    @Deprecated
    public static class DecimalQuantitySamplesRange {
        @Deprecated
        public final DecimalQuantity start;
        @Deprecated
        public final DecimalQuantity end;

        @Deprecated
        public DecimalQuantitySamplesRange(DecimalQuantity start, DecimalQuantity end) {
            if (start.getPluralOperand(Operand.v) != end.getPluralOperand(Operand.v)) {
                throw new IllegalArgumentException("Ranges must have the same number of visible decimals: " + start + "~" + end);
            }
            this.start = start;
            this.end = end;
        }

        @Deprecated
        public String toString() {
            return this.start.toExponentString() + (this.end == this.start ? "" : "~" + this.end.toExponentString());
        }
    }

    @Deprecated
    public static enum SampleType {
        INTEGER,
        DECIMAL;

    }

    @Deprecated
    public static class FixedDecimal
    extends Number
    implements Comparable<FixedDecimal>,
    IFixedDecimal {
        private static final long serialVersionUID = -4756200506571685661L;
        final double source;
        final int visibleDecimalDigitCount;
        final int visibleDecimalDigitCountWithoutTrailingZeros;
        final long decimalDigits;
        final long decimalDigitsWithoutTrailingZeros;
        final long integerValue;
        final boolean hasIntegerValue;
        final boolean isNegative;
        final int exponent;
        private final int baseFactor;
        static final long MAX = 1000000000000000000L;
        private static final long MAX_INTEGER_PART = 1000000000L;

        @Deprecated
        public double getSource() {
            return this.source;
        }

        @Deprecated
        public int getVisibleDecimalDigitCount() {
            return this.visibleDecimalDigitCount;
        }

        @Deprecated
        public int getVisibleDecimalDigitCountWithoutTrailingZeros() {
            return this.visibleDecimalDigitCountWithoutTrailingZeros;
        }

        @Deprecated
        public long getDecimalDigits() {
            return this.decimalDigits;
        }

        @Deprecated
        public long getDecimalDigitsWithoutTrailingZeros() {
            return this.decimalDigitsWithoutTrailingZeros;
        }

        @Deprecated
        public long getIntegerValue() {
            return this.integerValue;
        }

        @Override
        @Deprecated
        public boolean isHasIntegerValue() {
            return this.hasIntegerValue;
        }

        @Deprecated
        public boolean isNegative() {
            return this.isNegative;
        }

        @Deprecated
        public int getBaseFactor() {
            return this.baseFactor;
        }

        @Deprecated
        public FixedDecimal(double n, int v, long f, int e, int c) {
            this.isNegative = n < 0.0;
            this.source = this.isNegative ? -n : n;
            this.visibleDecimalDigitCount = v;
            this.decimalDigits = f;
            this.integerValue = n > 1.0E18 ? 1000000000000000000L : (long)this.source;
            int initExpVal = e;
            if (initExpVal == 0) {
                initExpVal = c;
            }
            this.exponent = initExpVal;
            boolean bl = this.hasIntegerValue = this.source == (double)this.integerValue;
            if (f == 0L) {
                this.decimalDigitsWithoutTrailingZeros = 0L;
                this.visibleDecimalDigitCountWithoutTrailingZeros = 0;
            } else {
                long fdwtz = f;
                int trimmedCount = v;
                while (fdwtz % 10L == 0L) {
                    fdwtz /= 10L;
                    --trimmedCount;
                }
                this.decimalDigitsWithoutTrailingZeros = fdwtz;
                this.visibleDecimalDigitCountWithoutTrailingZeros = trimmedCount;
            }
            this.baseFactor = (int)Math.pow(10.0, v);
        }

        @Deprecated
        public FixedDecimal(double n, int v, long f, int e) {
            this(n, v, f, e, e);
        }

        @Deprecated
        public FixedDecimal(double n, int v, long f) {
            this(n, v, f, 0);
        }

        @Deprecated
        public static FixedDecimal createWithExponent(double n, int v, int e) {
            return new FixedDecimal(n, v, FixedDecimal.getFractionalDigits(n, v), e);
        }

        @Deprecated
        public FixedDecimal(double n, int v) {
            this(n, v, FixedDecimal.getFractionalDigits(n, v));
        }

        private static int getFractionalDigits(double n, int v) {
            if (v == 0) {
                return 0;
            }
            if (n < 0.0) {
                n = -n;
            }
            int baseFactor = (int)Math.pow(10.0, v);
            long scaled = Math.round(n * (double)baseFactor);
            return (int)(scaled % (long)baseFactor);
        }

        @Deprecated
        public FixedDecimal(double n) {
            this(n, FixedDecimal.decimals(n));
        }

        @Deprecated
        public FixedDecimal(long n) {
            this(n, 0);
        }

        @Deprecated
        public static int decimals(double n) {
            String exponentStr;
            int exponent;
            int numFractionDigits;
            int ePos;
            int expNumPos;
            if (Double.isInfinite(n) || Double.isNaN(n)) {
                return 0;
            }
            if (n < 0.0) {
                n = -n;
            }
            if (n == Math.floor(n)) {
                return 0;
            }
            if (n < 1.0E9) {
                long temp = (long)(n * 1000000.0) % 1000000L;
                int mask = 10;
                for (int digits = 6; digits > 0; --digits) {
                    if (temp % (long)mask != 0L) {
                        return digits;
                    }
                    mask *= 10;
                }
                return 0;
            }
            String buf = String.format(Locale.ENGLISH, "%1.15e", n);
            if (buf.charAt(expNumPos = (ePos = buf.lastIndexOf(101)) + 1) == '+') {
                ++expNumPos;
            }
            if ((numFractionDigits = ePos - 2 - (exponent = Integer.parseInt(exponentStr = buf.substring(expNumPos)))) < 0) {
                return 0;
            }
            int i = ePos - 1;
            while (numFractionDigits > 0 && buf.charAt(i) == '0') {
                --numFractionDigits;
                --i;
            }
            return numFractionDigits;
        }

        @Deprecated
        private FixedDecimal(FixedDecimal other) {
            this.source = other.source;
            this.visibleDecimalDigitCount = other.visibleDecimalDigitCount;
            this.visibleDecimalDigitCountWithoutTrailingZeros = other.visibleDecimalDigitCountWithoutTrailingZeros;
            this.decimalDigits = other.decimalDigits;
            this.decimalDigitsWithoutTrailingZeros = other.decimalDigitsWithoutTrailingZeros;
            this.integerValue = other.integerValue;
            this.hasIntegerValue = other.hasIntegerValue;
            this.isNegative = other.isNegative;
            this.exponent = other.exponent;
            this.baseFactor = other.baseFactor;
        }

        @Override
        @Deprecated
        public double getPluralOperand(Operand operand) {
            switch (operand) {
                case n: {
                    return this.exponent == 0 ? this.source : this.source * Math.pow(10.0, this.exponent);
                }
                case i: {
                    return this.intValue();
                }
                case f: {
                    return this.decimalDigits;
                }
                case t: {
                    return this.decimalDigitsWithoutTrailingZeros;
                }
                case v: {
                    return this.visibleDecimalDigitCount;
                }
                case w: {
                    return this.visibleDecimalDigitCountWithoutTrailingZeros;
                }
                case e: {
                    return this.exponent;
                }
                case c: {
                    return this.exponent;
                }
            }
            return this.doubleValue();
        }

        @Deprecated
        public static Operand getOperand(String t) {
            return Operand.valueOf(t);
        }

        @Override
        @Deprecated
        public int compareTo(FixedDecimal other) {
            if (this.exponent != other.exponent) {
                return this.doubleValue() < other.doubleValue() ? -1 : 1;
            }
            if (this.integerValue != other.integerValue) {
                return this.integerValue < other.integerValue ? -1 : 1;
            }
            if (this.source != other.source) {
                return this.source < other.source ? -1 : 1;
            }
            if (this.visibleDecimalDigitCount != other.visibleDecimalDigitCount) {
                return this.visibleDecimalDigitCount < other.visibleDecimalDigitCount ? -1 : 1;
            }
            long diff = this.decimalDigits - other.decimalDigits;
            if (diff != 0L) {
                return diff < 0L ? -1 : 1;
            }
            return 0;
        }

        @Deprecated
        public boolean equals(Object arg0) {
            if (arg0 == null) {
                return false;
            }
            if (arg0 == this) {
                return true;
            }
            if (!(arg0 instanceof FixedDecimal)) {
                return false;
            }
            FixedDecimal other = (FixedDecimal)arg0;
            return this.source == other.source && this.visibleDecimalDigitCount == other.visibleDecimalDigitCount && this.decimalDigits == other.decimalDigits && this.exponent == other.exponent;
        }

        @Deprecated
        public int hashCode() {
            return (int)(this.decimalDigits + (long)(37 * (this.visibleDecimalDigitCount + (int)(37.0 * this.source))));
        }

        @Deprecated
        public String toString() {
            String baseString = String.format(Locale.ROOT, "%." + this.visibleDecimalDigitCount + "f", this.source);
            if (this.exponent != 0) {
                return baseString + "e" + this.exponent;
            }
            return baseString;
        }

        @Deprecated
        public boolean hasIntegerValue() {
            return this.hasIntegerValue;
        }

        @Override
        @Deprecated
        public int intValue() {
            return (int)this.longValue();
        }

        @Override
        @Deprecated
        public long longValue() {
            if (this.exponent == 0) {
                return this.integerValue;
            }
            return (long)(Math.pow(10.0, this.exponent) * (double)this.integerValue);
        }

        @Override
        @Deprecated
        public float floatValue() {
            return (float)(this.source * Math.pow(10.0, this.exponent));
        }

        @Override
        @Deprecated
        public double doubleValue() {
            return (this.isNegative ? -this.source : this.source) * Math.pow(10.0, this.exponent);
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            throw new NotSerializableException();
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            throw new NotSerializableException();
        }

        @Override
        @Deprecated
        public boolean isNaN() {
            return Double.isNaN(this.source);
        }

        @Override
        @Deprecated
        public boolean isInfinite() {
            return Double.isInfinite(this.source);
        }
    }

    @Deprecated
    public static interface IFixedDecimal {
        @Deprecated
        public double getPluralOperand(Operand var1);

        @Deprecated
        public boolean isNaN();

        @Deprecated
        public boolean isInfinite();

        @Deprecated
        public boolean isHasIntegerValue();
    }

    @Deprecated
    public static enum Operand {
        n,
        i,
        f,
        t,
        v,
        w,
        e,
        c,
        j;

    }

    public static enum PluralType {
        CARDINAL,
        ORDINAL;

    }

    @Deprecated
    public static abstract class Factory {
        @Deprecated
        protected Factory() {
        }

        @Deprecated
        public abstract PluralRules forLocale(ULocale var1, PluralType var2);

        @Deprecated
        public final PluralRules forLocale(ULocale locale) {
            return this.forLocale(locale, PluralType.CARDINAL);
        }

        @Deprecated
        public abstract ULocale[] getAvailableULocales();

        @Deprecated
        public abstract ULocale getFunctionalEquivalent(ULocale var1, boolean[] var2);

        @Deprecated
        public static PluralRulesLoader getDefaultFactory() {
            return PluralRulesLoader.loader;
        }

        @Deprecated
        public abstract boolean hasOverride(ULocale var1);
    }
}

