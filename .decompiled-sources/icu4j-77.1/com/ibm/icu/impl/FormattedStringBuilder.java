/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl;

import com.ibm.icu.text.NumberFormat;
import java.text.Format;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FormattedStringBuilder
implements CharSequence,
Appendable {
    public static final FormattedStringBuilder EMPTY = new FormattedStringBuilder();
    char[] chars;
    Object[] fields;
    int zero;
    int length;
    int appendOffset = 0;
    Object appendableField = null;
    private static final Map<Object, Character> fieldToDebugChar = new HashMap<Object, Character>();

    public static Format.Field unwrapField(Object field) {
        if (field == null) {
            return null;
        }
        if (field instanceof FieldWrapper) {
            return ((FieldWrapper)field).unwrap();
        }
        if (field instanceof Format.Field) {
            return (Format.Field)field;
        }
        throw new AssertionError((Object)("Not a field: " + field));
    }

    public FormattedStringBuilder() {
        this(40);
    }

    public FormattedStringBuilder(int capacity) {
        this.chars = new char[capacity];
        this.fields = new Object[capacity];
        this.zero = capacity / 2;
        this.length = 0;
    }

    public FormattedStringBuilder(FormattedStringBuilder source) {
        this.copyFrom(source);
    }

    public void copyFrom(FormattedStringBuilder source) {
        this.chars = Arrays.copyOf(source.chars, source.chars.length);
        this.fields = Arrays.copyOf(source.fields, source.fields.length);
        this.zero = source.zero;
        this.length = source.length;
    }

    @Override
    public int length() {
        return this.length;
    }

    public int codePointCount() {
        return Character.codePointCount(this, 0, this.length());
    }

    @Override
    public char charAt(int index) {
        assert (index >= 0);
        assert (index < this.length);
        return this.chars[this.zero + index];
    }

    public Object fieldAt(int index) {
        assert (index >= 0);
        assert (index < this.length);
        return this.fields[this.zero + index];
    }

    public int getFirstCodePoint() {
        if (this.length == 0) {
            return -1;
        }
        return Character.codePointAt(this.chars, this.zero, this.zero + this.length);
    }

    public int getLastCodePoint() {
        if (this.length == 0) {
            return -1;
        }
        return Character.codePointBefore(this.chars, this.zero + this.length, this.zero);
    }

    public int codePointAt(int index) {
        return Character.codePointAt(this.chars, this.zero + index, this.zero + this.length);
    }

    public int codePointBefore(int index) {
        return Character.codePointBefore(this.chars, this.zero + index, this.zero);
    }

    public FormattedStringBuilder clear() {
        this.zero = this.getCapacity() / 2;
        this.length = 0;
        return this;
    }

    public void setAppendIndex(int index) {
        this.appendOffset = this.length - index;
    }

    public int appendChar16(char codeUnit, Object field) {
        return this.insertChar16(this.length - this.appendOffset, codeUnit, field);
    }

    public int insertChar16(int index, char codeUnit, Object field) {
        int count = 1;
        int position = this.prepareForInsert(index, count);
        this.chars[position] = codeUnit;
        this.fields[position] = field;
        return count;
    }

    public int appendCodePoint(int codePoint, Object field) {
        return this.insertCodePoint(this.length - this.appendOffset, codePoint, field);
    }

    public int insertCodePoint(int index, int codePoint, Object field) {
        int count = Character.charCount(codePoint);
        int position = this.prepareForInsert(index, count);
        Character.toChars(codePoint, this.chars, position);
        this.fields[position] = field;
        if (count == 2) {
            this.fields[position + 1] = field;
        }
        return count;
    }

    public int append(CharSequence sequence, Object field) {
        return this.insert(this.length - this.appendOffset, sequence, field);
    }

    public int insert(int index, CharSequence sequence, Object field) {
        if (sequence.length() == 0) {
            return 0;
        }
        if (sequence.length() == 1) {
            return this.insertCodePoint(index, sequence.charAt(0), field);
        }
        return this.insert(index, sequence, 0, sequence.length(), field);
    }

    public int insert(int index, CharSequence sequence, int start, int end, Object field) {
        int count = end - start;
        int position = this.prepareForInsert(index, count);
        for (int i = 0; i < count; ++i) {
            this.chars[position + i] = sequence.charAt(start + i);
            this.fields[position + i] = field;
        }
        return count;
    }

    public int splice(int startThis, int endThis, CharSequence sequence, int startOther, int endOther, Object field) {
        int otherLength = endOther - startOther;
        int thisLength = endThis - startThis;
        int count = otherLength - thisLength;
        int position = count > 0 ? this.prepareForInsert(startThis, count) : this.remove(startThis, -count);
        for (int i = 0; i < otherLength; ++i) {
            this.chars[position + i] = sequence.charAt(startOther + i);
            this.fields[position + i] = field;
        }
        return count;
    }

    public int append(char[] chars, Object[] fields) {
        return this.insert(this.length - this.appendOffset, chars, fields);
    }

    public int insert(int index, char[] chars, Object[] fields) {
        assert (fields == null || chars.length == fields.length);
        int count = chars.length;
        if (count == 0) {
            return 0;
        }
        int position = this.prepareForInsert(index, count);
        for (int i = 0; i < count; ++i) {
            this.chars[position + i] = chars[i];
            this.fields[position + i] = fields == null ? null : fields[i];
        }
        return count;
    }

    public int append(FormattedStringBuilder other) {
        return this.insert(this.length - this.appendOffset, other);
    }

    public int insert(int index, FormattedStringBuilder other) {
        if (this == other) {
            throw new IllegalArgumentException("Cannot call insert/append on myself");
        }
        int count = other.length;
        if (count == 0) {
            return 0;
        }
        int position = this.prepareForInsert(index, count);
        for (int i = 0; i < count; ++i) {
            this.chars[position + i] = other.charAt(i);
            this.fields[position + i] = other.fieldAt(i);
        }
        return count;
    }

    private int prepareForInsert(int index, int count) {
        if (index == -1) {
            index = this.length;
        }
        if (index == 0 && this.zero - count >= 0) {
            this.zero -= count;
            this.length += count;
            return this.zero;
        }
        if (index == this.length && this.zero + this.length + count < this.getCapacity()) {
            this.length += count;
            return this.zero + this.length - count;
        }
        return this.prepareForInsertHelper(index, count);
    }

    private int prepareForInsertHelper(int index, int count) {
        int oldCapacity = this.getCapacity();
        int oldZero = this.zero;
        char[] oldChars = this.chars;
        Object[] oldFields = this.fields;
        if (this.length + count > oldCapacity) {
            int newCapacity = (this.length + count) * 2;
            int newZero = newCapacity / 2 - (this.length + count) / 2;
            char[] newChars = new char[newCapacity];
            Object[] newFields = new Object[newCapacity];
            System.arraycopy(oldChars, oldZero, newChars, newZero, index);
            System.arraycopy(oldChars, oldZero + index, newChars, newZero + index + count, this.length - index);
            System.arraycopy(oldFields, oldZero, newFields, newZero, index);
            System.arraycopy(oldFields, oldZero + index, newFields, newZero + index + count, this.length - index);
            this.chars = newChars;
            this.fields = newFields;
            this.zero = newZero;
            this.length += count;
        } else {
            int newZero = oldCapacity / 2 - (this.length + count) / 2;
            System.arraycopy(oldChars, oldZero, oldChars, newZero, this.length);
            System.arraycopy(oldChars, newZero + index, oldChars, newZero + index + count, this.length - index);
            System.arraycopy(oldFields, oldZero, oldFields, newZero, this.length);
            System.arraycopy(oldFields, newZero + index, oldFields, newZero + index + count, this.length - index);
            this.zero = newZero;
            this.length += count;
        }
        return this.zero + index;
    }

    private int remove(int index, int count) {
        int position = index + this.zero;
        System.arraycopy(this.chars, position + count, this.chars, position, this.length - index - count);
        System.arraycopy(this.fields, position + count, this.fields, position, this.length - index - count);
        this.length -= count;
        return position;
    }

    private int getCapacity() {
        return this.chars.length;
    }

    @Override
    @Deprecated
    public CharSequence subSequence(int start, int end) {
        assert (start >= 0);
        assert (end <= this.length);
        assert (end >= start);
        FormattedStringBuilder other = new FormattedStringBuilder(this);
        other.zero = this.zero + start;
        other.length = end - start;
        return other;
    }

    public String subString(int start, int end) {
        if (start < 0 || end > this.length || end < start) {
            throw new IndexOutOfBoundsException();
        }
        return new String(this.chars, start + this.zero, end - start);
    }

    @Override
    public String toString() {
        return new String(this.chars, this.zero, this.length);
    }

    public String toDebugString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<FormattedStringBuilder [");
        sb.append(this.toString());
        sb.append("] [");
        for (int i = this.zero; i < this.zero + this.length; ++i) {
            if (this.fields[i] == null) {
                sb.append('n');
                continue;
            }
            if (fieldToDebugChar.containsKey(this.fields[i])) {
                sb.append(fieldToDebugChar.get(this.fields[i]));
                continue;
            }
            sb.append('?');
        }
        sb.append("]>");
        return sb.toString();
    }

    public char[] toCharArray() {
        return Arrays.copyOfRange(this.chars, this.zero, this.zero + this.length);
    }

    public Object[] toFieldArray() {
        return Arrays.copyOfRange(this.fields, this.zero, this.zero + this.length);
    }

    public void setAppendableField(Object field) {
        this.appendableField = field;
    }

    @Override
    public Appendable append(CharSequence csq) {
        assert (this.appendableField != null);
        this.insert(this.length - this.appendOffset, csq, this.appendableField);
        return this;
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) {
        assert (this.appendableField != null);
        this.insert(this.length - this.appendOffset, csq, start, end, this.appendableField);
        return this;
    }

    @Override
    public Appendable append(char c) {
        assert (this.appendableField != null);
        this.insertChar16(this.length - this.appendOffset, c, this.appendableField);
        return this;
    }

    public boolean contentEquals(char[] chars, Object[] fields) {
        if (chars.length != this.length) {
            return false;
        }
        if (fields.length != this.length) {
            return false;
        }
        for (int i = 0; i < this.length; ++i) {
            if (this.chars[this.zero + i] != chars[i]) {
                return false;
            }
            if (FormattedStringBuilder.unwrapField(this.fields[this.zero + i]) == FormattedStringBuilder.unwrapField(fields[i])) continue;
            return false;
        }
        return true;
    }

    public boolean contentEquals(FormattedStringBuilder other) {
        if (this.length != other.length) {
            return false;
        }
        for (int i = 0; i < this.length; ++i) {
            if (this.charAt(i) != other.charAt(i)) {
                return false;
            }
            if (FormattedStringBuilder.unwrapField(this.fieldAt(i)) == FormattedStringBuilder.unwrapField(other.fieldAt(i))) continue;
            return false;
        }
        return true;
    }

    public int hashCode() {
        throw new UnsupportedOperationException("Don't call #hashCode() or #equals() on a mutable.");
    }

    public boolean equals(Object other) {
        throw new UnsupportedOperationException("Don't call #hashCode() or #equals() on a mutable.");
    }

    static {
        fieldToDebugChar.put(NumberFormat.Field.SIGN, Character.valueOf('-'));
        fieldToDebugChar.put(NumberFormat.Field.INTEGER, Character.valueOf('i'));
        fieldToDebugChar.put(NumberFormat.Field.FRACTION, Character.valueOf('f'));
        fieldToDebugChar.put(NumberFormat.Field.EXPONENT, Character.valueOf('e'));
        fieldToDebugChar.put(NumberFormat.Field.EXPONENT_SIGN, Character.valueOf('+'));
        fieldToDebugChar.put(NumberFormat.Field.EXPONENT_SYMBOL, Character.valueOf('E'));
        fieldToDebugChar.put(NumberFormat.Field.DECIMAL_SEPARATOR, Character.valueOf('.'));
        fieldToDebugChar.put(NumberFormat.Field.GROUPING_SEPARATOR, Character.valueOf(','));
        fieldToDebugChar.put(NumberFormat.Field.PERCENT, Character.valueOf('%'));
        fieldToDebugChar.put(NumberFormat.Field.PERMILLE, Character.valueOf('\u2030'));
        fieldToDebugChar.put(NumberFormat.Field.CURRENCY, Character.valueOf('$'));
        fieldToDebugChar.put(NumberFormat.Field.MEASURE_UNIT, Character.valueOf('u'));
        fieldToDebugChar.put(NumberFormat.Field.COMPACT, Character.valueOf('C'));
    }

    public static interface FieldWrapper {
        public Format.Field unwrap();
    }
}

