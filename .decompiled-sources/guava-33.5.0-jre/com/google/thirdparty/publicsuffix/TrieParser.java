/*
 * Decompiled with CFR 0.152.
 */
package com.google.thirdparty.publicsuffix;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.thirdparty.publicsuffix.PublicSuffixType;
import java.util.ArrayDeque;
import java.util.Deque;

@GwtCompatible
final class TrieParser {
    private static final Joiner DIRECT_JOINER = Joiner.on("");

    static ImmutableMap<String, PublicSuffixType> parseTrie(CharSequence ... encodedChunks) {
        String encoded = DIRECT_JOINER.join(encodedChunks);
        return TrieParser.parseFullString(encoded);
    }

    @VisibleForTesting
    static ImmutableMap<String, PublicSuffixType> parseFullString(String encoded) {
        ImmutableMap.Builder<String, PublicSuffixType> builder = ImmutableMap.builder();
        int encodedLen = encoded.length();
        for (int idx = 0; idx < encodedLen; idx += TrieParser.doParseTrieToBuilder(new ArrayDeque<CharSequence>(), encoded, idx, builder)) {
        }
        return builder.buildOrThrow();
    }

    private static int doParseTrieToBuilder(Deque<CharSequence> stack, CharSequence encoded, int start, ImmutableMap.Builder<String, PublicSuffixType> builder) {
        String domain;
        int idx;
        int encodedLen = encoded.length();
        char c = '\u0000';
        for (idx = start; idx < encodedLen && (c = encoded.charAt(idx)) != '&' && c != '?' && c != '!' && c != ':' && c != ','; ++idx) {
        }
        stack.push(TrieParser.reverse(encoded.subSequence(start, idx)));
        if ((c == '!' || c == '?' || c == ':' || c == ',') && (domain = DIRECT_JOINER.join(stack)).length() > 0) {
            builder.put(domain, PublicSuffixType.fromCode(c));
        }
        ++idx;
        if (c != '?' && c != ',') {
            while (idx < encodedLen) {
                if (encoded.charAt(idx += TrieParser.doParseTrieToBuilder(stack, encoded, idx, builder)) != '?' && encoded.charAt(idx) != ',') continue;
                ++idx;
                break;
            }
        }
        stack.pop();
        return idx - start;
    }

    private static CharSequence reverse(CharSequence s) {
        return new StringBuilder(s).reverse();
    }

    private TrieParser() {
    }
}

