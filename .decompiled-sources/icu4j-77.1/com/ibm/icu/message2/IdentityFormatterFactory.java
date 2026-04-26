/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.message2;

import com.ibm.icu.message2.Directionality;
import com.ibm.icu.message2.FormattedPlaceholder;
import com.ibm.icu.message2.Formatter;
import com.ibm.icu.message2.FormatterFactory;
import com.ibm.icu.message2.OptUtils;
import com.ibm.icu.message2.PlainStringFormattedValue;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

class IdentityFormatterFactory
implements FormatterFactory {
    IdentityFormatterFactory() {
    }

    @Override
    public Formatter createFormatter(Locale locale, Map<String, Object> fixedOptions) {
        return new IdentityFormatterImpl(OptUtils.getDirectionality(fixedOptions));
    }

    private static class IdentityFormatterImpl
    implements Formatter {
        private final Directionality directionality;

        public IdentityFormatterImpl(Directionality directionality) {
            this.directionality = directionality == null ? Directionality.INHERIT : directionality;
        }

        @Override
        public FormattedPlaceholder format(Object toFormat, Map<String, Object> variableOptions) {
            return new FormattedPlaceholder(toFormat, new PlainStringFormattedValue(Objects.toString(toFormat)), this.directionality, true);
        }

        @Override
        public String formatToString(Object toFormat, Map<String, Object> variableOptions) {
            return this.format(toFormat, variableOptions).toString();
        }
    }
}

