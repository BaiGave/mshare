/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl;

import com.ibm.icu.impl.UnicodeMap;
import com.ibm.icu.text.UTF16;
import java.util.Iterator;
import java.util.Set;

public class UnicodeMapIterator<T> {
    public static int IS_STRING = -1;
    public int codepoint;
    public int codepointEnd;
    public String string;
    public T value;
    private UnicodeMap<T> map;
    private int endRange = 0;
    private int range = 0;
    private Iterator<String> stringIterator = null;
    protected int endElement;
    protected int nextElement;

    public UnicodeMapIterator(UnicodeMap set) {
        this.reset(set);
    }

    public UnicodeMapIterator() {
        this.reset(new UnicodeMap());
    }

    public boolean next() {
        if (this.nextElement <= this.endElement) {
            this.codepointEnd = this.nextElement++;
            this.codepoint = this.codepointEnd;
            return true;
        }
        while (this.range < this.endRange) {
            if (this.loadRange(++this.range) == null) continue;
            this.codepointEnd = this.nextElement++;
            this.codepoint = this.codepointEnd;
            return true;
        }
        if (this.stringIterator == null) {
            return false;
        }
        this.codepoint = IS_STRING;
        this.string = this.stringIterator.next();
        if (!this.stringIterator.hasNext()) {
            this.stringIterator = null;
        }
        return true;
    }

    public boolean nextRange() {
        if (this.nextElement <= this.endElement) {
            this.codepointEnd = this.endElement;
            this.codepoint = this.nextElement;
            this.nextElement = this.endElement + 1;
            return true;
        }
        while (this.range < this.endRange) {
            if (this.loadRange(++this.range) == null) continue;
            this.codepointEnd = this.endElement;
            this.codepoint = this.nextElement;
            this.nextElement = this.endElement + 1;
            return true;
        }
        if (this.stringIterator == null) {
            return false;
        }
        this.codepoint = IS_STRING;
        this.string = this.stringIterator.next();
        if (!this.stringIterator.hasNext()) {
            this.stringIterator = null;
        }
        return true;
    }

    public void reset(UnicodeMap set) {
        this.map = set;
        this.reset();
    }

    public UnicodeMapIterator<T> reset() {
        this.endRange = this.map.getRangeCount() - 1;
        this.nextElement = 0;
        this.endElement = -1;
        this.range = -1;
        this.stringIterator = null;
        Set<String> strings = this.map.getNonRangeStrings();
        if (strings != null) {
            this.stringIterator = strings.iterator();
            if (!this.stringIterator.hasNext()) {
                this.stringIterator = null;
            }
        }
        this.value = null;
        return this;
    }

    public String getString() {
        if (this.codepoint != IS_STRING) {
            return UTF16.valueOf(this.codepoint);
        }
        return this.string;
    }

    protected T loadRange(int range) {
        this.nextElement = this.map.getRangeStart(range);
        this.endElement = this.map.getRangeEnd(range);
        this.value = this.map.getRangeValue(range);
        return this.value;
    }
}

