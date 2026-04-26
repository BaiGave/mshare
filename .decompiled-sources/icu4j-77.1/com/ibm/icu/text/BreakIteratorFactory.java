/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.text;

import com.ibm.icu.impl.Assert;
import com.ibm.icu.impl.ICUBinary;
import com.ibm.icu.impl.ICULocaleService;
import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.impl.ICUService;
import com.ibm.icu.text.BreakIterator;
import com.ibm.icu.text.FilteredBreakIteratorBuilder;
import com.ibm.icu.text.RuleBasedBreakIterator;
import com.ibm.icu.util.ULocale;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.StringCharacterIterator;
import java.util.Locale;
import java.util.MissingResourceException;

final class BreakIteratorFactory
extends BreakIterator.BreakIteratorServiceShim {
    static final ICULocaleService service = new BFService();
    private static final String[] KIND_NAMES = new String[]{"grapheme", "word", "line", "sentence", "title"};

    BreakIteratorFactory() {
    }

    @Override
    public Object registerInstance(BreakIterator iter, ULocale locale, int kind) {
        iter.setText(new StringCharacterIterator(""));
        return service.registerObject((Object)iter, locale, kind);
    }

    @Override
    public boolean unregister(Object key) {
        if (service.isDefault()) {
            return false;
        }
        return service.unregisterFactory((ICUService.Factory)key);
    }

    @Override
    public Locale[] getAvailableLocales() {
        if (service == null) {
            return ICUResourceBundle.getAvailableLocales();
        }
        return service.getAvailableLocales();
    }

    @Override
    public ULocale[] getAvailableULocales() {
        if (service == null) {
            return ICUResourceBundle.getAvailableULocales();
        }
        return service.getAvailableULocales();
    }

    @Override
    public BreakIterator createBreakIterator(ULocale locale, int kind) {
        if (service.isDefault()) {
            return BreakIteratorFactory.createBreakInstance(locale, kind);
        }
        ULocale[] actualLoc = new ULocale[1];
        BreakIterator iter = (BreakIterator)service.get(locale, kind, actualLoc);
        iter.setLocale(actualLoc[0], actualLoc[0]);
        return iter;
    }

    private static BreakIterator createBreakInstance(ULocale locale, int kind) {
        String ssKeyword;
        String brkfname;
        RuleBasedBreakIterator iter = null;
        ICUResourceBundle rb = ICUResourceBundle.getBundleInstance("com/ibm/icu/impl/data/icudata/brkitr", locale, ICUResourceBundle.OpenType.LOCALE_ROOT);
        ByteBuffer bytes = null;
        String typeKeyExt = "";
        if (kind == 2) {
            String language;
            String keyValue = locale.getKeywordValue("lb");
            if (keyValue != null && (keyValue.equals("strict") || keyValue.equals("normal") || keyValue.equals("loose"))) {
                typeKeyExt = "_" + keyValue;
            }
            if ((language = locale.getLanguage()) != null && (language.equals("ja") || language.equals("ko")) && (keyValue = locale.getKeywordValue("lw")) != null && keyValue.equals("phrase")) {
                typeKeyExt = typeKeyExt + "_" + keyValue;
            }
        }
        try {
            String typeKey = typeKeyExt.isEmpty() ? KIND_NAMES[kind] : KIND_NAMES[kind] + typeKeyExt;
            brkfname = rb.getStringWithFallback("boundaries/" + typeKey);
            String rulesFileName = "brkitr/" + brkfname;
            bytes = ICUBinary.getRequiredData(rulesFileName);
        }
        catch (Exception e) {
            throw new MissingResourceException(e.toString(), "", "");
        }
        try {
            boolean isPhraseBreaking = brkfname != null && brkfname.contains("phrase");
            iter = RuleBasedBreakIterator.getInstanceFromCompiledRules(bytes, isPhraseBreaking);
        }
        catch (IOException e) {
            Assert.fail(e);
        }
        ULocale uloc = ULocale.forLocale(rb.getLocale());
        iter.setLocale(uloc, uloc);
        if (kind == 3 && (ssKeyword = locale.getKeywordValue("ss")) != null && ssKeyword.equals("standard")) {
            ULocale base = new ULocale(locale.getBaseName());
            return FilteredBreakIteratorBuilder.getInstance(base).wrapIteratorWithFilter(iter);
        }
        return iter;
    }

    private static class BFService
    extends ICULocaleService {
        BFService() {
            super("BreakIterator");
            class RBBreakIteratorFactory
            extends ICULocaleService.ICUResourceBundleFactory {
                RBBreakIteratorFactory() {
                }

                @Override
                protected Object handleCreate(ULocale loc, int kind, ICUService srvc) {
                    return BreakIteratorFactory.createBreakInstance(loc, kind);
                }
            }
            this.registerFactory(new RBBreakIteratorFactory());
            this.markDefault();
        }

        @Override
        public String validateFallbackLocale() {
            return "";
        }
    }
}

