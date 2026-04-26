/*
 * Decompiled with CFR 0.152.
 */
package it.unimi.dsi.fastutil.io;

import it.unimi.dsi.fastutil.io.MeasurableInputStream;
import it.unimi.dsi.fastutil.io.RepositionableStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.UTFDataFormatException;
import java.io.UncheckedIOException;

public class FastByteArrayInputStream
extends MeasurableInputStream
implements RepositionableStream,
ObjectInput {
    public byte[] array;
    public int offset;
    public int length;
    protected int position;
    private int mark;

    public FastByteArrayInputStream(byte[] array, int offset, int length) {
        this.array = array;
        this.offset = offset;
        this.length = Math.min(length, array.length - offset);
    }

    public FastByteArrayInputStream(byte[] array) {
        this(array, 0, array.length);
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public void reset() {
        this.position = this.mark;
    }

    @Override
    public void close() {
    }

    @Override
    public void mark(int dummy) {
        this.mark = this.position;
    }

    @Override
    public int available() {
        return this.length - this.position;
    }

    @Override
    public long skip(long n) {
        if (n <= (long)(this.length - this.position)) {
            this.position += (int)n;
            return n;
        }
        n = this.length - this.position;
        this.position = this.length;
        return n;
    }

    @Override
    public int read() {
        if (this.length == this.position) {
            return -1;
        }
        return this.array[this.offset + this.position++] & 0xFF;
    }

    @Override
    public int read(byte[] b, int offset, int length) {
        if (this.length == this.position) {
            return length == 0 ? 0 : -1;
        }
        int n = Math.min(length, this.length - this.position);
        System.arraycopy(this.array, this.offset + this.position, b, offset, n);
        this.position += n;
        return n;
    }

    @Override
    public long position() {
        return this.position;
    }

    @Override
    public void position(long newPosition) {
        this.position = (int)Math.min(newPosition, (long)this.length);
    }

    @Override
    public long length() {
        return this.length;
    }

    @Override
    public int read(byte[] b) {
        return this.read(b, 0, b.length);
    }

    public int peek() {
        if ((long)this.length <= this.position()) {
            return -1;
        }
        return this.array[(int)((long)this.offset + this.position())] & 0xFF;
    }

    @Override
    public void readFully(byte[] b) {
        this.read(b);
    }

    @Override
    public void readFully(byte[] b, int off, int len) {
        this.read(b, off, len);
    }

    @Override
    public int skipBytes(int n) {
        return (int)this.skip(n);
    }

    @Override
    public boolean readBoolean() {
        return this.read() != 0;
    }

    @Override
    public byte readByte() {
        return (byte)this.read();
    }

    @Override
    public int readUnsignedByte() {
        return this.read() & 0xFF;
    }

    @Override
    public short readShort() {
        return (short)(this.read() << 8 | this.read() & 0xFF);
    }

    @Override
    public int readUnsignedShort() {
        return (this.read() & 0xFF) << 8 | this.read() & 0xFF;
    }

    @Override
    public char readChar() {
        return (char)((this.read() & 0xFF) << 8 | this.read() & 0xFF);
    }

    @Override
    public int readInt() {
        return this.read() << 24 | (this.read() & 0xFF) << 16 | (this.read() & 0xFF) << 8 | this.read() & 0xFF;
    }

    @Override
    public long readLong() {
        return (long)this.readInt() << 32 | (long)this.readInt() & 0xFFFFFFFFL;
    }

    @Override
    public float readFloat() {
        return Float.intBitsToFloat(this.readInt());
    }

    @Override
    public double readDouble() {
        return Double.longBitsToDouble(this.readLong());
    }

    @Override
    @Deprecated
    public String readLine() {
        StringBuilder sb = new StringBuilder(99);
        block5: while (true) {
            int c = this.read();
            switch (c) {
                case -1: {
                    break block5;
                }
                case 10: {
                    return sb.toString();
                }
                case 13: {
                    if (this.peek() == 10) {
                        this.read();
                    }
                    return sb.toString();
                }
                default: {
                    sb.append((char)c);
                    continue block5;
                }
            }
            break;
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    @Override
    public String readUTF() throws UTFDataFormatException {
        try {
            return this.available() > 0 ? DataInputStream.readUTF(this) : null;
        }
        catch (UTFDataFormatException badBinaryFormatting) {
            throw badBinaryFormatting;
        }
        catch (IOException e) {
            throw new UncheckedIOException("readUTF @ " + this, e);
        }
    }

    @Override
    public Object readObject() throws ClassNotFoundException, IOException {
        try (ObjectInputStream ois = new ObjectInputStream(this);){
            Object object = ois.readObject();
            return object;
        }
    }
}

