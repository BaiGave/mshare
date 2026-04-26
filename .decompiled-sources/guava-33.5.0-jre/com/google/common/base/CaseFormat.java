/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Ascii;
import com.google.common.base.CharMatcher;
import com.google.common.base.Converter;
import com.google.common.base.Preconditions;
import java.io.Serializable;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public enum CaseFormat {
    LOWER_HYPHEN(CharMatcher.is('-'), "-"){

        @Override
        String normalizeWord(String word) {
            return Ascii.toLowerCase(word);
        }

        @Override
        String convert(CaseFormat format, String s) {
            if (format == LOWER_UNDERSCORE) {
                return s.replace('-', '_');
            }
            if (format == UPPER_UNDERSCORE) {
                return Ascii.toUpperCase(s.replace('-', '_'));
            }
            return super.convert(format, s);
        }
    }
    ,
    LOWER_UNDERSCORE(CharMatcher.is('_'), "_"){

        @Override
        String normalizeWord(String word) {
            return Ascii.toLowerCase(word);
        }

        @Override
        String convert(CaseFormat format, String s) {
            if (format == LOWER_HYPHEN) {
                return s.replace('_', '-');
            }
            if (format == UPPER_UNDERSCORE) {
                return Ascii.toUpperCase(s);
            }
            return super.convert(format, s);
        }
    }
    ,
    LOWER_CAMEL(CharMatcher.inRange('A', 'Z'), ""){

        @Override
        String normalizeWord(String word) {
            return CaseFormat.firstCharOnlyToUpper(word);
        }

        @Override
        String normalizeFirstWord(String word) {
            return Ascii.toLowerCase(word);
        }
    }
    ,
    UPPER_CAMEL(CharMatcher.inRange('A', 'Z'), ""){

        @Override
        String normalizeWord(String word) {
            return CaseFormat.firstCharOnlyToUpper(word);
        }
    }
    ,
    UPPER_UNDERSCORE(CharMatcher.is('_'), "_"){

        @Override
        String normalizeWord(String word) {
            return Ascii.toUpperCase(word);
        }

        @Override
        String convert(CaseFormat format, String s) {
            if (format == LOWER_HYPHEN) {
                return Ascii.toLowerCase(s.replace('_', '-'));
            }
            if (format == LOWER_UNDERSCORE) {
                return Ascii.toLowerCase(s);
            }
            return super.convert(format, s);
        }
    };

    private final CharMatcher wordBoundary;
    private final String wordSeparator;

    private CaseFormat(CharMatcher wordBoundary, String wordSeparator) {
        this.wordBoundary = wordBoundary;
        this.wordSeparator = wordSeparator;
    }

    public final String to(CaseFormat format, String str) {
        Preconditions.checkNotNull(format);
        Preconditions.checkNotNull(str);
        return format == this ? str : this.convert(format, str);
    }

    String convert(CaseFormat format, String s) {
        StringBuilder out = null;
        int i = 0;
        int j = -1;
        while (true) {
            ++j;
            if ((j = this.wordBoundary.indexIn(s, j)) == -1) break;
            if (i == 0) {
                out = new StringBuilder(s.length() + 4 * format.wordSeparator.length());
                out.append(format.normalizeFirstWord(s.substring(i, j)));
            } else {
                ((StringBuilder)Objects.requireNonNull(out)).append(format.normalizeWord(s.substring(i, j)));
            }
            out.append(format.wordSeparator);
            i = j + this.wordSeparator.length();
        }
        return i == 0 ? format.normalizeFirstWord(s) : ((StringBuilder)Objects.requireNonNull(out)).append(format.normalizeWord(s.substring(i))).toString();
    }

    public Converter<String, String> converterTo(CaseFormat targetFormat) {
        return new StringConverter(this, targetFormat);
    }

    abstract String normalizeWord(String var1);

    String normalizeFirstWord(String word) {
        return this.normalizeWord(word);
    }

    private static String firstCharOnlyToUpper(String word) {
        return word.isEmpty() ? word : Ascii.toUpperCase(word.charAt(0)) + Ascii.toLowerCase(word.substring(1));
    }

    private static final class StringConverter
    extends Converter<String, String>
    implements Serializable {
        private final CaseFormat sourceFormat;
        private final CaseFormat targetFormat;
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        StringConverter(CaseFormat sourceFormat, CaseFormat targetFormat) {
            this.sourceFormat = Preconditions.checkNotNull(sourceFormat);
            this.targetFormat = Preconditions.checkNotNull(targetFormat);
        }

        @Override
        protected String doForward(String s) {
            return this.sourceFormat.to(this.targetFormat, s);
        }

        @Override
        protected String doBackward(String s) {
            return this.targetFormat.to(this.sourceFormat, s);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof StringConverter) {
                StringConverter that = (StringConverter)obj;
                return this.sourceFormat.equals((Object)that.sourceFormat) && this.targetFormat.equals((Object)that.targetFormat);
            }
            return false;
        }

        public int hashCode() {
            return this.sourceFormat.hashCode() ^ this.targetFormat.hashCode();
        }

        public String toString() {
            return (Object)((Object)this.sourceFormat) + ".converterTo(" + (Object)((Object)this.targetFormat) + ")";
        }
    }
}

