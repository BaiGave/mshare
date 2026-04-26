/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.input;

import java.io.IOException;
import java.io.Reader;
import org.apache.commons.io.input.UnsynchronizedReader;

public class UnsynchronizedBufferedReader
extends UnsynchronizedReader {
    private static final char NUL = '\u0000';
    private final Reader in;
    private char[] buf;
    private int pos;
    private int end;
    private int mark = -1;
    private int markLimit = -1;

    public UnsynchronizedBufferedReader(Reader in) {
        this(in, 8192);
    }

    public UnsynchronizedBufferedReader(Reader in, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("size <= 0");
        }
        this.in = in;
        this.buf = new char[size];
    }

    final void chompNewline() throws IOException {
        if ((this.pos != this.end || this.fillBuf() != -1) && this.buf[this.pos] == '\n') {
            ++this.pos;
        }
    }

    @Override
    public void close() throws IOException {
        if (!this.isClosed()) {
            this.in.close();
            this.buf = null;
            super.close();
        }
    }

    private int fillBuf() throws IOException {
        if (this.mark == -1 || this.pos - this.mark >= this.markLimit) {
            int result = this.in.read(this.buf, 0, this.buf.length);
            if (result > 0) {
                this.mark = -1;
                this.pos = 0;
                this.end = result;
            }
            return result;
        }
        if (this.mark == 0 && this.markLimit > this.buf.length) {
            int newLength = this.buf.length * 2;
            if (newLength > this.markLimit) {
                newLength = this.markLimit;
            }
            char[] newbuf = new char[newLength];
            System.arraycopy(this.buf, 0, newbuf, 0, this.buf.length);
            this.buf = newbuf;
        } else if (this.mark > 0) {
            System.arraycopy(this.buf, this.mark, this.buf, 0, this.buf.length - this.mark);
            this.pos -= this.mark;
            this.end -= this.mark;
            this.mark = 0;
        }
        int count = this.in.read(this.buf, this.pos, this.buf.length - this.pos);
        if (count != -1) {
            this.end += count;
        }
        return count;
    }

    @Override
    public void mark(int markLimit) throws IOException {
        if (markLimit < 0) {
            throw new IllegalArgumentException();
        }
        this.checkOpen();
        this.markLimit = markLimit;
        this.mark = this.pos;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    public int peek() throws IOException {
        this.mark(1);
        int c = this.read();
        this.reset();
        return c;
    }

    public int peek(char[] buf) throws IOException {
        int n = buf.length;
        this.mark(n);
        int c = this.read(buf, 0, n);
        this.reset();
        return c;
    }

    @Override
    public int read() throws IOException {
        this.checkOpen();
        if (this.pos < this.end || this.fillBuf() != -1) {
            return this.buf[this.pos++];
        }
        return -1;
    }

    @Override
    public int read(char[] buffer, int offset, int length) throws IOException {
        int count;
        this.checkOpen();
        if (offset < 0 || offset > buffer.length - length || length < 0) {
            throw new IndexOutOfBoundsException();
        }
        int outstanding = length;
        while (outstanding > 0) {
            int count2;
            int available = this.end - this.pos;
            if (available > 0) {
                count2 = available >= outstanding ? outstanding : available;
                System.arraycopy(this.buf, this.pos, buffer, offset, count2);
                this.pos += count2;
                offset += count2;
                outstanding -= count2;
            }
            if (outstanding == 0 || outstanding < length && !this.in.ready()) break;
            if ((this.mark == -1 || this.pos - this.mark >= this.markLimit) && outstanding >= this.buf.length) {
                count2 = this.in.read(buffer, offset, outstanding);
                if (count2 <= 0) break;
                outstanding -= count2;
                this.mark = -1;
                break;
            }
            if (this.fillBuf() != -1) continue;
            break;
        }
        return (count = length - outstanding) > 0 || count == length ? count : -1;
    }

    public String readLine() throws IOException {
        this.checkOpen();
        if (this.pos == this.end && this.fillBuf() == -1) {
            return null;
        }
        for (int charPos = this.pos; charPos < this.end; ++charPos) {
            char ch = this.buf[charPos];
            if (ch > '\r') continue;
            if (ch == '\n') {
                String res = new String(this.buf, this.pos, charPos - this.pos);
                this.pos = charPos + 1;
                return res;
            }
            if (ch != '\r') continue;
            String res = new String(this.buf, this.pos, charPos - this.pos);
            this.pos = charPos + 1;
            if ((this.pos < this.end || this.fillBuf() != -1) && this.buf[this.pos] == '\n') {
                ++this.pos;
            }
            return res;
        }
        int eol = 0;
        StringBuilder result = new StringBuilder(80);
        result.append(this.buf, this.pos, this.end - this.pos);
        while (true) {
            this.pos = this.end;
            if (eol == 10) {
                return result.toString();
            }
            if (this.fillBuf() == -1) {
                return result.length() > 0 || eol != 0 ? result.toString() : null;
            }
            for (int charPos = this.pos; charPos < this.end; ++charPos) {
                int c = this.buf[charPos];
                if (eol != 0) {
                    if (eol == 13 && c == 10) {
                        if (charPos > this.pos) {
                            result.append(this.buf, this.pos, charPos - this.pos - 1);
                        }
                        this.pos = charPos + 1;
                    } else {
                        if (charPos > this.pos) {
                            result.append(this.buf, this.pos, charPos - this.pos - 1);
                        }
                        this.pos = charPos;
                    }
                    return result.toString();
                }
                if (c != 10 && c != 13) continue;
                eol = c;
            }
            if (eol == 0) {
                result.append(this.buf, this.pos, this.end - this.pos);
                continue;
            }
            result.append(this.buf, this.pos, this.end - this.pos - 1);
        }
    }

    @Override
    public boolean ready() throws IOException {
        this.checkOpen();
        return this.end - this.pos > 0 || this.in.ready();
    }

    @Override
    public void reset() throws IOException {
        this.checkOpen();
        if (this.mark == -1) {
            throw new IOException("mark == -1");
        }
        this.pos = this.mark;
    }

    @Override
    public long skip(long amount) throws IOException {
        if (amount < 0L) {
            throw new IllegalArgumentException();
        }
        this.checkOpen();
        if (amount < 1L) {
            return 0L;
        }
        if ((long)(this.end - this.pos) >= amount) {
            this.pos += Math.toIntExact(amount);
            return amount;
        }
        this.pos = this.end;
        for (long read = (long)(this.end - this.pos); read < amount; read += (long)(this.end - this.pos)) {
            if (this.fillBuf() == -1) {
                return read;
            }
            if ((long)(this.end - this.pos) >= amount - read) {
                this.pos += Math.toIntExact(amount - read);
                return amount;
            }
            this.pos = this.end;
        }
        return amount;
    }
}

