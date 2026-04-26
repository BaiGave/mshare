/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl.coll;

import com.ibm.icu.impl.Normalizer2Impl;
import com.ibm.icu.impl.Trie2_32;
import com.ibm.icu.impl.coll.Collation;
import com.ibm.icu.impl.coll.UVector32;
import com.ibm.icu.text.UnicodeSet;
import com.ibm.icu.util.ICUException;

public final class CollationData {
    static final int REORDER_RESERVED_BEFORE_LATIN = 4110;
    static final int REORDER_RESERVED_AFTER_LATIN = 4111;
    static final int MAX_NUM_SPECIAL_REORDER_CODES = 8;
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    static final int JAMO_CE32S_LENGTH = 67;
    Trie2_32 trie;
    int[] ce32s;
    long[] ces;
    String contexts;
    public CollationData base;
    int[] jamoCE32s = new int[67];
    public Normalizer2Impl nfcImpl;
    long numericPrimary = 0x12000000L;
    public boolean[] compressibleBytes;
    UnicodeSet unsafeBackwardSet;
    public char[] fastLatinTable;
    char[] fastLatinTableHeader;
    int numScripts;
    char[] scriptsIndex;
    char[] scriptStarts;
    public long[] rootElements;

    CollationData(Normalizer2Impl nfc) {
        this.nfcImpl = nfc;
    }

    public int getCE32(int c) {
        return this.trie.get(c);
    }

    int getCE32FromSupplementary(int c) {
        return this.trie.get(c);
    }

    boolean isDigit(int c) {
        return c < 1632 ? c <= 57 && 48 <= c : Collation.hasCE32Tag(this.getCE32(c), 10);
    }

    public boolean isUnsafeBackward(int c, boolean numeric) {
        return this.unsafeBackwardSet.contains(c) || numeric && this.isDigit(c);
    }

    public boolean isCompressibleLeadByte(int b) {
        return this.compressibleBytes[b];
    }

    public boolean isCompressiblePrimary(long p) {
        return this.isCompressibleLeadByte((int)p >>> 24);
    }

    int getCE32FromContexts(int index) {
        return this.contexts.charAt(index) << 16 | this.contexts.charAt(index + 1);
    }

    int getIndirectCE32(int ce32) {
        assert (Collation.isSpecialCE32(ce32));
        int tag = Collation.tagFromCE32(ce32);
        if (tag == 10) {
            ce32 = this.ce32s[Collation.indexFromCE32(ce32)];
        } else if (tag == 13) {
            ce32 = -1;
        } else if (tag == 11) {
            ce32 = this.ce32s[0];
        }
        return ce32;
    }

    int getFinalCE32(int ce32) {
        if (Collation.isSpecialCE32(ce32)) {
            ce32 = this.getIndirectCE32(ce32);
        }
        return ce32;
    }

    long getCEFromOffsetCE32(int c, int ce32) {
        long dataCE = this.ces[Collation.indexFromCE32(ce32)];
        return Collation.makeCE(Collation.getThreeBytePrimaryForOffsetData(c, dataCE));
    }

    long getSingleCE(int c) {
        CollationData d;
        int ce32 = this.getCE32(c);
        if (ce32 == 192) {
            d = this.base;
            ce32 = this.base.getCE32(c);
        } else {
            d = this;
        }
        while (Collation.isSpecialCE32(ce32)) {
            switch (Collation.tagFromCE32(ce32)) {
                case 4: 
                case 7: 
                case 8: 
                case 9: 
                case 12: 
                case 13: {
                    throw new UnsupportedOperationException(String.format("there is not exactly one collation element for U+%04X (CE32 0x%08x)", c, ce32));
                }
                case 0: 
                case 3: {
                    throw new AssertionError((Object)String.format("unexpected CE32 tag for U+%04X (CE32 0x%08x)", c, ce32));
                }
                case 1: {
                    return Collation.ceFromLongPrimaryCE32(ce32);
                }
                case 2: {
                    return Collation.ceFromLongSecondaryCE32(ce32);
                }
                case 5: {
                    if (Collation.lengthFromCE32(ce32) == 1) {
                        ce32 = d.ce32s[Collation.indexFromCE32(ce32)];
                        break;
                    }
                    throw new UnsupportedOperationException(String.format("there is not exactly one collation element for U+%04X (CE32 0x%08x)", c, ce32));
                }
                case 6: {
                    if (Collation.lengthFromCE32(ce32) == 1) {
                        return d.ces[Collation.indexFromCE32(ce32)];
                    }
                    throw new UnsupportedOperationException(String.format("there is not exactly one collation element for U+%04X (CE32 0x%08x)", c, ce32));
                }
                case 10: {
                    ce32 = d.ce32s[Collation.indexFromCE32(ce32)];
                    break;
                }
                case 11: {
                    assert (c == 0);
                    ce32 = d.ce32s[0];
                    break;
                }
                case 14: {
                    return d.getCEFromOffsetCE32(c, ce32);
                }
                case 15: {
                    return Collation.unassignedCEFromCodePoint(c);
                }
            }
        }
        return Collation.ceFromSimpleCE32(ce32);
    }

