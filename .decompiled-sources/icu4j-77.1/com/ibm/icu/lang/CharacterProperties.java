/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.lang;

import com.ibm.icu.impl.CharacterPropertiesImpl;
import com.ibm.icu.impl.EmojiProps;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.UnicodeSet;
import com.ibm.icu.util.CodePointMap;
import com.ibm.icu.util.CodePointTrie;
import com.ibm.icu.util.MutableCodePointTrie;

public final class CharacterProperties {
    private static final UnicodeSet[] sets = new UnicodeSet[76];
    private static final CodePointMap[] maps = new CodePointMap[27];

    private CharacterProperties() {
    }

    private static UnicodeSet makeSet(int property) {
        UnicodeSet set = new UnicodeSet();
        if (65 <= property && property <= 71) {
            EmojiProps.INSTANCE.addStrings(property, set);
            if (property != 65 && property != 71) {
                return set.freeze();
            }
        }
        UnicodeSet inclusions = CharacterPropertiesImpl.getInclusionsForProperty(property);
        int numRanges = inclusions.getRangeCount();
        int startHasProperty = -1;
        for (int i = 0; i < numRanges; ++i) {
            int rangeEnd = inclusions.getRangeEnd(i);
            for (int c = inclusions.getRangeStart(i); c <= rangeEnd; ++c) {
                if (UCharacter.hasBinaryProperty(c, property)) {
                    if (startHasProperty >= 0) continue;
                    startHasProperty = c;
                    continue;
                }
                if (startHasProperty < 0) continue;
                set.add(startHasProperty, c - 1);
                startHasProperty = -1;
            }
        }
        if (startHasProperty >= 0) {
            set.add(startHasProperty, 0x10FFFF);
        }
        return set.freeze();
    }

    private static CodePointMap makeMap(int property) {
        int nullValue = property == 4106 ? 103 : 0;
        MutableCodePointTrie mutableTrie = new MutableCodePointTrie(nullValue, nullValue);
        UnicodeSet inclusions = CharacterPropertiesImpl.getInclusionsForProperty(property);
        int numRanges = inclusions.getRangeCount();
        int start = 0;
        int value = nullValue;
        for (int i = 0; i < numRanges; ++i) {
            int rangeEnd = inclusions.getRangeEnd(i);
            for (int c = inclusions.getRangeStart(i); c <= rangeEnd; ++c) {
                int nextValue = UCharacter.getIntPropertyValue(c, property);
                if (value == nextValue) continue;
                if (value != nullValue) {
                    mutableTrie.setRange(start, c - 1, value);
                }
                start = c;
                value = nextValue;
            }
        }
        if (value != 0) {
            mutableTrie.setRange(start, 0x10FFFF, value);
        }
        CodePointTrie.Type type = property == 4096 || property == 4101 ? CodePointTrie.Type.FAST : CodePointTrie.Type.SMALL;
        int max = UCharacter.getIntPropertyMaxValue(property);
        CodePointTrie.ValueWidth valueWidth = max <= 255 ? CodePointTrie.ValueWidth.BITS_8 : (max <= 65535 ? CodePointTrie.ValueWidth.BITS_16 : CodePointTrie.ValueWidth.BITS_32);
        return mutableTrie.buildImmutable(type, valueWidth);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final UnicodeSet getBinaryPropertySet(int property) {
        if (property < 0 || 76 <= property) {
            throw new IllegalArgumentException("" + property + " is not a constant for a UProperty binary property");
        }
        UnicodeSet[] unicodeSetArray = sets;
        synchronized (sets) {
            UnicodeSet set = sets[property];
            if (set == null) {
                CharacterProperties.sets[property] = set = CharacterProperties.makeSet(property);
            }
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return set;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final CodePointMap getIntPropertyMap(int property) {
        if (property < 4096 || 4123 <= property) {
            throw new IllegalArgumentException("" + property + " is not a constant for a UProperty int property");
        }
        CodePointMap[] codePointMapArray = maps;
        synchronized (maps) {
            CodePointMap map = maps[property - 4096];
            if (map == null) {
                CharacterProperties.maps[property - 4096] = map = CharacterProperties.makeMap(property);
            }
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return map;
        }
    }
}

