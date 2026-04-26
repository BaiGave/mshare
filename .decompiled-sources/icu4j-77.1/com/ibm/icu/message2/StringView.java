/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.message2;

class StringView
implements CharSequence {
    final int offset;
    final String text;

    StringView(String text, int offset) {
        this.offset = offset;
        this.text = text;
    }

    StringView(String text) {
        this(text, 0);
    }

    @Override
    public int length() {
        return this.text.length() - this.offset;
    }

    @Override
    public char charAt(int index) {
        return this.text.charAt(index + this.offset);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return this.text.subSequence(start + this.offset, end + this.offset);
    }

    @Override
    public String toString() {
        return this.text.substring(this.offset);
    }
}

