/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.io;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.io.CharStreams;
import com.google.common.io.Java8Compatibility;
import com.google.common.io.LineBuffer;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.ArrayDeque;
import java.util.Queue;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
public final class LineReader {
    private final Readable readable;
    private final @Nullable Reader reader;
    private final CharBuffer cbuf = CharStreams.createBuffer();
    private final char[] buf = this.cbuf.array();
    private final Queue<String> lines = new ArrayDeque<String>();
    private final LineBuffer lineBuf = new LineBuffer(){

        @Override
        protected void handleLine(String line, String end) {
            LineReader.this.lines.add(line);
        }
    };

    public LineReader(Readable readable) {
        this.readable = Preconditions.checkNotNull(readable);
        this.reader = readable instanceof Reader ? (Reader)readable : null;
    }

    @CanIgnoreReturnValue
    public @Nullable String readLine() throws IOException {
        while (this.lines.peek() == null) {
            int read;
            Java8Compatibility.clear(this.cbuf);
            int n = read = this.reader != null ? this.reader.read(this.buf, 0, this.buf.length) : this.readable.read(this.cbuf);
            if (read == -1) {
                this.lineBuf.finish();
                break;
            }
            this.lineBuf.add(this.buf, 0, read);
        }
        return this.lines.poll();
    }
}

