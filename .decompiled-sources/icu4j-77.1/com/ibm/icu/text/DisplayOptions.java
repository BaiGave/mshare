/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.text;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class DisplayOptions {
    private final GrammaticalCase grammaticalCase;
    private final NounClass nounClass;
    private final PluralCategory pluralCategory;
    private final Capitalization capitalization;
    private final NameStyle nameStyle;
    private final DisplayLength displayLength;
    private final SubstituteHandling substituteHandling;

    private DisplayOptions(Builder builder) {
        this.grammaticalCase = builder.grammaticalCase;
        this.nounClass = builder.nounClass;
        this.pluralCategory = builder.pluralCategory;
        this.capitalization = builder.capitalization;
        this.nameStyle = builder.nameStyle;
        this.displayLength = builder.displayLength;
        this.substituteHandling = builder.substituteHandling;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder copyToBuilder() {
        return new Builder(this);
    }

    public GrammaticalCase getGrammaticalCase() {
        return this.grammaticalCase;
    }

    public NounClass getNounClass() {
        return this.nounClass;
    }

    public PluralCategory getPluralCategory() {
        return this.pluralCategory;
    }

    public Capitalization getCapitalization() {
        return this.capitalization;
    }

    public NameStyle getNameStyle() {
        return this.nameStyle;
    }

    public DisplayLength getDisplayLength() {
        return this.displayLength;
    }

    public SubstituteHandling getSubstituteHandling() {
        return this.substituteHandling;
    }

    public static enum GrammaticalCase {
        UNDEFINED("undefined"),
        ABLATIVE("ablative"),
        ACCUSATIVE("accusative"),
        COMITATIVE("comitative"),
        DATIVE("dative"),
        ERGATIVE("ergative"),
        GENITIVE("genitive"),
        INSTRUMENTAL("instrumental"),
        LOCATIVE("locative"),
        LOCATIVE_COPULATIVE("locative_copulative"),
        NOMINATIVE("nominative"),
        OBLIQUE("oblique"),
        PREPOSITIONAL("prepositional"),
        SOCIATIVE("sociative"),
        VOCATIVE("vocative");

        private final String identifier;
        public static final List<GrammaticalCase> VALUES;

        private GrammaticalCase(String identifier) {
            this.identifier = identifier;
        }

        public final String getIdentifier() {
            return this.identifier;
        }

        public static final GrammaticalCase fromIdentifier(String identifier) {
            if (identifier == null) {
                return UNDEFINED;
            }
            for (GrammaticalCase grammaticalCase : VALUES) {
                if (!identifier.equals(grammaticalCase.getIdentifier())) continue;
                return grammaticalCase;
            }
            return UNDEFINED;
        }

        static {
            VALUES = Collections.unmodifiableList(Arrays.asList(GrammaticalCase.values()));
        }
    }

    public static enum PluralCategory {
        UNDEFINED("undefined"),
        ZERO("zero"),
        ONE("one"),
        TWO("two"),
        FEW("few"),
        MANY("many"),
        OTHER("other");

        private final String identifier;
        public static final List<PluralCategory> VALUES;

        private PluralCategory(String identifier) {
            this.identifier = identifier;
        }

        public final String getIdentifier() {
            return this.identifier;
        }

        public static final PluralCategory fromIdentifier(String identifier) {
            if (identifier == null) {
                return UNDEFINED;
            }
            for (PluralCategory pluralCategory : VALUES) {
                if (!identifier.equals(pluralCategory.getIdentifier())) continue;
                return pluralCategory;
            }
            return UNDEFINED;
        }

        static {
            VALUES = Collections.unmodifiableList(Arrays.asList(PluralCategory.values()));
        }
    }

    public static enum Capitalization {
        UNDEFINED,
        BEGINNING_OF_SENTENCE,
        MIDDLE_OF_SENTENCE,
        STANDALONE,
        UI_LIST_OR_MENU;

        public static final List<Capitalization> VALUES;

        static {
            VALUES = Collections.unmodifiableList(Arrays.asList(Capitalization.values()));
        }
    }

    public static enum DisplayLength {
        UNDEFINED,
        LENGTH_FULL,
        LENGTH_SHORT;

        public static final List<DisplayLength> VALUES;

        static {
            VALUES = Collections.unmodifiableList(Arrays.asList(DisplayLength.values()));
        }
    }

    public static enum SubstituteHandling {
        UNDEFINED,
        SUBSTITUTE,
        NO_SUBSTITUTE;

        public static final List<SubstituteHandling> VALUES;

        static {
            VALUES = Collections.unmodifiableList(Arrays.asList(SubstituteHandling.values()));
        }
    }

    public static enum NameStyle {
        UNDEFINED,
        STANDARD_NAMES,
        DIALECT_NAMES;

        public static final List<NameStyle> VALUES;

        static {
            VALUES = Collections.unmodifiableList(Arrays.asList(NameStyle.values()));
        }
    }

    public static enum NounClass {
        UNDEFINED("undefined"),
        OTHER("other"),
        NEUTER("neuter"),
        FEMININE("feminine"),
        MASCULINE("masculine"),
        ANIMATE("animate"),
        INANIMATE("inanimate"),
        PERSONAL("personal"),
        COMMON("common");

        private final String identifier;
        public static final List<NounClass> VALUES;

        private NounClass(String identifier) {
            this.identifier = identifier;
        }

        public final String getIdentifier() {
            return this.identifier;
        }

        public static final NounClass fromIdentifier(String identifier) {
            if (identifier == null) {
                return UNDEFINED;
            }
            for (NounClass nounClass : VALUES) {
                if (!identifier.equals(nounClass.getIdentifier())) continue;
                return nounClass;
            }
            return UNDEFINED;
        }

        static {
            VALUES = Collections.unmodifiableList(Arrays.asList(NounClass.values()));
        }
    }

    public static class Builder {
        private GrammaticalCase grammaticalCase;
        private NounClass nounClass;
        private PluralCategory pluralCategory;
        private Capitalization capitalization;
        private NameStyle nameStyle;
        private DisplayLength displayLength;
        private SubstituteHandling substituteHandling;

        private Builder() {
            this.grammaticalCase = GrammaticalCase.UNDEFINED;
            this.nounClass = NounClass.UNDEFINED;
            this.pluralCategory = PluralCategory.UNDEFINED;
            this.capitalization = Capitalization.UNDEFINED;
            this.nameStyle = NameStyle.UNDEFINED;
            this.displayLength = DisplayLength.UNDEFINED;
            this.substituteHandling = SubstituteHandling.UNDEFINED;
        }

        private Builder(DisplayOptions displayOptions) {
            this.grammaticalCase = displayOptions.grammaticalCase;
            this.nounClass = displayOptions.nounClass;
            this.pluralCategory = displayOptions.pluralCategory;
            this.capitalization = displayOptions.capitalization;
            this.nameStyle = displayOptions.nameStyle;
            this.displayLength = displayOptions.displayLength;
            this.substituteHandling = displayOptions.substituteHandling;
        }

        public Builder setGrammaticalCase(GrammaticalCase grammaticalCase) {
            this.grammaticalCase = grammaticalCase;
            return this;
        }

        public Builder setNounClass(NounClass nounClass) {
            this.nounClass = nounClass;
            return this;
        }

        public Builder setPluralCategory(PluralCategory pluralCategory) {
            this.pluralCategory = pluralCategory;
            return this;
        }

        public Builder setCapitalization(Capitalization capitalization) {
            this.capitalization = capitalization;
            return this;
        }

        public Builder setNameStyle(NameStyle nameStyle) {
            this.nameStyle = nameStyle;
            return this;
        }

        public Builder setDisplayLength(DisplayLength displayLength) {
            this.displayLength = displayLength;
            return this;
        }

        public Builder setSubstituteHandling(SubstituteHandling substituteHandling) {
            this.substituteHandling = substituteHandling;
            return this;
        }

        public DisplayOptions build() {
            DisplayOptions displayOptions = new DisplayOptions(this);
            return displayOptions;
        }
    }
}

