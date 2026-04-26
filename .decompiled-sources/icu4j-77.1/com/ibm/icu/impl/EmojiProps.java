/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl;

import com.ibm.icu.impl.ICUBinary;
import com.ibm.icu.text.UnicodeSet;
import com.ibm.icu.util.BytesTrie;
import com.ibm.icu.util.CharsTrie;
import com.ibm.icu.util.CodePointMap;
import com.ibm.icu.util.CodePointTrie;
import com.ibm.icu.util.ICUUncheckedIOException;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class EmojiProps {
    private static final IsAcceptable IS_ACCEPTABLE = new IsAcceptable();
    private static final int DATA_FORMAT = 1164799850;
    private static final int IX_CPTRIE_OFFSET = 0;
    private static final int IX_BASIC_EMOJI_TRIE_OFFSET = 4;
    private static final int IX_RGI_EMOJI_ZWJ_SEQUENCE_TRIE_OFFSET = 9;
    private static final int BIT_EMOJI = 0;
    private static final int BIT_EMOJI_PRESENTATION = 1;
    private static final int BIT_EMOJI_MODIFIER = 2;
    private static final int BIT_EMOJI_MODIFIER_BASE = 3;
    private static final int BIT_EMOJI_COMPONENT = 4;
    private static final int BIT_EXTENDED_PICTOGRAPHIC = 5;
    private static final int BIT_BASIC_EMOJI = 6;
    public static final EmojiProps INSTANCE = new EmojiProps();
    private CodePointTrie.Fast8 cpTrie = null;
    private String[] stringTries = new String[6];
    private static final byte[] bitFlags = new byte[]{0, 1, 2, 3, 4, -1, -1, 5, 6, -1, -1, -1, -1, -1, 6};

    private static int getStringTrieIndex(int i) {
        return i - 4;
    }

    private EmojiProps() {
        ByteBuffer bytes = ICUBinary.getRequiredData("uemoji.icu");
        try {
            int i;
            ICUBinary.readHeaderAndDataVersion(bytes, 1164799850, IS_ACCEPTABLE);
            int startPos = bytes.position();
            int cpTrieOffset = bytes.getInt();
            int indexesLength = cpTrieOffset / 4;
            if (indexesLength <= 9) {
                throw new ICUUncheckedIOException("Emoji properties data: not enough indexes");
            }
            int[] inIndexes = new int[indexesLength];
            inIndexes[0] = cpTrieOffset;
            for (i = 1; i < indexesLength; ++i) {
                inIndexes[i] = bytes.getInt();
            }
            i = 0;
            int offset = inIndexes[i++];
            int nextOffset = inIndexes[i];
            this.cpTrie = CodePointTrie.Fast8.fromBinary(bytes);
            int pos = bytes.position() - startPos;
            assert (nextOffset >= pos);
            ICUBinary.skipBytes(bytes, nextOffset - pos);
            offset = nextOffset;
            nextOffset = inIndexes[4];
            ICUBinary.skipBytes(bytes, nextOffset - offset);
            for (i = 4; i <= 9; ++i) {
                nextOffset = inIndexes[i + 1];
                offset = inIndexes[i];
                if (nextOffset <= offset) continue;
                this.stringTries[EmojiProps.getStringTrieIndex((int)i)] = ICUBinary.getString(bytes, (nextOffset - offset) / 2, 0);
            }
        }
        catch (IOException e) {
            throw new ICUUncheckedIOException(e);
        }
    }

    public UnicodeSet addPropertyStarts(UnicodeSet set) {
        CodePointMap.Range range = new CodePointMap.Range();
        int start = 0;
        while (this.cpTrie.getRange(start, null, range)) {
            set.add(start);
            start = range.getEnd() + 1;
        }
        return set;
    }

    public boolean hasBinaryProperty(int c, int which) {
        if (which < 57 || 71 < which) {
            return false;
        }
        byte bit = bitFlags[which - 57];
        if (bit < 0) {
            return false;
        }
        int bits = this.cpTrie.get(c);
        return (bits >> bit & 1) != 0;
    }

    public boolean hasBinaryProperty(CharSequence s, int which) {
        int length = s.length();
        if (length == 0) {
            return false;
        }
        if (which < 65 || 71 < which) {
            return false;
        }
        int firstProp = which;
        int lastProp = which;
        if (which == 71) {
            firstProp = 65;
            lastProp = 70;
        }
        for (int prop = firstProp; prop <= lastProp; ++prop) {
            CharsTrie trie;
            BytesTrie.Result result;
            String trieUChars = this.stringTries[prop - 65];
            if (trieUChars == null || !(result = (trie = new CharsTrie(trieUChars, 0)).next(s, 0, length)).hasValue()) continue;
            return true;
        }
        return false;
    }

    public void addStrings(int which, UnicodeSet set) {
        if (which < 65 || 71 < which) {
            return;
        }
        int firstProp = which;
        int lastProp = which;
        if (which == 71) {
            firstProp = 65;
            lastProp = 70;
        }
        for (int prop = firstProp; prop <= lastProp; ++prop) {
            String trieUChars = this.stringTries[prop - 65];
            if (trieUChars == null) continue;
            CharsTrie trie = new CharsTrie(trieUChars, 0);
            for (CharsTrie.Entry entry : trie) {
                set.add(entry.chars);
            }
        }
    }

    private static final class IsAcceptable
    implements ICUBinary.Authenticate {
        private IsAcceptable() {
        }

        @Override
        public boolean isDataVersionAcceptable(byte[] version) {
            return version[0] == 1;
        }
    }
}

