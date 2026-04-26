/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.message2;

import com.ibm.icu.message2.FormattedPlaceholder;
import java.util.Map;

@Deprecated
public interface Formatter {
    @Deprecated
    public String formatToString(Object var1, Map<String, Object> var2);

    @Deprecated
    public FormattedPlaceholder format(Object var1, Map<String, Object> var2);
}

