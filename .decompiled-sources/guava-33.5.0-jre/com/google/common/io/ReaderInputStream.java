/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.io;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.io.Java8Compatibility;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;

@J2ktIncompatible
@GwtIncompatible
final class ReaderInputStream
extends InputStream {
    private final Reader reader;
    private final CharsetEncoder encoder;
    private final byte[] singleByte = new byte[1];
    private CharBuffer charBuffer;
    private ByteBuffer byteBuffer;
    private boolean endOfInput;
    private boolean draining;
    private boolean doneFlushing;

    ReaderInputStream(Reader reader, Charset charset, int bufferSize) {
        this(reader, charset.newEncoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE), bufferSize);
    }

    ReaderInputStream(Reader reader, CharsetEncoder encoder, int bufferSize) {
        this.reader = Preconditions.checkNotNull(reader);
        this.encoder = Preconditions.checkNotNull(encoder);
        Preconditions.checkArgument(bufferSize > 0, "bufferSize must be positive: %s", bufferSize);
        encoder.reset();
        this.charBuffer = CharBuffer.allocate(bufferSize);
        Java8Compatibility.flip(this.charBuffer);
        this.byteBuffer = ByteBuffer.allocate(bufferSize);
    }

    @Override
    public void close() throws IOException {
        this.reader.close();
    }

    @Override
    public int read() throws IOException {
        return this.read(this.singleByte) == 1 ? Byte.toUnsignedInt(this.singleByte[0]) : -1;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        CoderResult result;
        Preconditions.checkPositionIndexes(off, off + len, b.length);
        if (len == 0) {
            return 0;
        }
        int totalBytesRead = 0;
        boolean doneEncoding = this.endOfInput;
        block0: while (true) {
            if (this.draining) {
                if ((totalBytesRead += this.drain(b, off + totalBytesRead, len - totalBytesRead)) == len || this.doneFlushing) {
                    return totalBytesRead > 0 ? totalBytesRead : -1;
                }
                this.draining = false;
                Java8Compatibility.clear(this.byteBuffer);
            }
            while (true) {
                if ((result = this.doneFlushing ? CoderResult.UNDERFLOW : (doneEncoding ? this.encoder.flush(this.byteBuffer) : this.encoder.encode(this.charBuffer, this.byteBuffer, this.endOfInput))).isOverflow()) {
                    this.startDraining(true);
                    continue block0;
                }
                if (result.isUnderflow()) {
                    if (doneEncoding) {
                        this.doneFlushing = true;
                        this.startDraining(false);
                        continue block0;
                    }
                    if (this.endOfInput) {
                        doneEncoding = true;
                        continue;
                    }
                    this.readMoreChars();
                    continue;
                }
                if (result.isError()) break block0;
            }
            break;
        }
        result.throwException();
        return 0;
    }

    private static CharBuffer grow(CharBuffer buf) {
        char[] copy = Arrays.copyOf(buf.array(), buf.capacity() * 2);
        CharBuffer bigger = CharBuffer.wrap(copy);
        Java8Compatibility.position(bigger, buf.position());
        Java8Compatibility.limit(bigger, buf.limit());
        return bigger;
    }

    private void readMoreChars() throws IOException {
        if (ReaderInputStream.availableCapacity(this.charBuffer) == 0) {
            if (this.charBuffer.position() > 0) {
                Java8Compatibility.flip(this.charBuffer.compact());
            } else {
                this.charBuffer = ReaderInputStream.grow(this.charBuffer);
            }
        }
        int limit = this.charBuffer.limit();
        int numChars = this.reader.read(this.charBuffer.array(), limit, ReaderInputStream.availableCapacity(this.charBuffer));
        if (numChars == -1) {
            this.endOfInput = true;
        } else {
            Java8Compatibility.limit(this.charBuffer, limit + numChars);
        }
    }

    private static int availableCapacity(Buffer buffer) {
        return buffer.capacity() - buffer.limit();
    }

    private void startDraining(boolean overflow) {
        Java8Compatibility.flip(this.byteBuffer);
        if (overflow && this.byteBuffer.remaining() == 0) {
            this.byteBuffer = ByteBuffer.allocate(this.byteBuffer.capacity() * 2);
        } else {
            this.draining = true;
        }
    }

    private int drain(byte[] b, int off, int len) {
        int remaining = Math.min(len, this.byteBuffer.remaining());
        this.byteBuffer.get(b, off, remaining);
        return remaining;
    }
}

