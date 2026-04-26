/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.message2;

import com.ibm.icu.message2.Selector;
import java.util.Locale;
import java.util.Map;

@Deprecated
public interface SelectorFactory {
    @Deprecated
    public Selector createSelector(Locale var1, Map<String, Object> var2);
}

