/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl.personname;

import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.impl.personname.PersonNamePattern;
import com.ibm.icu.lang.UScript;
import com.ibm.icu.text.PersonName;
import com.ibm.icu.text.PersonNameFormatter;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.UResourceBundle;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class PersonNameFormatterImpl {
    private final Locale locale;
    private final PersonNamePattern[] gnFirstPatterns;
    private final PersonNamePattern[] snFirstPatterns;
    private final Set<String> gnFirstLocales;
    private final Set<String> snFirstLocales;
    private final String initialPattern;
    private final String initialSequencePattern;
    private final boolean capitalizeSurname;
    private final String foreignSpaceReplacement;
    private final String nativeSpaceReplacement;
    private final PersonNameFormatter.Length length;
    private final PersonNameFormatter.Usage usage;
    private final PersonNameFormatter.Formality formality;
    private final PersonNameFormatter.DisplayOrder displayOrder;
    static final Set<String> NON_DEFAULT_SCRIPTS = new HashSet<String>(Arrays.asList("Hani", "Hira", "Kana"));

    public PersonNameFormatterImpl(Locale locale, PersonNameFormatter.Length length, PersonNameFormatter.Usage usage, PersonNameFormatter.Formality formality, PersonNameFormatter.DisplayOrder displayOrder, boolean surnameAllCaps) {
        this.length = length;
        this.usage = usage;
        this.formality = formality;
        this.displayOrder = displayOrder;
        this.capitalizeSurname = surnameAllCaps;
        ICUResourceBundle rb = (ICUResourceBundle)UResourceBundle.getBundleInstance("com/ibm/icu/impl/data/icudata", locale);
        this.locale = locale;
        this.initialPattern = rb.getStringWithFallback("personNames/initialPattern/initial");
        this.initialSequencePattern = rb.getStringWithFallback("personNames/initialPattern/initialSequence");
        this.foreignSpaceReplacement = rb.getStringWithFallback("personNames/foreignSpaceReplacement");
        this.nativeSpaceReplacement = rb.getStringWithFallback("personNames/nativeSpaceReplacement");
        if (usage == PersonNameFormatter.Usage.MONOGRAM) {
            displayOrder = PersonNameFormatter.DisplayOrder.DEFAULT;
        } else if (displayOrder == PersonNameFormatter.DisplayOrder.SORTING) {
            usage = PersonNameFormatter.Usage.REFERRING;
        }
        String RESOURCE_PATH_PREFIX = "personNames/namePattern/";
        String lengthStr = length != PersonNameFormatter.Length.DEFAULT ? length.toString().toLowerCase() : rb.getStringWithFallback("personNames/parameterDefault/length");
        String formalityStr = formality != PersonNameFormatter.Formality.DEFAULT ? formality.toString().toLowerCase() : rb.getStringWithFallback("personNames/parameterDefault/formality");
        String resourceNameBody = lengthStr + "-" + usage.toString().toLowerCase() + "-" + formalityStr;
        if (displayOrder != PersonNameFormatter.DisplayOrder.SORTING) {
            ICUResourceBundle gnFirstResource = rb.getWithFallback("personNames/namePattern/givenFirst-" + resourceNameBody);
            ICUResourceBundle snFirstResource = rb.getWithFallback("personNames/namePattern/surnameFirst-" + resourceNameBody);
            this.gnFirstPatterns = PersonNamePattern.makePatterns(this.asStringArray(gnFirstResource), this);
            this.snFirstPatterns = PersonNamePattern.makePatterns(this.asStringArray(snFirstResource), this);
            this.gnFirstLocales = new HashSet<String>();
            Collections.addAll(this.gnFirstLocales, this.asStringArray(rb.getWithFallback("personNames/nameOrderLocales/givenFirst")));
            this.snFirstLocales = new HashSet<String>();
            Collections.addAll(this.snFirstLocales, this.asStringArray(rb.getWithFallback("personNames/nameOrderLocales/surnameFirst")));
        } else {
            ICUResourceBundle patternResource = rb.getWithFallback("personNames/namePattern/sorting-" + resourceNameBody);
            this.gnFirstPatterns = PersonNamePattern.makePatterns(this.asStringArray(patternResource), this);
            this.snFirstPatterns = null;
            this.gnFirstLocales = null;
            this.snFirstLocales = null;
        }
    }

    public PersonNameFormatterImpl(Locale locale, String[] gnFirstPatterns, String[] snFirstPatterns, String[] gnFirstLocales, String[] snFirstLocales) {
        this.length = PersonNameFormatter.Length.MEDIUM;
        this.usage = PersonNameFormatter.Usage.REFERRING;
        this.formality = PersonNameFormatter.Formality.FORMAL;
        this.displayOrder = PersonNameFormatter.DisplayOrder.DEFAULT;
        this.initialPattern = "{0}.";
        this.initialSequencePattern = "{0} {1}";
        this.capitalizeSurname = false;
        this.foreignSpaceReplacement = " ";
        this.nativeSpaceReplacement = " ";
        this.locale = locale;
        this.gnFirstPatterns = PersonNamePattern.makePatterns(gnFirstPatterns, this);
        PersonNamePattern[] personNamePatternArray = this.snFirstPatterns = snFirstPatterns != null ? PersonNamePattern.makePatterns(snFirstPatterns, this) : null;
        if (gnFirstLocales != null) {
            this.gnFirstLocales = new HashSet<String>();
            Collections.addAll(this.gnFirstLocales, gnFirstLocales);
        } else {
            this.gnFirstLocales = null;
        }
        if (snFirstLocales != null) {
            this.snFirstLocales = new HashSet<String>();
            Collections.addAll(this.snFirstLocales, snFirstLocales);
        } else {
            this.snFirstLocales = null;
        }
    }

    public String toString() {
        return "PersonNameFormatter: " + (Object)((Object)this.displayOrder) + "-" + (Object)((Object)this.length) + "-" + (Object)((Object)this.usage) + "-" + (Object)((Object)this.formality) + ", " + this.locale;
    }

    public String formatToString(PersonName name) {
        Locale nameLocale = this.getNameLocale(name);
        String nameScript = this.getNameScript(name);
        if (!this.nameScriptMatchesLocale(nameScript, this.locale)) {
            Locale newFormattingLocale = this.formattingLocaleExists(nameLocale) ? nameLocale : this.newLocaleWithScript(null, nameScript, nameLocale.getCountry());
            PersonNameFormatterImpl nameLocaleFormatter = new PersonNameFormatterImpl(newFormattingLocale, this.length, this.usage, this.formality, this.displayOrder, this.capitalizeSurname);
            return nameLocaleFormatter.formatToString(name);
        }
        String result = null;
        result = this.snFirstPatterns == null || this.nameIsGnFirst(name) ? this.getBestPattern(this.gnFirstPatterns, name).format(name) : this.getBestPattern(this.snFirstPatterns, name).format(name);
        if (!this.nativeSpaceReplacement.equals(" ") || !this.foreignSpaceReplacement.equals(" ")) {
            result = this.localesMatch(nameLocale, this.locale) ? result.replace(" ", this.nativeSpaceReplacement) : result.replace(" ", this.foreignSpaceReplacement);
        }
        return result;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public PersonNameFormatter.Length getLength() {
        return this.length;
    }

    public PersonNameFormatter.Usage getUsage() {
        return this.usage;
    }

    public PersonNameFormatter.Formality getFormality() {
        return this.formality;
    }

    public PersonNameFormatter.DisplayOrder getDisplayOrder() {
        return this.displayOrder;
    }

    public boolean getSurnameAllCaps() {
        return this.capitalizeSurname;
    }

    public String getInitialPattern() {
        return this.initialPattern;
    }

    public String getInitialSequencePattern() {
        return this.initialSequencePattern;
    }

    public boolean shouldCapitalizeSurname() {
        return this.capitalizeSurname;
    }

    private String[] asStringArray(ICUResourceBundle resource) {
        if (resource.getType() == 0) {
            return new String[]{resource.getString()};
        }
        if (resource.getType() == 8) {
            return resource.getStringArray();
        }
        throw new IllegalStateException("Unsupported resource type " + resource.getType());
    }

    private boolean nameIsGnFirst(PersonName name) {
        String parentLocaleStr;
        String localeStr;
        ULocale nameULocale;
        if (this.displayOrder == PersonNameFormatter.DisplayOrder.FORCE_GIVEN_FIRST) {
            return true;
        }
        if (this.displayOrder == PersonNameFormatter.DisplayOrder.FORCE_SURNAME_FIRST) {
            return false;
        }
        if (name.getPreferredOrder() == PersonName.PreferredOrder.GIVEN_FIRST) {
            return true;
        }
        if (name.getPreferredOrder() == PersonName.PreferredOrder.SURNAME_FIRST) {
            return false;
        }
        Locale nameLocale = name.getNameLocale();
        if (nameLocale == null) {
            nameLocale = this.getNameLocale(name);
        }
        if (NON_DEFAULT_SCRIPTS.contains((nameULocale = ULocale.forLocale(nameLocale)).getScript())) {
            ULocale.Builder builder = new ULocale.Builder();
            builder.setLocale(nameULocale);
            builder.setScript(null);
            nameULocale = ULocale.addLikelySubtags(builder.build());
        }
        String origLocaleStr = localeStr = nameULocale.getName();
        String languageCode = nameULocale.getLanguage();
        do {
            if (this.gnFirstLocales.contains(localeStr)) {
                return true;
            }
            if (this.snFirstLocales.contains(localeStr)) {
                return false;
            }
            String undStr = localeStr.replaceAll("^" + languageCode, "und");
            if (this.gnFirstLocales.contains(undStr)) {
                return true;
            }
            if (!this.snFirstLocales.contains(undStr)) continue;
            return false;
        } while ((localeStr = (parentLocaleStr = ICUResourceBundle.getParentLocaleID(localeStr, origLocaleStr, ICUResourceBundle.OpenType.LOCALE_DEFAULT_ROOT)) != null ? parentLocaleStr : languageCode) != null);
        return true;
    }

    private PersonNamePattern getBestPattern(PersonNamePattern[] patterns, PersonName name) {
        if (patterns.length == 1) {
            return patterns[0];
        }
        int maxPopulatedFields = 0;
        int minEmptyFields = Integer.MAX_VALUE;
        PersonNamePattern bestPattern = null;
        for (PersonNamePattern pattern : patterns) {
            int populatedFields = pattern.numPopulatedFields(name);
            int emptyFields = pattern.numEmptyFields(name);
            if (populatedFields > maxPopulatedFields) {
                maxPopulatedFields = populatedFields;
                minEmptyFields = emptyFields;
                bestPattern = pattern;
                continue;
            }
            if (populatedFields != maxPopulatedFields || emptyFields >= minEmptyFields) continue;
            minEmptyFields = emptyFields;
            bestPattern = pattern;
        }
        return bestPattern;
    }

    private String getNameScript(PersonName name) {
        String givenName = name.getFieldValue(PersonName.NameField.SURNAME, Collections.emptySet());
        String surname = name.getFieldValue(PersonName.NameField.GIVEN, Collections.emptySet());
        String nameText = (surname != null ? surname : "") + (givenName != null ? givenName : "");
        int stringScript = 103;
        for (int i = 0; stringScript == 103 && i < nameText.length(); ++i) {
            int c = nameText.codePointAt(i);
            int charScript = UScript.getScript(c);
            if (charScript == 0 || charScript == 1 || charScript == 103) continue;
            stringScript = charScript;
        }
        return UScript.getShortName(stringScript);
    }

    private Locale newLocaleWithScript(Locale oldLocale, String scriptCode, String regionCode) {
        String localeScript;
        Locale workingLocale;
        if (scriptCode.equals("Zzzz")) {
            return oldLocale;
        }
        Locale.Builder builder = new Locale.Builder();
        if (oldLocale != null) {
            workingLocale = oldLocale;
            builder.setLocale(oldLocale);
            localeScript = ULocale.addLikelySubtags(ULocale.forLocale(oldLocale)).getScript();
        } else {
            ULocale tmpLocale = ULocale.addLikelySubtags(new ULocale("und_" + scriptCode));
            builder.setLanguage(tmpLocale.getLanguage());
            workingLocale = ULocale.addLikelySubtags(new ULocale(tmpLocale.getLanguage())).toLocale();
            localeScript = workingLocale.getScript();
            if (regionCode != null) {
                builder.setRegion(regionCode);
            }
        }
        if (!scriptCode.equals(localeScript) && this.nameScriptMatchesLocale(scriptCode, workingLocale)) {
            scriptCode = localeScript;
        }
        builder.setScript(scriptCode);
        return builder.build();
    }

    private Locale getNameLocale(PersonName name) {
        return this.newLocaleWithScript(name.getNameLocale(), this.getNameScript(name), null);
    }

    private boolean nameScriptMatchesLocale(String nameScriptID, Locale formatterLocale) {
        if (nameScriptID.equals("Zzzz")) {
            return true;
        }
        int[] localeScripts = UScript.getCode(formatterLocale);
        int nameScript = UScript.getCodeFromName(nameScriptID);
        for (int localeScript : localeScripts) {
            if (localeScript != nameScript && (localeScript != 73 || nameScript != 17) && (localeScript != 74 || nameScript != 17)) continue;
            return true;
        }
        return false;
    }

    private boolean formattingLocaleExists(Locale formattingLocale) {
        String formattingLanguage = formattingLocale.getLanguage();
        ICUResourceBundle mainRB = ICUResourceBundle.getBundleInstance("com/ibm/icu/impl/data/icudata", ULocale.forLocale(formattingLocale), ICUResourceBundle.OpenType.LOCALE_DEFAULT_ROOT);
        if (!mainRB.getULocale().getLanguage().equals(formattingLanguage)) {
            return false;
        }
        ICUResourceBundle gnFirstResource = mainRB.getWithFallback("personNames/nameOrderLocales/givenFirst");
        ICUResourceBundle snFirstResource = mainRB.getWithFallback("personNames/nameOrderLocales/surnameFirst");
        return gnFirstResource.getULocale().getLanguage().equals(formattingLanguage) || snFirstResource.getULocale().getLanguage().equals(formattingLanguage);
    }

    private boolean localesMatch(Locale nameLocale, Locale formatterLocale) {
        String formatterLanguage;
        String nameLanguage = nameLocale.getLanguage();
        if (nameLanguage.equals(formatterLanguage = formatterLocale.getLanguage())) {
            return true;
        }
        return !(!nameLanguage.equals("ja") && !nameLanguage.equals("zh") || !formatterLanguage.equals("ja") && !formatterLanguage.equals("zh"));
    }
}

