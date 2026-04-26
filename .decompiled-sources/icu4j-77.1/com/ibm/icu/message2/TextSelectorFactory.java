/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.message2;

import com.ibm.icu.message2.MFDataModel;
import com.ibm.icu.message2.OptUtils;
import com.ibm.icu.message2.Selector;
import com.ibm.icu.message2.SelectorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

class TextSelectorFactory
implements SelectorFactory {
    TextSelectorFactory() {
    }

    @Override
    public Selector createSelector(Locale locale, Map<String, Object> fixedOptions) {
        return new TextSelector();
    }

    private static class TextSelector
    implements Selector {
        private TextSelector() {
        }

        @Override
        public List<String> matches(Object value, List<String> keys, Map<String, Object> variableOptions) {
            ArrayList<String> result = new ArrayList<String>();
            if (value == null) {
                if (OptUtils.reportErrors(variableOptions)) {
                    throw new IllegalArgumentException("unresolved-variable: argument to match on can't be null");
                }
                return result;
            }
            for (String key : keys) {
                if (!this.matches(value, key)) continue;
                result.add(key);
            }
            result.sort(String::compareTo);
            return result;
        }

        private boolean matches(Object value, String key) {
            if (MFDataModel.CatchallKey.isCatchAll(key)) {
                return true;
            }
            return key.equals(Objects.toString(value));
        }
    }
}