    int getFCD16(int c) {
        return this.nfcImpl.getFCD16(c);
    }

    long getFirstPrimaryForGroup(int script) {
        int index = this.getScriptIndex(script);
        return index == 0 ? 0L : (long)this.scriptStarts[index] << 16;
    }

    public long getLastPrimaryForGroup(int script) {
        int index = this.getScriptIndex(script);
        if (index == 0) {
            return 0L;
        }
        long limit = this.scriptStarts[index + 1];
        return (limit << 16) - 1L;
    }

    public int getGroupForPrimary(long p) {
        int i;
        if ((p >>= 16) < (long)this.scriptStarts[1] || (long)this.scriptStarts[this.scriptStarts.length - 1] <= p) {
            return -1;
        }
        char index = '\u0001';
        while (p >= (long)this.scriptStarts[index + 1]) {
            ++index;
        }
        for (i = 0; i < this.numScripts; ++i) {
            if (this.scriptsIndex[i] != index) continue;
            return i;
        }
        for (i = 0; i < 8; ++i) {
            if (this.scriptsIndex[this.numScripts + i] != index) continue;
            return 4096 + i;
        }
        return -1;
    }

    private int getScriptIndex(int script) {
        if (script < 0) {
            return 0;
        }
        if (script < this.numScripts) {
            return this.scriptsIndex[script];
        }
        if (script < 4096) {
            return 0;
        }
        if ((script -= 4096) < 8) {
            return this.scriptsIndex[this.numScripts + script];
        }
        return 0;
    }

    public int[] getEquivalentScripts(int script) {
        int index = this.getScriptIndex(script);
        if (index == 0) {
            return EMPTY_INT_ARRAY;
        }
        if (script >= 4096) {
            return new int[]{script};
        }
        int length = 0;
        for (int i = 0; i < this.numScripts; ++i) {
            if (this.scriptsIndex[i] != index) continue;
            ++length;
        }
        int[] dest = new int[length];
        if (length == 1) {
            dest[0] = script;
            return dest;
        }
        length = 0;
        for (int i = 0; i < this.numScripts; ++i) {
            if (this.scriptsIndex[i] != index) continue;
            dest[length++] = i;
        }
        return dest;
    }

    void makeReorderRanges(int[] reorder, UVector32 ranges) {
        this.makeReorderRanges(reorder, false, ranges);
    }

