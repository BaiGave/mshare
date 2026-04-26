/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl;

import com.ibm.icu.impl.PatternProps;
import com.ibm.icu.impl.Utility;
import com.ibm.icu.text.SymbolTable;
import com.ibm.icu.text.UTF16;
import java.text.ParsePosition;

public class RuleCharacterIterator {
    private String text;
    private ParsePosition pos;
    private SymbolTable sym;
    private String buf;
    private int bufPos;
    private boolean isEscaped;
    public static final int DONE = -1;
    public static final int PARSE_VARIABLES = 1;
    public static final int PARSE_ESCAPES = 2;
    public static final int SKIP_WHITESPACE = 4;

    public RuleCharacterIterator(String text, SymbolTable sym, ParsePosition pos) {
        if (text == null || pos.getIndex() > text.length()) {
            throw new IllegalArgumentException();
        }
        this.text = text;
        this.sym = sym;
        this.pos = pos;
        this.buf = null;
    }

    public boolean atEnd() {
        return this.buf == null && this.pos.getIndex() == this.text.length();
    }

    public int next(int options) {
        int c;
        block6: {
            c = -1;
            this.isEscaped = false;
            while (true) {
                c = this._current();
                this._advance(UTF16.getCharCount(c));
                if (c == 36 && this.buf == null && (options & 1) != 0 && this.sym != null) {
                    String name = this.sym.parseReference(this.text, this.pos, this.text.length());
                    if (name != null) {
                        this.bufPos = 0;
                        char[] chars = this.sym.lookup(name);
                        if (chars == null) {
                            this.buf = null;
                            throw new IllegalArgumentException("Undefined variable: " + name);
                        }
                        if (chars.length == 0) {
                            this.buf = null;
                        }
                        this.buf = new String(chars);
                        continue;
                    }
                    break block6;
                }
                if ((options & 4) == 0 || !PatternProps.isWhiteSpace(c)) break;
            }
            if (c != 92 || (options & 2) == 0) break block6;
            int cpAndLength = Utility.unescapeAndLengthAt(this.getCurrentBuffer(), this.getCurrentBufferPos());
            if (cpAndLength < 0) {
                throw new IllegalArgumentException("Invalid escape");
            }
            c = Utility.cpFromCodePointAndLength(cpAndLength);
            this.jumpahead(Utility.lengthFromCodePointAndLength(cpAndLength));
            this.isEscaped = true;
        }
        return c;
    }

    public boolean isEscaped() {
        return this.isEscaped;
    }

    public boolean inVariable() {
        return this.buf != null;
    }

    public Position getPos(Position p) {
        if (p == null) {
            p = new Position();
        }
        p.buf = this.buf;
        p.bufPos = this.bufPos;
        p.posIndex = this.pos.getIndex();
        return p;
    }

    public void setPos(Position p) {
        this.buf = p.buf;
        this.pos.setIndex(p.posIndex);
        this.bufPos = p.bufPos;
    }

    public void skipIgnored(int options) {
        if ((options & 4) != 0) {
            int a;
            while (PatternProps.isWhiteSpace(a = this._current())) {
                this._advance(UTF16.getCharCount(a));
            }
        }
    }

    public String getCurrentBuffer() {
        if (this.buf != null) {
            return this.buf;
        }
        return this.text;
    }

    public int getCurrentBufferPos() {
        if (this.buf != null) {
            return this.bufPos;
        }
        return this.pos.getIndex();
    }

    public void jumpahead(int count) {
        if (count < 0) {
            throw new IllegalArgumentException();
        }
        if (this.buf != null) {
            this.bufPos += count;
            if (this.bufPos > this.buf.length()) {
                throw new IllegalArgumentException();
            }
            if (this.bufPos == this.buf.length()) {
                this.buf = null;
            }
        } else {
            int i = this.pos.getIndex() + count;
            this.pos.setIndex(i);
            if (i > this.text.length()) {
                throw new IllegalArgumentException();
            }
        }
    }

    public String toString() {
        int b = this.pos.getIndex();
        return this.text.substring(0, b) + '|' + this.text.substring(b);
    }

    private int _current() {
        if (this.buf != null) {
            return UTF16.charAt(this.buf, this.bufPos);
        }
        int i = this.pos.getIndex();
        return i < this.text.length() ? UTF16.charAt(this.text, i) : -1;
    }

    private void _advance(int count) {
        if (this.buf != null) {
            this.bufPos += count;
            if (this.bufPos == this.buf.length()) {
                this.buf = null;
            }
        } else {
            this.pos.setIndex(this.pos.getIndex() + count);
            if (this.pos.getIndex() > this.text.length()) {
                this.pos.setIndex(this.text.length());
            }
        }
    }

    public static final class Position {
        private String buf;
        private int bufPos;
        private int posIndex;
    }
}

