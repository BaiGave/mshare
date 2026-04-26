/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl;

import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.impl.number.range.StandardPluralRanges;
import com.ibm.icu.text.PluralRules;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.UResourceBundle;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeMap;

public class PluralRulesLoader
extends PluralRules.Factory {
    private final Map<String, PluralRules> pluralRulesCache = new HashMap<String, PluralRules>();
    private Map<String, String> localeIdToCardinalRulesId;
    private Map<String, String> localeIdToOrdinalRulesId;
    private Map<String, ULocale> rulesIdToEquivalentULocale;
    public static final PluralRulesLoader loader = new PluralRulesLoader();

    private PluralRulesLoader() {
    }

    @Override
    public ULocale[] getAvailableULocales() {
        Set<String> keys = this.getLocaleIdToRulesIdMap(PluralRules.PluralType.CARDINAL).keySet();
        LinkedHashSet<ULocale> locales = new LinkedHashSet<ULocale>(keys.size());
        Iterator<String> iter = keys.iterator();
        while (iter.hasNext()) {
            locales.add(ULocale.createCanonical(iter.next()));
        }
        return locales.toArray(new ULocale[0]);
    }

    @Override
    public ULocale getFunctionalEquivalent(ULocale locale, boolean[] isAvailable) {
        String rulesId;
        if (isAvailable != null && isAvailable.length > 0) {
            String localeId = ULocale.canonicalize(locale.getBaseName());
            Map<String, String> idMap = this.getLocaleIdToRulesIdMap(PluralRules.PluralType.CARDINAL);
            isAvailable[0] = idMap.containsKey(localeId);
        }
        if ((rulesId = this.getRulesIdForLocale(locale, PluralRules.PluralType.CARDINAL)) == null || rulesId.trim().length() == 0) {
            return ULocale.ROOT;
        }
        ULocale result = this.getRulesIdToEquivalentULocaleMap().get(rulesId);
        if (result == null) {
            return ULocale.ROOT;
        }
        return result;
    }

    private Map<String, String> getLocaleIdToRulesIdMap(PluralRules.PluralType type) {
        this.checkBuildRulesIdMaps();
        return type == PluralRules.PluralType.CARDINAL ? this.localeIdToCardinalRulesId : this.localeIdToOrdinalRulesId;
    }

    private Map<String, ULocale> getRulesIdToEquivalentULocaleMap() {
        this.checkBuildRulesIdMaps();
        return this.rulesIdToEquivalentULocale;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void checkBuildRulesIdMaps() {
        boolean haveMap;
        PluralRulesLoader pluralRulesLoader = this;
        synchronized (pluralRulesLoader) {
            haveMap = this.localeIdToCardinalRulesId != null;
        }
        if (!haveMap) {
            Map<String, String> tempLocaleIdToOrdinalRulesId;
            Map<String, ULocale> tempRulesIdToEquivalentULocale;
            Map<String, String> tempLocaleIdToCardinalRulesId;
            try {
                String value;
                String id;
                UResourceBundle b;
                int i;
                UResourceBundle pluralb = this.getPluralBundle();
                UResourceBundle localeb = pluralb.get("locales");
                tempLocaleIdToCardinalRulesId = new TreeMap();
                tempRulesIdToEquivalentULocale = new HashMap();
                for (i = 0; i < localeb.getSize(); ++i) {
                    b = localeb.get(i);
                    id = b.getKey();
                    value = b.getString().intern();
                    tempLocaleIdToCardinalRulesId.put(id, value);
                    if (tempRulesIdToEquivalentULocale.containsKey(value)) continue;
                    tempRulesIdToEquivalentULocale.put(value, new ULocale(id));
                }
                localeb = pluralb.get("locales_ordinals");
                tempLocaleIdToOrdinalRulesId = new TreeMap();
                for (i = 0; i < localeb.getSize(); ++i) {
                    b = localeb.get(i);
                    id = b.getKey();
                    value = b.getString().intern();
                    tempLocaleIdToOrdinalRulesId.put(id, value);
                }
            }
            catch (MissingResourceException e) {
                tempLocaleIdToCardinalRulesId = Collections.emptyMap();
                tempLocaleIdToOrdinalRulesId = Collections.emptyMap();
                tempRulesIdToEquivalentULocale = Collections.emptyMap();
            }
            PluralRulesLoader pluralRulesLoader2 = this;
            synchronized (pluralRulesLoader2) {
                if (this.localeIdToCardinalRulesId == null) {
                    this.localeIdToCardinalRulesId = tempLocaleIdToCardinalRulesId;
                    this.localeIdToOrdinalRulesId = tempLocaleIdToOrdinalRulesId;
                    this.rulesIdToEquivalentULocale = tempRulesIdToEquivalentULocale;
                }
            }
        }
    }

    public String getRulesIdForLocale(ULocale locale, PluralRules.PluralType type) {
        int ix;
        Map<String, String> idMap = this.getLocaleIdToRulesIdMap(type);
        String localeId = ULocale.canonicalize(locale.getBaseName());
        String rulesId = null;
        while (null == (rulesId = idMap.get(localeId)) && (ix = localeId.lastIndexOf("_")) != -1) {
            localeId = localeId.substring(0, ix);
        }
        return rulesId;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public PluralRules getOrCreateRulesForLocale(ULocale locale, PluralRules.PluralType type) {
        boolean hasRules;
        String rulesId = this.getRulesIdForLocale(locale, type);
        if (rulesId == null || rulesId.trim().length() == 0) {
            return null;
        }
        String rangesId = StandardPluralRanges.getSetForLocale(locale);
        String cacheKey = rulesId + "/" + rangesId;
        PluralRules rules = null;
        Map<String, PluralRules> map = this.pluralRulesCache;
        synchronized (map) {
            hasRules = this.pluralRulesCache.containsKey(cacheKey);
            if (hasRules) {
                rules = this.pluralRulesCache.get(cacheKey);
            }
        }
        if (!hasRules) {
            try {
                UResourceBundle pluralb = this.getPluralBundle();
                UResourceBundle rulesb = pluralb.get("rules");
                UResourceBundle setb = rulesb.get(rulesId);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < setb.getSize(); ++i) {
                    UResourceBundle b = setb.get(i);
                    if (i > 0) {
                        sb.append("; ");
                    }
                    sb.append(b.getKey());
                    sb.append(": ");
                    sb.append(b.getString());
                }
                StandardPluralRanges ranges = StandardPluralRanges.forSet(rangesId);
                rules = PluralRules.newInternal(sb.toString(), ranges);
            }
            catch (ParseException parseException) {
            }
            catch (MissingResourceException missingResourceException) {
                // empty catch block
            }
            map = this.pluralRulesCache;
            synchronized (map) {
                if (this.pluralRulesCache.containsKey(cacheKey)) {
                    rules = this.pluralRulesCache.get(cacheKey);
                } else {
                    this.pluralRulesCache.put(cacheKey, rules);
                }
            }
        }
        return rules;
    }

    public UResourceBundle getPluralBundle() throws MissingResourceException {
        return ICUResourceBundle.getBundleInstance("com/ibm/icu/impl/data/icudata", "plurals", ICUResourceBundle.ICU_DATA_CLASS_LOADER, true);
    }

    @Override
    public PluralRules forLocale(ULocale locale, PluralRules.PluralType type) {
        PluralRules rules = this.getOrCreateRulesForLocale(locale, type);
        if (rules == null) {
            rules = PluralRules.DEFAULT;
        }
        return rules;
    }

    @Override
    public boolean hasOverride(ULocale locale) {
        return false;
    }
}

