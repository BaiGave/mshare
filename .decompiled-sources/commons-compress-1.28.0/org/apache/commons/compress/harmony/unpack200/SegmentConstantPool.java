/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200;

import java.util.List;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.unpack200.CpBands;
import org.apache.commons.compress.harmony.unpack200.SegmentConstantPoolArrayCache;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;
import org.apache.commons.compress.harmony.unpack200.bytecode.ConstantPoolEntry;

public class SegmentConstantPool {
    public static final int ALL = 0;
    public static final int UTF_8 = 1;
    public static final int CP_INT = 2;
    public static final int CP_FLOAT = 3;
    public static final int CP_LONG = 4;
    public static final int CP_DOUBLE = 5;
    public static final int CP_STRING = 6;
    public static final int CP_CLASS = 7;
    public static final int SIGNATURE = 8;
    public static final int CP_DESCR = 9;
    public static final int CP_FIELD = 10;
    public static final int CP_METHOD = 11;
    public static final int CP_IMETHOD = 12;
    protected static final String REGEX_MATCH_ALL = ".*";
    protected static final String INITSTRING = "<init>";
    protected static final String REGEX_MATCH_INIT = "^<init>.*";
    private final CpBands bands;
    private final SegmentConstantPoolArrayCache arrayCache = new SegmentConstantPoolArrayCache();

    protected static boolean regexMatches(String regexString, String compareString) {
        if (REGEX_MATCH_ALL.equals(regexString)) {
            return true;
        }
        if (REGEX_MATCH_INIT.equals(regexString)) {
            if (compareString.length() < INITSTRING.length()) {
                return false;
            }
            return INITSTRING.equals(compareString.substring(0, INITSTRING.length()));
        }
        throw new Error("regex trying to match a pattern I don't know: " + regexString);
    }

    static int toIndex(long index) throws Pack200Exception {
        if (index < 0L) {
            throw new Pack200Exception("Cannot have a negative index.");
        }
        return SegmentConstantPool.toIntExact(index);
    }

    static int toIntExact(long index) throws Pack200Exception {
        try {
            return Math.toIntExact(index);
        }
        catch (ArithmeticException e) {
            throw new Pack200Exception("index", e);
        }
    }

    public SegmentConstantPool(CpBands bands) {
        this.bands = bands;
    }

    public ConstantPoolEntry getClassPoolEntry(String name) throws Pack200Exception {
        int index = this.matchSpecificPoolEntryIndex(this.bands.getCpClass(), name, 0);
        return index == -1 ? null : this.getConstantPoolEntry(7, index);
    }

    public ConstantPoolEntry getClassSpecificPoolEntry(int cp, long desiredIndex, String desiredClassName) throws Pack200Exception {
        String[] array;
        switch (cp) {
            case 10: {
                array = this.bands.getCpFieldClass();
                break;
            }
            case 11: {
                array = this.bands.getCpMethodClass();
                break;
            }
            case 12: {
                array = this.bands.getCpIMethodClass();
                break;
            }
            default: {
                throw new Pack200Exception("Type is not supported yet: " + cp);
            }
        }
        return this.getConstantPoolEntry(cp, this.matchSpecificPoolEntryIndex(array, desiredClassName, SegmentConstantPool.toIndex(desiredIndex)));
    }

    public ConstantPoolEntry getConstantPoolEntry(int type, long index) throws Pack200Exception {
        if (index == -1L) {
            return null;
        }
        int actualIndex = SegmentConstantPool.toIndex(index);
        switch (type) {
            case 1: {
                return this.bands.cpUTF8Value(actualIndex);
            }
            case 2: {
                return this.bands.cpIntegerValue(actualIndex);
            }
            case 3: {
                return this.bands.cpFloatValue(actualIndex);
            }
            case 4: {
                return this.bands.cpLongValue(actualIndex);
            }
            case 5: {
                return this.bands.cpDoubleValue(actualIndex);
            }
            case 6: {
                return this.bands.cpStringValue(actualIndex);
            }
            case 7: {
                return this.bands.cpClassValue(actualIndex);
            }
            case 8: {
                throw new Pack200Exception("Type SIGNATURE is not supported yet: 8");
            }
            case 9: {
                throw new Pack200Exception("Type CP_DESCR is not supported yet: 9");
            }
            case 10: {
                return this.bands.cpFieldValue(actualIndex);
            }
            case 11: {
                return this.bands.cpMethodValue(actualIndex);
            }
            case 12: {
                return this.bands.cpIMethodValue(actualIndex);
            }
        }
        throw new Pack200Exception("Type is not supported yet: " + type);
    }

    public ConstantPoolEntry getInitMethodPoolEntry(int cp, long value, String desiredClassName) throws Pack200Exception {
        if (cp != 11) {
            throw new Pack200Exception("Nothing but CP_METHOD can be an <init>");
        }
        int realIndex = this.matchSpecificPoolEntryIndex(this.bands.getCpMethodClass(), this.bands.getCpMethodDescriptor(), desiredClassName, REGEX_MATCH_INIT, SegmentConstantPool.toIndex(value));
        return this.getConstantPoolEntry(cp, realIndex);
    }

    public ClassFileEntry getValue(int cp, long longIndex) throws Pack200Exception {
        int index = (int)longIndex;
        if (index == -1) {
            return null;
        }
        if (index < 0) {
            throw new Pack200Exception("Cannot have a negative range");
        }
        switch (cp) {
            case 1: {
                return this.bands.cpUTF8Value(index);
            }
            case 2: {
                return this.bands.cpIntegerValue(index);
            }
            case 3: {
                return this.bands.cpFloatValue(index);
            }
            case 4: {
                return this.bands.cpLongValue(index);
            }
            case 5: {
                return this.bands.cpDoubleValue(index);
            }
            case 6: {
                return this.bands.cpStringValue(index);
            }
            case 7: {
                return this.bands.cpClassValue(index);
            }
            case 8: {
                return this.bands.cpSignatureValue(index);
            }
            case 9: {
                return this.bands.cpNameAndTypeValue(index);
            }
        }
        throw new Error("Tried to get a value I don't know about: " + cp);
    }

    protected int matchSpecificPoolEntryIndex(String[] nameArray, String compareString, int desiredIndex) {
        return this.matchSpecificPoolEntryIndex(nameArray, nameArray, compareString, REGEX_MATCH_ALL, desiredIndex);
    }

    protected int matchSpecificPoolEntryIndex(String[] primaryArray, String[] secondaryArray, String primaryCompareString, String secondaryCompareRegex, int desiredIndex) {
        int instanceCount = -1;
        List<Integer> indexList = this.arrayCache.indexesForArrayKey(primaryArray, primaryCompareString);
        if (indexList.isEmpty()) {
            return -1;
        }
        for (Integer element : indexList) {
            int arrayIndex = element;
            if (!SegmentConstantPool.regexMatches(secondaryCompareRegex, secondaryArray[arrayIndex]) || ++instanceCount != desiredIndex) continue;
            return arrayIndex;
        }
        return -1;
    }
}

