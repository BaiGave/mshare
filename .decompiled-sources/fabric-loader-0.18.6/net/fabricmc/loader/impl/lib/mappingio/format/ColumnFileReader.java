/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.mappingio.format;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import net.fabricmc.loader.impl.lib.mappingio.format.tiny.Tiny2Util;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public final class ColumnFileReader
implements Closeable {
    private static final String NO_MATCH = new String();
    private final Reader reader;
    private final char indentationChar;
    private final char columnSeparator;
    private char[] buffer = new char[16384];
    private int bufferPos;
    private int bufferLimit;
    private int lineNumber = 1;
    private boolean bof = true;
    private boolean eol;
    private boolean eof;
    private int markIdx = 0;
    private int[] markedBufferPositions = new int[3];
    private int[] markedLineNumbers = new int[3];
    private boolean[] markedBofs = new boolean[3];
    private boolean[] markedEols = new boolean[3];
    private boolean[] markedEofs = new boolean[3];

    public ColumnFileReader(Reader reader, char indentationChar, char columnSeparator) {
        assert (indentationChar != '\r');
        assert (indentationChar != '\n');
        assert (columnSeparator != '\r');
        assert (columnSeparator != '\n');
        this.reader = reader;
        this.indentationChar = indentationChar;
        this.columnSeparator = columnSeparator;
    }

    @Override
    public void close() throws IOException {
        this.reader.close();
    }

    public boolean nextCol(String expected) throws IOException {
        return this.read(false, false, true, expected) != NO_MATCH;
    }

    @Nullable
    public String nextCol() throws IOException {
        return this.nextCol(false);
    }

    @Nullable
    public String nextCol(boolean unescape) throws IOException {
        return this.read(unescape, true, true, null);
    }

    @Nullable
    private String read(boolean unescape, boolean consume, boolean stopAtNextCol, @Nullable String expected) throws IOException {
        String ret;
        int start;
        boolean isColumnSeparator;
        boolean filled;
        boolean readAnything;
        int modifiedBufferPos;
        int firstEscaped;
        int end;
        block19: {
            int expectedLength;
            if (this.eol) {
                return expected == null ? null : NO_MATCH;
            }
            int n = expectedLength = expected != null ? expected.length() : -1;
            if (expectedLength > 0 && this.bufferPos + expectedLength >= this.bufferLimit && !this.fillBuffer(expectedLength, !consume, false)) {
                return NO_MATCH;
            }
            end = this.bufferPos;
            firstEscaped = -1;
            int contentCharsRead = 0;
            modifiedBufferPos = -1;
            readAnything = false;
            filled = true;
            isColumnSeparator = false;
            while (true) {
                if (end < this.bufferLimit) {
                    char c = this.buffer[end];
                    isColumnSeparator = c == this.columnSeparator;
                    readAnything = true;
                    if (expected != null && (contentCharsRead < expectedLength && c != expected.charAt(contentCharsRead) || contentCharsRead > expectedLength)) {
                        return NO_MATCH;
                    }
                    if (c == '\n' || c == '\r' || isColumnSeparator && stopAtNextCol) {
                        start = this.bufferPos;
                        modifiedBufferPos = end;
                        if (!isColumnSeparator && (consume || expected != null)) {
                            this.eol = true;
                        }
                        break block19;
                    }
                    if (unescape && c == '\\' && firstEscaped < 0) {
                        firstEscaped = this.bufferPos;
                    }
                    ++contentCharsRead;
                    ++end;
                    continue;
                }
                int oldStart = this.bufferPos;
                filled = this.fillBuffer(end - this.bufferPos + 1, !consume, consume);
                int posShift = this.bufferPos - oldStart;
                assert (posShift <= 0);
                end += posShift;
                if (firstEscaped >= 0) {
                    firstEscaped += posShift;
                }
                if (!filled) break;
            }
            start = this.bufferPos;
        }
        if (expected != null) {
            consume = true;
            ret = expected;
        } else {
            int contentLength = end - start;
            ret = contentLength == 0 ? (readAnything ? "" : null) : (firstEscaped >= 0 ? Tiny2Util.unescape(String.valueOf(this.buffer, start, contentLength)) : String.valueOf(this.buffer, start, contentLength));
        }
        if (consume) {
            if (readAnything) {
                this.bof = false;
            }
            if (modifiedBufferPos != -1) {
                this.bufferPos = modifiedBufferPos;
                if (isColumnSeparator && this.fillBuffer(1, false, false)) {
                    ++this.bufferPos;
                }
            }
            if (!filled) {
                this.eol = true;
                this.eof = true;
            }
            if (this.eol && !this.eof) {
                int charsToRead;
                int n = charsToRead = this.buffer[this.bufferPos] == '\r' ? 2 : 1;
                if (end >= this.bufferLimit - charsToRead) {
                    this.fillBuffer(charsToRead, false, true);
                }
            }
        }
        return ret;
    }

    @Nullable
    public String nextCols(boolean unescape) throws IOException {
        return this.read(unescape, true, false, null);
    }

    public int nextIntCol() throws IOException {
        String str = this.nextCol(false);
        try {
            return str != null ? Integer.parseInt(str) : -1;
        }
        catch (NumberFormatException e) {
            throw new IOException("invalid number in line " + this.lineNumber + ": " + str);
        }
    }

    public boolean nextLine(int indent) throws IOException {
        while (true) {
            if (this.bufferPos < this.bufferLimit) {
                char c = this.buffer[this.bufferPos];
                if (c == '\n') {
                    if (indent == 0) {
                        if (!this.fillBuffer(2, false, true)) break;
                        char next = this.buffer[this.bufferPos + 1];
                        if (next == '\n' || next == '\r') {
                            ++this.bufferPos;
                            ++this.lineNumber;
                            this.bof = false;
                            continue;
                        }
                    }
                    if (!this.fillBuffer(indent + 1, false, true)) {
                        return false;
                    }
                    for (int i = 1; i <= indent; ++i) {
                        if (this.buffer[this.bufferPos + i] == this.indentationChar) continue;
                        return false;
                    }
                    this.bufferPos += indent + 1;
                    ++this.lineNumber;
                    this.bof = false;
                    this.eol = false;
                    return true;
                }
                ++this.bufferPos;
                this.bof = false;
                continue;
            }
            if (!this.fillBuffer(1, false, true)) break;
        }
        return false;
    }

    public boolean hasExtraIndents() throws IOException {
        return this.fillBuffer(1, false, false) && this.buffer[this.bufferPos] == this.indentationChar;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public boolean isAtEol() {
        return this.eol;
    }

    public boolean isAtEof() {
        return this.eof;
    }

    public int mark() {
        if (this.markIdx == 0 && this.bufferPos > 0) {
            int available = this.bufferLimit - this.bufferPos;
            System.arraycopy(this.buffer, this.bufferPos, this.buffer, 0, available);
            this.bufferPos = 0;
            this.bufferLimit = available;
        }
        if (this.markIdx == this.markedBufferPositions.length) {
            this.markedBufferPositions = Arrays.copyOf(this.markedBufferPositions, this.markedBufferPositions.length * 2);
            this.markedLineNumbers = Arrays.copyOf(this.markedLineNumbers, this.markedLineNumbers.length * 2);
            this.markedBofs = Arrays.copyOf(this.markedBofs, this.markedBofs.length * 2);
            this.markedEols = Arrays.copyOf(this.markedEols, this.markedEols.length * 2);
            this.markedEofs = Arrays.copyOf(this.markedEofs, this.markedEofs.length * 2);
        }
        this.markedBufferPositions[this.markIdx] = this.bufferPos;
        this.markedLineNumbers[this.markIdx] = this.lineNumber;
        this.markedBofs[this.markIdx] = this.bof;
        this.markedEols[this.markIdx] = this.eol;
        this.markedEofs[this.markIdx] = this.eof;
        return ++this.markIdx;
    }

    public void discardMark() {
        this.discardMark(this.markIdx);
    }

    private void discardMark(int index) {
        if (this.markIdx == 0) {
            throw new IllegalStateException("no mark to discard");
        }
        if (index < 1 || index > this.markIdx) {
            throw new IllegalStateException("index out of bounds");
        }
        for (int i = this.markIdx; i >= index; --i) {
            this.markedBufferPositions[i - 1] = 0;
            this.markedLineNumbers[i - 1] = 0;
        }
        this.markIdx = index - 1;
    }

    public int reset() {
        this.reset(this.markIdx);
        return this.markIdx;
    }

    public void reset(int indexToResetTo) {
        if (this.markIdx == 0) {
            throw new IllegalStateException("no mark to reset to");
        }
        if (indexToResetTo < -this.markIdx || indexToResetTo > this.markIdx) {
            throw new IllegalStateException("index out of bounds");
        }
        if (indexToResetTo < 0) {
            indexToResetTo += this.markIdx;
        }
        int arrayIdx = indexToResetTo == 0 ? indexToResetTo : indexToResetTo - 1;
        this.bufferPos = this.markedBufferPositions[arrayIdx];
        this.lineNumber = this.markedLineNumbers[arrayIdx];
        this.bof = this.markedBofs[arrayIdx];
        this.eol = this.markedEols[arrayIdx];
        this.eof = this.markedEofs[arrayIdx];
        if (indexToResetTo == 0) {
            this.discardMark(1);
        }
        this.markIdx = indexToResetTo;
    }

    private boolean fillBuffer(int count, boolean preventCompaction, boolean markEof) throws IOException {
        int available = this.bufferLimit - this.bufferPos;
        int req = count - available;
        if (req <= 0) {
            return true;
        }
        if (this.bufferPos + count > this.buffer.length) {
            if (this.markIdx > 0 || preventCompaction) {
                this.buffer = Arrays.copyOf(this.buffer, Math.max(this.bufferPos + count, this.buffer.length * 2));
            } else {
                if (count > this.buffer.length) {
                    char[] newBuffer = new char[Math.max(count, this.buffer.length * 2)];
                    System.arraycopy(this.buffer, this.bufferPos, newBuffer, 0, available);
                    this.buffer = newBuffer;
                } else {
                    System.arraycopy(this.buffer, this.bufferPos, this.buffer, 0, available);
                }
                this.bufferPos = 0;
                this.bufferLimit = available;
            }
        }
        int reqLimit = this.bufferLimit + req;
        do {
            int read;
            if ((read = this.reader.read(this.buffer, this.bufferLimit, this.buffer.length - this.bufferLimit)) < 0) {
                if (markEof) {
                    this.eol = true;
                    this.eof = true;
                }
                return false;
            }
            this.bufferLimit += read;
        } while (this.bufferLimit < reqLimit);
        return true;
    }
}

