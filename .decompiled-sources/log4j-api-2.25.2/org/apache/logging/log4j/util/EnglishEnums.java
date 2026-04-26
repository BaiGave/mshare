/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import org.apache.logging.log4j.util.InternalApi;
import org.apache.logging.log4j.util.Strings;

@InternalApi
public final class EnglishEnums {
    private EnglishEnums() {
    }

    public static <T extends Enum<T>> T valueOf(Class<T> enumType, String name) {
        return EnglishEnums.valueOf(enumType, name, null);
    }

    public static <T extends Enum<T>> T valueOf(Class<T> enumType, String name, T defaultValue) {
        return name == null ? defaultValue : Enum.valueOf(enumType, Strings.toRootUpperCase(name));
    }
}

