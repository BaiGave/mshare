/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.io;

import it.unimi.dsi.fastutil.bytes.ByteArrays;
import it.unimi.dsi.fastutil.io.MeasurableOutputStream;
import it.unimi.dsi.fastutil.io.RepositionableStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class FastByteArrayOutputStream
extends MeasurableOutputStream
implements RepositionableStream,
ObjectOutput {
    public static final int DEFAULT_INITIAL_CAPACITY = 16;
    public byte[] array;
    public int length;
    protected int position;

    public FastByteArrayOutputStream() {
        this(16);
    }

    public FastByteArrayOutputStream(int initialCapacity) {
        this.array = new byte[initialCapacity];
    }

    public FastByteArrayOutputStream(byte[] a) {
        this.array = a;
    }

    public void reset() {
        this.length = 0;
        this.position = 0;
    }

    public void trim() {
        this.array = ByteArrays.trim(this.array, this.length);
    }

    @Override
    public void write(int b) {
        if (this.position >= this.array.length) {
            this.array = ByteArrays.grow(this.array, this.position + 1, this.length);
        }
        this.array[this.position++] = (byte)b;
        if (this.length < this.position) {
            this.length = this.position;
        }
    }

    @Override
    public void write(byte[] b, int off, int len) {
        ByteArrays.ensureOffsetLength(b, off, len);
        if (this.position + len > this.array.length) {
            this.array = ByteArrays.grow(this.array, this.position + len, this.position);
        }
        System.arraycopy(b, off, this.array, this.position, len);
        if (this.position + len > this.length) {
            this.length = this.position += len;
        }
    }

    @Override
    public void position(long newPosition) {
        if (newPosition > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Position too large: " + newPosition);
        }
        this.position = (int)newPosition;
    }

    @Override
    public long position() {
        return this.position;
    }

    @Override
    public long length() {
        return this.length;
    }

    public byte[] toByteArray() {
        return ByteArrays.copy(this.array, 0, this.length);
    }

    @Override
    public void close() {
    }

    @Override
    public void write(byte[] b) {
        this.write(b, 0, b.length);
    }

    public String toString(Charset charset) {
        return new String(this.array, 0, this.length, charset);
    }

    public synchronized void writeTo(OutputStream out) throws IOException {
        out.write(this.array, 0, this.length);
    }

    @Override
    public void writeBoolean(boolean v) {
        this.write(v ? 1 : 0);
    }

    @Override
    public void writeByte(int v) {
        this.write(v);
    }

    @Override
    public void writeShort(int v) {
        this.write(v >> 8);
        this.write(v);
    }

    @Override
    public void writeChar(int v) {
        this.write(v >> 8);
        this.write(v);
    }

    @Override
    public void writeInt(int v) {
        this.write(v >> 24);
        this.write(v >> 16);
        this.write(v >> 8);
        this.write(v);
    }

    @Override
    public void writeLong(long v) {
        this.writeInt((int)(v >> 32));
        this.writeInt((int)v);
    }

    @Override
    public void writeFloat(float v) {
        this.writeInt(Float.floatToIntBits(v));
    }

    @Override
    public void writeDouble(double v) {
        this.writeLong(Double.doubleToLongBits(v));
    }

    @Override
    @Deprecated
    public void writeBytes(String s) {
        int len = s.length();
        for (int i = 0; i < len; ++i) {
            this.write((byte)s.charAt(i));
        }
    }

    @Override
    public void writeChars(String s) {
        int len = s.length();
        for (int i = 0; i < len; ++i) {
            char v = s.charAt(i);
            this.writeChar(v);
        }
    }

    @Override
    public void writeUTF(String s) {
        int savePos = this.position;
        this.writeShort(0);
        int len = s.length();
        for (int i = 0; i < len; ++i) {
            this.writeUtf8Char(s.charAt(i));
            if (this.position - savePos <= 65537) continue;
            this.length = this.position = savePos;
            throw new IllegalArgumentException(String.format("UTF encoded string too long: %d: %s", s.length(), s.substring(0, 99)));
        }
        int len2 = this.position - savePos - 2;
        this.array[savePos] = (byte)(len2 >> 8);
        this.array[savePos + 1] = (byte)len2;
    }

    public int writeUtf8Char(char c) {
        if (c != '\u0000' && c < '\u0080') {
            this.write(c);
            return 1;
        }
        if (c >= '\u0800') {
            this.write(0xE0 | c >> 12 & 0xF);
            this.write(0x80 | c >> 6 & 0x3F);
            this.write(0x80 | c & 0x3F);
            return 3;
        }
        this.write(0xC0 | c >> 6 & 0x1F);
        this.write(0x80 | c & 0x3F);
        return 2;
    }

    @Override
    public void writeObject(Object obj) throws IOException {
        try (ObjectOutputStream oout = new ObjectOutputStream(this);){
            oout.writeObject(obj);
            oout.flush();
        }
    }
}

