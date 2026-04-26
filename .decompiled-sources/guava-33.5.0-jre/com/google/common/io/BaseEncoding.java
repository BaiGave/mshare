/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.io;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Ascii;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import com.google.common.math.IntMath;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public abstract class BaseEncoding {
    private static final BaseEncoding BASE64 = new Base64Encoding("base64()", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", Character.valueOf('='));
    private static final BaseEncoding BASE64_URL = new Base64Encoding("base64Url()", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_", Character.valueOf('='));
    private static final BaseEncoding BASE32 = new StandardBaseEncoding("base32()", "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567", Character.valueOf('='));
    private static final BaseEncoding BASE32_HEX = new StandardBaseEncoding("base32Hex()", "0123456789ABCDEFGHIJKLMNOPQRSTUV", Character.valueOf('='));
    private static final BaseEncoding BASE16 = new Base16Encoding("base16()", "0123456789ABCDEF");

    BaseEncoding() {
    }

    public String encode(byte[] bytes) {
        return this.encode(bytes, 0, bytes.length);
    }

    public final String encode(byte[] bytes, int off, int len) {
        Preconditions.checkPositionIndexes(off, off + len, bytes.length);
        StringBuilder result = new StringBuilder(this.maxEncodedSize(len));
        try {
            this.encodeTo(result, bytes, off, len);
        }
        catch (IOException impossible) {
            throw new AssertionError((Object)impossible);
        }
        return result.toString();
    }

    @J2ktIncompatible
    @GwtIncompatible
    public abstract OutputStream encodingStream(Writer var1);

    @J2ktIncompatible
    @GwtIncompatible
    public final ByteSink encodingSink(final CharSink encodedSink) {
        Preconditions.checkNotNull(encodedSink);
        return new ByteSink(this){
            final /* synthetic */ BaseEncoding this$0;
            {
                this.this$0 = this$0;
            }

            @Override
            public OutputStream openStream() throws IOException {
                return this.this$0.encodingStream(encodedSink.openStream());
            }
        };
    }

    private static byte[] extract(byte[] result, int length) {
        if (length == result.length) {
            return result;
        }
        byte[] trunc = new byte[length];
        System.arraycopy(result, 0, trunc, 0, length);
        return trunc;
    }

    public abstract boolean canDecode(CharSequence var1);

    public final byte[] decode(CharSequence chars) {
        try {
            return this.decodeChecked(chars);
        }
        catch (DecodingException badInput) {
            throw new IllegalArgumentException(badInput);
        }
    }

    final byte[] decodeChecked(CharSequence chars) throws DecodingException {
        chars = this.trimTrailingPadding(chars);
        byte[] tmp = new byte[this.maxDecodedSize(chars.length())];
        int len = this.decodeTo(tmp, chars);
        return BaseEncoding.extract(tmp, len);
    }

    @J2ktIncompatible
    @GwtIncompatible
    public abstract InputStream decodingStream(Reader var1);

    @J2ktIncompatible
    @GwtIncompatible
    public final ByteSource decodingSource(final CharSource encodedSource) {
        Preconditions.checkNotNull(encodedSource);
        return new ByteSource(this){
            final /* synthetic */ BaseEncoding this$0;
            {
                this.this$0 = this$0;
            }

            @Override
            public InputStream openStream() throws IOException {
                return this.this$0.decodingStream(encodedSource.openStream());
            }
        };
    }

    abstract int maxEncodedSize(int var1);

    abstract void encodeTo(Appendable var1, byte[] var2, int var3, int var4) throws IOException;

    abstract int maxDecodedSize(int var1);

    abstract int decodeTo(byte[] var1, CharSequence var2) throws DecodingException;

    CharSequence trimTrailingPadding(CharSequence chars) {
        return Preconditions.checkNotNull(chars);
    }

    public abstract BaseEncoding omitPadding();

    public abstract BaseEncoding withPadChar(char var1);

    public abstract BaseEncoding withSeparator(String var1, int var2);

    public abstract BaseEncoding upperCase();

    public abstract BaseEncoding lowerCase();

    public abstract BaseEncoding ignoreCase();

    public static BaseEncoding base64() {
        return BASE64;
    }

    public static BaseEncoding base64Url() {
        return BASE64_URL;
    }

    public static BaseEncoding base32() {
        return BASE32;
    }

    public static BaseEncoding base32Hex() {
        return BASE32_HEX;
    }

    public static BaseEncoding base16() {
        return BASE16;
    }

    @J2ktIncompatible
    @GwtIncompatible
    static Reader ignoringReader(final Reader delegate, final String toIgnore) {
        Preconditions.checkNotNull(delegate);
        Preconditions.checkNotNull(toIgnore);
        return new Reader(){

            @Override
            public int read() throws IOException {
                int readChar;
                while ((readChar = delegate.read()) != -1 && toIgnore.indexOf((char)readChar) >= 0) {
                }
                return readChar;
            }

            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new UnsupportedOperationException();
            }

            @Override
            public void close() throws IOException {
                delegate.close();
            }
        };
    }

    static Appendable separatingAppendable(final Appendable delegate, final String separator, final int afterEveryChars) {
        Preconditions.checkNotNull(delegate);
        Preconditions.checkNotNull(separator);
        Preconditions.checkArgument(afterEveryChars > 0);
        return new Appendable(){
            int charsUntilSeparator;
            {
                this.charsUntilSeparator = afterEveryChars;
            }

            @Override
            public Appendable append(char c) throws IOException {
                if (this.charsUntilSeparator == 0) {
                    delegate.append(separator);
                    this.charsUntilSeparator = afterEveryChars;
                }
                delegate.append(c);
                --this.charsUntilSeparator;
                return this;
            }

            @Override
            public Appendable append(@Nullable CharSequence chars, int off, int len) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Appendable append(@Nullable CharSequence chars) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @J2ktIncompatible
    @GwtIncompatible
    static Writer separatingWriter(final Writer delegate, String separator, int afterEveryChars) {
        final Appendable separatingAppendable = BaseEncoding.separatingAppendable(delegate, separator, afterEveryChars);
        return new Writer(){

            @Override
            public void write(int c) throws IOException {
                separatingAppendable.append((char)c);
            }

            @Override
            public void write(char[] chars, int off, int len) throws IOException {
                throw new UnsupportedOperationException();
            }

            @Override
            public void flush() throws IOException {
                delegate.flush();
            }

            @Override
            public void close() throws IOException {
                delegate.close();
            }
        };
    }

    public static final class DecodingException
    extends IOException {
        DecodingException(@Nullable String message) {
            super(message);
        }
    }

    private static final class Base64Encoding
    extends StandardBaseEncoding {
        Base64Encoding(String name, String alphabetChars, @Nullable Character paddingChar) {
            this(new Alphabet(name, alphabetChars.toCharArray()), paddingChar);
        }

        private Base64Encoding(Alphabet alphabet, @Nullable Character paddingChar) {
            super(alphabet, paddingChar);
            Preconditions.checkArgument(alphabet.chars.length == 64);
        }

        @Override
        void encodeTo(Appendable target, byte[] bytes, int off, int len) throws IOException {
            Preconditions.checkNotNull(target);
            Preconditions.checkPositionIndexes(off, off + len, bytes.length);
            int i = off;
            for (int remaining = len; remaining >= 3; remaining -= 3) {
                int chunk = (bytes[i++] & 0xFF) << 16 | (bytes[i++] & 0xFF) << 8 | bytes[i++] & 0xFF;
                target.append(this.alphabet.encode(chunk >>> 18));
                target.append(this.alphabet.encode(chunk >>> 12 & 0x3F));
                target.append(this.alphabet.encode(chunk >>> 6 & 0x3F));
                target.append(this.alphabet.encode(chunk & 0x3F));
            }
            if (i < off + len) {
                this.encodeChunkTo(target, bytes, i, off + len - i);
            }
        }

        @Override
        int decodeTo(byte[] target, CharSequence chars) throws DecodingException {
            Preconditions.checkNotNull(target);
            chars = this.trimTrailingPadding(chars);
            if (!this.alphabet.isValidPaddingStartPosition(chars.length())) {
                throw new DecodingException("Invalid input length " + chars.length());
            }
            int bytesWritten = 0;
            int i = 0;
            while (i < chars.length()) {
                int chunk = this.alphabet.decode(chars.charAt(i++)) << 18;
                target[bytesWritten++] = (byte)((chunk |= this.alphabet.decode(chars.charAt(i++)) << 12) >>> 16);
                if (i >= chars.length()) continue;
                target[bytesWritten++] = (byte)((chunk |= this.alphabet.decode(chars.charAt(i++)) << 6) >>> 8 & 0xFF);
                if (i >= chars.length()) continue;
                target[bytesWritten++] = (byte)((chunk |= this.alphabet.decode(chars.charAt(i++))) & 0xFF);
            }
            return bytesWritten;
        }

        @Override
        BaseEncoding newInstance(Alphabet alphabet, @Nullable Character paddingChar) {
            return new Base64Encoding(alphabet, paddingChar);
        }
    }

    private static class StandardBaseEncoding
    extends BaseEncoding {
        final Alphabet alphabet;
        final @Nullable Character paddingChar;
        @LazyInit
        private volatile @Nullable BaseEncoding upperCase;
        @LazyInit
        private volatile @Nullable BaseEncoding lowerCase;
        @LazyInit
        private volatile @Nullable BaseEncoding ignoreCase;

        StandardBaseEncoding(String name, String alphabetChars, @Nullable Character paddingChar) {
            this(new Alphabet(name, alphabetChars.toCharArray()), paddingChar);
        }

        StandardBaseEncoding(Alphabet alphabet, @Nullable Character paddingChar) {
            this.alphabet = Preconditions.checkNotNull(alphabet);
            Preconditions.checkArgument(paddingChar == null || !alphabet.matches(paddingChar.charValue()), "Padding character %s was already in alphabet", (Object)paddingChar);
            this.paddingChar = paddingChar;
        }

        @Override
        int maxEncodedSize(int bytes) {
            return this.alphabet.charsPerChunk * IntMath.divide(bytes, this.alphabet.bytesPerChunk, RoundingMode.CEILING);
        }

        @Override
        @J2ktIncompatible
        @GwtIncompatible
        public OutputStream encodingStream(final Writer out) {
            Preconditions.checkNotNull(out);
            return new OutputStream(this){
                int bitBuffer = 0;
                int bitBufferLength = 0;
                int writtenChars = 0;
                final /* synthetic */ StandardBaseEncoding this$0;
                {
                    this.this$0 = this$0;
                }

                @Override
                public void write(int b) throws IOException {
                    this.bitBuffer <<= 8;
                    this.bitBuffer |= b & 0xFF;
                    this.bitBufferLength += 8;
                    while (this.bitBufferLength >= this.this$0.alphabet.bitsPerChar) {
                        int charIndex = this.bitBuffer >> this.bitBufferLength - this.this$0.alphabet.bitsPerChar & this.this$0.alphabet.mask;
                        out.write(this.this$0.alphabet.encode(charIndex));
                        ++this.writtenChars;
                        this.bitBufferLength -= this.this$0.alphabet.bitsPerChar;
                    }
                }

                @Override
                public void flush() throws IOException {
                    out.flush();
                }

                @Override
                public void close() throws IOException {
                    if (this.bitBufferLength > 0) {
                        int charIndex = this.bitBuffer << this.this$0.alphabet.bitsPerChar - this.bitBufferLength & this.this$0.alphabet.mask;
                        out.write(this.this$0.alphabet.encode(charIndex));
                        ++this.writtenChars;
                        if (this.this$0.paddingChar != null) {
                            while (this.writtenChars % this.this$0.alphabet.charsPerChunk != 0) {
                                out.write(this.this$0.paddingChar.charValue());
                                ++this.writtenChars;
                            }
                        }
                    }
                    out.close();
                }
            };
        }

        @Override
        void encodeTo(Appendable target, byte[] bytes, int off, int len) throws IOException {
            Preconditions.checkNotNull(target);
            Preconditions.checkPositionIndexes(off, off + len, bytes.length);
            for (int i = 0; i < len; i += this.alphabet.bytesPerChunk) {
                this.encodeChunkTo(target, bytes, off + i, Math.min(this.alphabet.bytesPerChunk, len - i));
            }
        }

        void encodeChunkTo(Appendable target, byte[] bytes, int off, int len) throws IOException {
            int bitsProcessed;
            Preconditions.checkNotNull(target);
            Preconditions.checkPositionIndexes(off, off + len, bytes.length);
            Preconditions.checkArgument(len <= this.alphabet.bytesPerChunk);
            long bitBuffer = 0L;
            for (int i = 0; i < len; ++i) {
                bitBuffer |= (long)(bytes[off + i] & 0xFF);
                bitBuffer <<= 8;
            }
            int bitOffset = (len + 1) * 8 - this.alphabet.bitsPerChar;
            for (bitsProcessed = 0; bitsProcessed < len * 8; bitsProcessed += this.alphabet.bitsPerChar) {
                int charIndex = (int)(bitBuffer >>> bitOffset - bitsProcessed) & this.alphabet.mask;
                target.append(this.alphabet.encode(charIndex));
            }
            if (this.paddingChar != null) {
                while (bitsProcessed < this.alphabet.bytesPerChunk * 8) {
                    target.append(this.paddingChar.charValue());
                    bitsProcessed += this.alphabet.bitsPerChar;
                }
            }
        }

        @Override
        int maxDecodedSize(int chars) {
            return (int)(((long)this.alphabet.bitsPerChar * (long)chars + 7L) / 8L);
        }

        @Override
        CharSequence trimTrailingPadding(CharSequence chars) {
            int l;
            Preconditions.checkNotNull(chars);
            if (this.paddingChar == null) {
                return chars;
            }
            char padChar = this.paddingChar.charValue();
            for (l = chars.length() - 1; l >= 0 && chars.charAt(l) == padChar; --l) {
            }
            return chars.subSequence(0, l + 1);
        }

        @Override
        public boolean canDecode(CharSequence chars) {
            Preconditions.checkNotNull(chars);
            chars = this.trimTrailingPadding(chars);
            if (!this.alphabet.isValidPaddingStartPosition(chars.length())) {
                return false;
            }
            for (int i = 0; i < chars.length(); ++i) {
                if (this.alphabet.canDecode(chars.charAt(i))) continue;
                return false;
            }
            return true;
        }

        @Override
        int decodeTo(byte[] target, CharSequence chars) throws DecodingException {
            Preconditions.checkNotNull(target);
            chars = this.trimTrailingPadding(chars);
            if (!this.alphabet.isValidPaddingStartPosition(chars.length())) {
                throw new DecodingException("Invalid input length " + chars.length());
            }
            int bytesWritten = 0;
            for (int charIdx = 0; charIdx < chars.length(); charIdx += this.alphabet.charsPerChunk) {
                long chunk = 0L;
                int charsProcessed = 0;
                for (int i = 0; i < this.alphabet.charsPerChunk; ++i) {
                    chunk <<= this.alphabet.bitsPerChar;
                    if (charIdx + i >= chars.length()) continue;
                    chunk |= (long)this.alphabet.decode(chars.charAt(charIdx + charsProcessed++));
                }
                int minOffset = this.alphabet.bytesPerChunk * 8 - charsProcessed * this.alphabet.bitsPerChar;
                for (int offset = (this.alphabet.bytesPerChunk - 1) * 8; offset >= minOffset; offset -= 8) {
                    target[bytesWritten++] = (byte)(chunk >>> offset & 0xFFL);
                }
            }
            return bytesWritten;
        }

        @Override
        @J2ktIncompatible
        @GwtIncompatible
        public InputStream decodingStream(final Reader reader) {
            Preconditions.checkNotNull(reader);
            return new InputStream(this){
                int bitBuffer = 0;
                int bitBufferLength = 0;
                int readChars = 0;
                boolean hitPadding = false;
                final /* synthetic */ StandardBaseEncoding this$0;
                {
                    this.this$0 = this$0;
                }

                @Override
                public int read() throws IOException {
                    while (true) {
                        int readChar;
                        if ((readChar = reader.read()) == -1) {
                            if (!this.hitPadding && !this.this$0.alphabet.isValidPaddingStartPosition(this.readChars)) {
                                throw new DecodingException("Invalid input length " + this.readChars);
                            }
                            return -1;
                        }
                        ++this.readChars;
                        char ch = (char)readChar;
                        if (this.this$0.paddingChar != null && this.this$0.paddingChar.charValue() == ch) {
                            if (!(this.hitPadding || this.readChars != 1 && this.this$0.alphabet.isValidPaddingStartPosition(this.readChars - 1))) {
                                throw new DecodingException("Padding cannot start at index " + this.readChars);
                            }
                            this.hitPadding = true;
                            continue;
                        }
                        if (this.hitPadding) {
                            throw new DecodingException("Expected padding character but found '" + ch + "' at index " + this.readChars);
                        }
                        this.bitBuffer <<= this.this$0.alphabet.bitsPerChar;
                        this.bitBuffer |= this.this$0.alphabet.decode(ch);
                        this.bitBufferLength += this.this$0.alphabet.bitsPerChar;
                        if (this.bitBufferLength >= 8) break;
                    }
                    this.bitBufferLength -= 8;
                    return this.bitBuffer >> this.bitBufferLength & 0xFF;
                }

                @Override
                public int read(byte[] buf, int off, int len) throws IOException {
                    int i;
                    Preconditions.checkPositionIndexes(off, off + len, buf.length);
                    for (i = off; i < off + len; ++i) {
                        int b = this.read();
                        if (b == -1) {
                            int read = i - off;
                            return read == 0 ? -1 : read;
                        }
                        buf[i] = (byte)b;
                    }
                    return i - off;
                }

                @Override
                public void close() throws IOException {
                    reader.close();
                }
            };
        }

        @Override
        public BaseEncoding omitPadding() {
            return this.paddingChar == null ? this : this.newInstance(this.alphabet, null);
        }

        @Override
        public BaseEncoding withPadChar(char padChar) {
            if (8 % this.alphabet.bitsPerChar == 0 || this.paddingChar != null && this.paddingChar.charValue() == padChar) {
                return this;
            }
            return this.newInstance(this.alphabet, Character.valueOf(padChar));
        }

        @Override
        public BaseEncoding withSeparator(String separator, int afterEveryChars) {
            for (int i = 0; i < separator.length(); ++i) {
                Preconditions.checkArgument(!this.alphabet.matches(separator.charAt(i)), "Separator (%s) cannot contain alphabet characters", (Object)separator);
            }
            if (this.paddingChar != null) {
                Preconditions.checkArgument(separator.indexOf(this.paddingChar.charValue()) < 0, "Separator (%s) cannot contain padding character", (Object)separator);
            }
            return new SeparatedBaseEncoding(this, separator, afterEveryChars);
        }

        @Override
        public BaseEncoding upperCase() {
            BaseEncoding result = this.upperCase;
            if (result == null) {
                Alphabet upper = this.alphabet.upperCase();
                this.upperCase = upper == this.alphabet ? this : this.newInstance(upper, this.paddingChar);
                result = this.upperCase;
            }
            return result;
        }

        @Override
        public BaseEncoding lowerCase() {
            BaseEncoding result = this.lowerCase;
            if (result == null) {
                Alphabet lower = this.alphabet.lowerCase();
                this.lowerCase = lower == this.alphabet ? this : this.newInstance(lower, this.paddingChar);
                result = this.lowerCase;
            }
            return result;
        }

        @Override
        public BaseEncoding ignoreCase() {
            BaseEncoding result = this.ignoreCase;
            if (result == null) {
                Alphabet ignore = this.alphabet.ignoreCase();
                this.ignoreCase = ignore == this.alphabet ? this : this.newInstance(ignore, this.paddingChar);
                result = this.ignoreCase;
            }
            return result;
        }

        BaseEncoding newInstance(Alphabet alphabet, @Nullable Character paddingChar) {
            return new StandardBaseEncoding(alphabet, paddingChar);
        }

        public String toString() {
            StringBuilder builder = new StringBuilder("BaseEncoding.");
            builder.append(this.alphabet);
            if (8 % this.alphabet.bitsPerChar != 0) {
                if (this.paddingChar == null) {
                    builder.append(".omitPadding()");
                } else {
                    builder.append(".withPadChar('").append(this.paddingChar).append("')");
                }
            }
            return builder.toString();
        }

        public boolean equals(@Nullable Object other) {
            if (other instanceof StandardBaseEncoding) {
                StandardBaseEncoding that = (StandardBaseEncoding)other;
                return this.alphabet.equals(that.alphabet) && Objects.equals(this.paddingChar, that.paddingChar);
            }
            return false;
        }

        public int hashCode() {
            return this.alphabet.hashCode() ^ Objects.hashCode(this.paddingChar);
        }
    }

    private static final class Base16Encoding
    extends StandardBaseEncoding {
        final char[] encoding = new char[512];

        Base16Encoding(String name, String alphabetChars) {
            this(new Alphabet(name, alphabetChars.toCharArray()));
        }

        private Base16Encoding(Alphabet alphabet) {
            super(alphabet, null);
            Preconditions.checkArgument(alphabet.chars.length == 16);
            for (int i = 0; i < 256; ++i) {
                this.encoding[i] = alphabet.encode(i >>> 4);
                this.encoding[i | 0x100] = alphabet.encode(i & 0xF);
            }
        }

        @Override
        void encodeTo(Appendable target, byte[] bytes, int off, int len) throws IOException {
            Preconditions.checkNotNull(target);
            Preconditions.checkPositionIndexes(off, off + len, bytes.length);
            for (int i = 0; i < len; ++i) {
                int b = bytes[off + i] & 0xFF;
                target.append(this.encoding[b]);
                target.append(this.encoding[b | 0x100]);
            }
        }

        @Override
        int decodeTo(byte[] target, CharSequence chars) throws DecodingException {
            Preconditions.checkNotNull(target);
            if (chars.length() % 2 == 1) {
                throw new DecodingException("Invalid input length " + chars.length());
            }
            int bytesWritten = 0;
            for (int i = 0; i < chars.length(); i += 2) {
                int decoded = this.alphabet.decode(chars.charAt(i)) << 4 | this.alphabet.decode(chars.charAt(i + 1));
                target[bytesWritten++] = (byte)decoded;
            }
            return bytesWritten;
        }

        @Override
        BaseEncoding newInstance(Alphabet alphabet, @Nullable Character paddingChar) {
            return new Base16Encoding(alphabet);
        }
    }

    static final class SeparatedBaseEncoding
    extends BaseEncoding {
        private final BaseEncoding delegate;
        private final String separator;
        private final int afterEveryChars;

        SeparatedBaseEncoding(BaseEncoding delegate, String separator, int afterEveryChars) {
            this.delegate = Preconditions.checkNotNull(delegate);
            this.separator = Preconditions.checkNotNull(separator);
            this.afterEveryChars = afterEveryChars;
            Preconditions.checkArgument(afterEveryChars > 0, "Cannot add a separator after every %s chars", afterEveryChars);
        }

        @Override
        CharSequence trimTrailingPadding(CharSequence chars) {
            return this.delegate.trimTrailingPadding(chars);
        }

        @Override
        int maxEncodedSize(int bytes) {
            int unseparatedSize = this.delegate.maxEncodedSize(bytes);
            return unseparatedSize + this.separator.length() * IntMath.divide(Math.max(0, unseparatedSize - 1), this.afterEveryChars, RoundingMode.FLOOR);
        }

        @Override
        @J2ktIncompatible
        @GwtIncompatible
        public OutputStream encodingStream(Writer output) {
            return this.delegate.encodingStream(SeparatedBaseEncoding.separatingWriter(output, this.separator, this.afterEveryChars));
        }

        @Override
        void encodeTo(Appendable target, byte[] bytes, int off, int len) throws IOException {
            this.delegate.encodeTo(SeparatedBaseEncoding.separatingAppendable(target, this.separator, this.afterEveryChars), bytes, off, len);
        }

        @Override
        int maxDecodedSize(int chars) {
            return this.delegate.maxDecodedSize(chars);
        }

        @Override
        public boolean canDecode(CharSequence chars) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < chars.length(); ++i) {
                char c = chars.charAt(i);
                if (this.separator.indexOf(c) >= 0) continue;
                builder.append(c);
            }
            return this.delegate.canDecode(builder);
        }

        @Override
        int decodeTo(byte[] target, CharSequence chars) throws DecodingException {
            StringBuilder stripped = new StringBuilder(chars.length());
            for (int i = 0; i < chars.length(); ++i) {
                char c = chars.charAt(i);
                if (this.separator.indexOf(c) >= 0) continue;
                stripped.append(c);
            }
            return this.delegate.decodeTo(target, stripped);
        }

        @Override
        @J2ktIncompatible
        @GwtIncompatible
        public InputStream decodingStream(Reader reader) {
            return this.delegate.decodingStream(SeparatedBaseEncoding.ignoringReader(reader, this.separator));
        }

        @Override
        public BaseEncoding omitPadding() {
            return this.delegate.omitPadding().withSeparator(this.separator, this.afterEveryChars);
        }

        @Override
        public BaseEncoding withPadChar(char padChar) {
            return this.delegate.withPadChar(padChar).withSeparator(this.separator, this.afterEveryChars);
        }

        @Override
        public BaseEncoding withSeparator(String separator, int afterEveryChars) {
            throw new UnsupportedOperationException("Already have a separator");
        }

        @Override
        public BaseEncoding upperCase() {
            return this.delegate.upperCase().withSeparator(this.separator, this.afterEveryChars);
        }

        @Override
        public BaseEncoding lowerCase() {
            return this.delegate.lowerCase().withSeparator(this.separator, this.afterEveryChars);
        }

        @Override
        public BaseEncoding ignoreCase() {
            return this.delegate.ignoreCase().withSeparator(this.separator, this.afterEveryChars);
        }

        public String toString() {
            return this.delegate + ".withSeparator(\"" + this.separator + "\", " + this.afterEveryChars + ")";
        }
    }

    static final class Alphabet {
        private final String name;
        private final char[] chars;
        final int mask;
        final int bitsPerChar;
        final int charsPerChunk;
        final int bytesPerChunk;
        private final byte[] decodabet;
        private final boolean[] validPadding;
        private final boolean ignoreCase;

        Alphabet(String name, char[] chars) {
            this(name, chars, Alphabet.decodabetFor(chars), false);
        }

        private Alphabet(String name, char[] chars, byte[] decodabet, boolean ignoreCase) {
            this.name = Preconditions.checkNotNull(name);
            this.chars = Preconditions.checkNotNull(chars);
            try {
                this.bitsPerChar = IntMath.log2(chars.length, RoundingMode.UNNECESSARY);
            }
            catch (ArithmeticException e) {
                throw new IllegalArgumentException("Illegal alphabet length " + chars.length, e);
            }
            int zeroesInBitsPerChar = Integer.numberOfTrailingZeros(this.bitsPerChar);
            this.charsPerChunk = 1 << 3 - zeroesInBitsPerChar;
            this.bytesPerChunk = this.bitsPerChar >> zeroesInBitsPerChar;
            this.mask = chars.length - 1;
            this.decodabet = decodabet;
            boolean[] validPadding = new boolean[this.charsPerChunk];
            for (int i = 0; i < this.bytesPerChunk; ++i) {
                validPadding[IntMath.divide((int)(i * 8), (int)this.bitsPerChar, (RoundingMode)RoundingMode.CEILING)] = true;
            }
            this.validPadding = validPadding;
            this.ignoreCase = ignoreCase;
        }

        private static byte[] decodabetFor(char[] chars) {
            byte[] decodabet = new byte[128];
            Arrays.fill(decodabet, (byte)-1);
            for (int i = 0; i < chars.length; ++i) {
                char c = chars[i];
                Preconditions.checkArgument(c < decodabet.length, "Non-ASCII character: %s", c);
                Preconditions.checkArgument(decodabet[c] == -1, "Duplicate character: %s", c);
                decodabet[c] = (byte)i;
            }
            return decodabet;
        }

        Alphabet ignoreCase() {
            if (this.ignoreCase) {
                return this;
            }
            byte[] newDecodabet = Arrays.copyOf(this.decodabet, this.decodabet.length);
            for (int upper = 65; upper <= 90; ++upper) {
                int lower = upper | 0x20;
                byte decodeUpper = this.decodabet[upper];
                byte decodeLower = this.decodabet[lower];
                if (decodeUpper == -1) {
                    newDecodabet[upper] = decodeLower;
                    continue;
                }
                Preconditions.checkState(decodeLower == -1, "Can't ignoreCase() since '%s' and '%s' encode different values", (char)upper, (char)lower);
                newDecodabet[lower] = decodeUpper;
            }
            return new Alphabet(this.name + ".ignoreCase()", this.chars, newDecodabet, true);
        }

        char encode(int bits) {
            return this.chars[bits];
        }

        boolean isValidPaddingStartPosition(int index) {
            return this.validPadding[index % this.charsPerChunk];
        }

        boolean canDecode(char ch) {
            return ch <= '\u007f' && this.decodabet[ch] != -1;
        }

        int decode(char ch) throws DecodingException {
            if (ch > '\u007f') {
                throw new DecodingException("Unrecognized character: 0x" + Integer.toHexString(ch));
            }
            byte result = this.decodabet[ch];
            if (result == -1) {
                if (ch <= ' ' || ch == '\u007f') {
                    throw new DecodingException("Unrecognized character: 0x" + Integer.toHexString(ch));
                }
                throw new DecodingException("Unrecognized character: " + ch);
            }
            return result;
        }

        private boolean hasLowerCase() {
            for (char c : this.chars) {
                if (!Ascii.isLowerCase(c)) continue;
                return true;
            }
            return false;
        }

        private boolean hasUpperCase() {
            for (char c : this.chars) {
                if (!Ascii.isUpperCase(c)) continue;
                return true;
            }
            return false;
        }

        Alphabet upperCase() {
            if (!this.hasLowerCase()) {
                return this;
            }
            Preconditions.checkState(!this.hasUpperCase(), "Cannot call upperCase() on a mixed-case alphabet");
            char[] upperCased = new char[this.chars.length];
            for (int i = 0; i < this.chars.length; ++i) {
                upperCased[i] = Ascii.toUpperCase(this.chars[i]);
            }
            Alphabet upperCase = new Alphabet(this.name + ".upperCase()", upperCased);
            return this.ignoreCase ? upperCase.ignoreCase() : upperCase;
        }

        Alphabet lowerCase() {
            if (!this.hasUpperCase()) {
                return this;
            }
            Preconditions.checkState(!this.hasLowerCase(), "Cannot call lowerCase() on a mixed-case alphabet");
            char[] lowerCased = new char[this.chars.length];
            for (int i = 0; i < this.chars.length; ++i) {
                lowerCased[i] = Ascii.toLowerCase(this.chars[i]);
            }
            Alphabet lowerCase = new Alphabet(this.name + ".lowerCase()", lowerCased);
            return this.ignoreCase ? lowerCase.ignoreCase() : lowerCase;
        }

        public boolean matches(char c) {
            return c < this.decodabet.length && this.decodabet[c] != -1;
        }

        public String toString() {
            return this.name;
        }

        public boolean equals(@Nullable Object other) {
            if (other instanceof Alphabet) {
                Alphabet that = (Alphabet)other;
                return this.ignoreCase == that.ignoreCase && Arrays.equals(this.chars, that.chars);
            }
            return false;
        }

        public int hashCode() {
            return Arrays.hashCode(this.chars) + (this.ignoreCase ? 1231 : 1237);
        }
    }
}

