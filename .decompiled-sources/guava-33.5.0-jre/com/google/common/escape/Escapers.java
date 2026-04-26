/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.escape;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import com.google.common.escape.ArrayBasedCharEscaper;
import com.google.common.escape.CharEscaper;
import com.google.common.escape.Escaper;
import com.google.common.escape.UnicodeEscaper;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public final class Escapers {
    private static final Escaper NULL_ESCAPER = new CharEscaper(){

        @Override
        public String escape(String string) {
            return Preconditions.checkNotNull(string);
        }

        @Override
        protected char @Nullable [] escape(char c) {
            return null;
        }
    };

    private Escapers() {
    }

    public static Escaper nullEscaper() {
        return NULL_ESCAPER;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static @Nullable String computeReplacement(CharEscaper escaper, char c) {
        return Escapers.stringOrNull(escaper.escape(c));
    }

    public static @Nullable String computeReplacement(UnicodeEscaper escaper, int cp) {
        return Escapers.stringOrNull(escaper.escape(cp));
    }

    private static @Nullable String stringOrNull(char @Nullable [] in) {
        return in == null ? null : new String(in);
    }

    public static final class Builder {
        private final Map<Character, String> replacementMap = new HashMap<Character, String>();
        private char safeMin = '\u0000';
        private char safeMax = (char)65535;
        private @Nullable String unsafeReplacement = null;

        private Builder() {
        }

        @CanIgnoreReturnValue
        public Builder setSafeRange(char safeMin, char safeMax) {
            this.safeMin = safeMin;
            this.safeMax = safeMax;
            return this;
        }

        @CanIgnoreReturnValue
        public Builder setUnsafeReplacement(@Nullable String unsafeReplacement) {
            this.unsafeReplacement = unsafeReplacement;
            return this;
        }

        @CanIgnoreReturnValue
        public Builder addEscape(char c, String replacement) {
            Preconditions.checkNotNull(replacement);
            this.replacementMap.put(Character.valueOf(c), replacement);
            return this;
        }

        public Escaper build() {
            return new ArrayBasedCharEscaper(this.replacementMap, this.safeMin, this.safeMax){
                private final char @Nullable [] replacementChars;
                {
                    super(replacementMap, safeMin, safeMax);
                    this.replacementChars = unsafeReplacement != null ? unsafeReplacement.toCharArray() : null;
                }

                @Override
                protected char @Nullable [] escapeUnsafe(char c) {
                    return this.replacementChars;
                }
            };
        }
    }
}

