/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.message2;

import com.ibm.icu.message2.Formatter;
import java.util.Locale;
import java.util.Map;

@Deprecated
public interface FormatterFactory {
    @Deprecated
    public Formatter createFormatter(Locale var1, Map<String, Object> var2);
}

