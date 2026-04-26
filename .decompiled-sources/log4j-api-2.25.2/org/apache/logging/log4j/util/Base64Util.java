/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.nio.charset.Charset;
import java.util.Base64;

/*
 * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
 */
public final class Base64Util {
    private static final Base64.Encoder ENCODER = Base64.getEncoder();

    private Base64Util() {
    }

    @Deprecated
    public static String encode(String str) {
        return str != null ? ENCODER.encodeToString(str.getBytes(Charset.defaultCharset())) : null;
    }
}