    private void makeReorderRanges(int[] reorder, boolean latinMustMove, UVector32 ranges) {
        char index;
        int i;
        ranges.removeAllElements();
        int length = reorder.length;
        if (length == 0 || length == 1 && reorder[0] == 103) {
            return;
        }
        short[] table = new short[this.scriptStarts.length - 1];
        char index2 = this.scriptsIndex[this.numScripts + 4110 - 4096];
        if (index2 != '\u0000') {
            table[index2] = 255;
        }
        if ((index2 = this.scriptsIndex[this.numScripts + 4111 - 4096]) != '\u0000') {
            table[index2] = 255;
        }
        assert (this.scriptStarts.length >= 2);
        assert (this.scriptStarts[0] == '\u0000');
        int lowStart = this.scriptStarts[1];
        assert (lowStart == 768);
        int highLimit = this.scriptStarts[this.scriptStarts.length - 1];
        assert (highLimit == 65280);
        int specials = 0;
        for (i = 0; i < length; ++i) {
            int reorderCode = reorder[i] - 4096;
            if (0 > reorderCode || reorderCode >= 8) continue;
            specials |= 1 << reorderCode;
        }
        for (i = 0; i < 8; ++i) {
            index = this.scriptsIndex[this.numScripts + i];
            if (index == '\u0000' || (specials & 1 << i) != 0) continue;
            lowStart = this.addLowScriptRange(table, index, lowStart);
        }
        int skippedReserved = 0;
        if (specials == 0 && reorder[0] == 25 && !latinMustMove) {
            index = this.scriptsIndex[25];
            assert (index != '\u0000');
            int start = this.scriptStarts[index];
            assert (lowStart <= start);
            skippedReserved = start - lowStart;
            lowStart = start;
        }
        boolean hasReorderToEnd = false;
        int i2 = 0;
        while (i2 < length) {
            int index3;
            int script;
            if ((script = reorder[i2++]) == 103) {
                hasReorderToEnd = true;
                while (i2 < length) {
                    if ((script = reorder[--length]) == 103) {
                        throw new IllegalArgumentException("setReorderCodes(): duplicate UScript.UNKNOWN");
                    }
                    if (script == -1) {
                        throw new IllegalArgumentException("setReorderCodes(): UScript.DEFAULT together with other scripts");
                    }
                    index3 = this.getScriptIndex(script);
                    if (index3 == 0) continue;
                    if (table[index3] != 0) {
                        throw new IllegalArgumentException("setReorderCodes(): duplicate or equivalent script " + CollationData.scriptCodeString(script));
                    }
                    highLimit = this.addHighScriptRange(table, index3, highLimit);
                }
                break;
            }
            if (script == -1) {
                throw new IllegalArgumentException("setReorderCodes(): UScript.DEFAULT together with other scripts");
            }
            index3 = this.getScriptIndex(script);
            if (index3 == 0) continue;
            if (table[index3] != 0) {
                throw new IllegalArgumentException("setReorderCodes(): duplicate or equivalent script " + CollationData.scriptCodeString(script));
            }
            lowStart = this.addLowScriptRange(table, index3, lowStart);
        }
        for (i2 = 1; i2 < this.scriptStarts.length - 1; ++i2) {
            short leadByte = table[i2];
            if (leadByte != 0) continue;
            int start = this.scriptStarts[i2];
            if (!hasReorderToEnd && start > lowStart) {
                lowStart = start;
            }
            lowStart = this.addLowScriptRange(table, i2, lowStart);
        }
        if (lowStart > highLimit) {
            if (lowStart - (skippedReserved & 0xFF00) <= highLimit) {
                this.makeReorderRanges(reorder, true, ranges);
                return;
            }
            throw new ICUException("setReorderCodes(): reordering too many partial-primary-lead-byte scripts");
        }
        int offset = 0;
        int i3 = 1;
        while (true) {
            short newLeadByte;
            int nextOffset = offset;
            while (i3 < this.scriptStarts.length - 1 && ((newLeadByte = table[i3]) == 255 || (nextOffset = newLeadByte - (this.scriptStarts[i3] >> 8)) == offset)) {
                ++i3;
            }
            if (offset != 0 || i3 < this.scriptStarts.length - 1) {
                ranges.addElement(this.scriptStarts[i3] << 16 | offset & 0xFFFF);
            }
            if (i3 == this.scriptStarts.length - 1) break;
            offset = nextOffset;
            ++i3;
        }
    }

    private int addLowScriptRange(short[] table, int index, int lowStart) {
        char start = this.scriptStarts[index];
        if ((start & 0xFF) < (lowStart & 0xFF)) {
            lowStart += 256;
        }
        table[index] = (short)(lowStart >> 8);
        char limit = this.scriptStarts[index + 1];
        lowStart = (lowStart & 0xFF00) + ((limit & 0xFF00) - (start & 0xFF00)) | limit & 0xFF;
        return lowStart;
    }

    private int addHighScriptRange(short[] table, int index, int highLimit) {
        char limit = this.scriptStarts[index + 1];
        if ((limit & 0xFF) > (highLimit & 0xFF)) {
            highLimit -= 256;
        }
        char start = this.scriptStarts[index];
        highLimit = (highLimit & 0xFF00) - ((limit & 0xFF00) - (start & 0xFF00)) | start & 0xFF;
        table[index] = (short)(highLimit >> 8);
        return highLimit;
    }

    private static String scriptCodeString(int script) {
        return script < 4096 ? Integer.toString(script) : "0x" + Integer.toHexString(script);
    }
}

