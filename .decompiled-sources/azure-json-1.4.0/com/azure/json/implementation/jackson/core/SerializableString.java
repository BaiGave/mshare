/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation.jackson.core;

public interface SerializableString {
    public String getValue();

    public char[] asQuotedChars();

    public byte[] asUnquotedUTF8();

    public byte[] asQuotedUTF8();

    public int appendQuotedUTF8(byte[] var1, int var2);

    public int appendQuoted(char[] var1, int var2);

    public int appendUnquotedUTF8(byte[] var1, int var2);

    public int appendUnquoted(char[] var1, int var2);
}

