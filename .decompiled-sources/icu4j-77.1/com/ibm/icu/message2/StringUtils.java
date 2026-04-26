/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.message2;

import com.ibm.icu.text.Normalizer2;

class StringUtils {
    private static final Normalizer2 NFC_NORMALIZER = Normalizer2.getNFCInstance();

    StringUtils() {
    }

    static boolean isContentChar(int cp) {
        return cp >= 1 && cp <= 8 || cp >= 11 && cp <= 12 || cp >= 14 && cp <= 31 || cp >= 33 && cp <= 45 || cp >= 47 && cp <= 63 || cp >= 65 && cp <= 91 || cp >= 93 && cp <= 122 || cp >= 126 && cp <= 12287 || cp >= 12289 && cp <= 0x10FFFF;
    }

    static boolean isTextChar(int cp) {
        return StringUtils.isContentChar(cp) || StringUtils.isWhitespace(cp) || cp == 46 || cp == 64 || cp == 124;
    }

    static boolean isBackslash(int cp) {
        return cp == 92;
    }

    static boolean isWhitespace(int cp) {
        return cp == 32 || cp == 9 || cp == 13 || cp == 10 || cp == 12288;
    }

    static boolean isBidi(int cp) {
        return cp == 1564 || cp == 8206 || cp == 8207 || cp >= 8294 && cp <= 8297;
    }

    static boolean isNameStart(int cp) {
        return StringUtils.isAlpha(cp) || cp == 95 || cp >= 192 && cp <= 214 || cp >= 216 && cp <= 246 || cp >= 248 && cp <= 767 || cp >= 880 && cp <= 893 || cp >= 895 && cp <= 1563 || cp >= 1565 && cp <= 8191 || cp >= 8204 && cp <= 8205 || cp >= 8304 && cp <= 8591 || cp >= 11264 && cp <= 12271 || cp >= 12289 && cp <= 55295 || cp >= 63744 && cp <= 64975 || cp >= 65008 && cp <= 65532 || cp >= 65536 && cp <= 983039;
    }

    static boolean isNameChar(int cp) {
        return StringUtils.isNameStart(cp) || StringUtils.isDigit(cp) || cp == 45 || cp == 46 || cp == 183 || cp >= 768 && cp <= 879 || cp >= 8255 && cp <= 8256;
    }

    static boolean isQuotedChar(int cp) {
        return StringUtils.isContentChar(cp) || StringUtils.isWhitespace(cp) || cp == 46 || cp == 64 || cp == 123 || cp == 125;
    }

    static boolean isSimpleStartChar(int cp) {
        return StringUtils.isContentChar(cp) || StringUtils.isWhitespace(cp) || cp == 64 || cp == 124;
    }

    static boolean isAlpha(int cp) {
        return cp >= 97 && cp <= 122 || cp >= 65 && cp <= 90;
    }

    static boolean isDigit(int cp) {
        return cp >= 48 && cp <= 57;
    }

    static boolean isFunctionSigil(int cp) {
        return cp == 58;
    }

    static String toNfc(CharSequence value) {
        return value == null ? null : NFC_NORMALIZER.normalize(value);
    }
}

