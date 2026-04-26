/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl;

import com.ibm.icu.impl.ImmutableEntry;
import com.ibm.icu.impl.Utility;
import com.ibm.icu.text.StringTransform;
import com.ibm.icu.text.UTF16;
import com.ibm.icu.text.UnicodeSet;
import com.ibm.icu.text.UnicodeSetIterator;
import com.ibm.icu.util.Freezable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public final class UnicodeMap<T>
implements Cloneable,
Freezable<UnicodeMap<T>>,
StringTransform,
Iterable<String> {
    static final boolean ASSERTIONS = false;
    static final long GROWTH_PERCENT = 200L;
    static final long GROWTH_GAP = 10L;
    private int length;
    private int[] transitions;
    T[] values;
    private LinkedHashSet<T> availableValues = new LinkedHashSet();
    private transient boolean staleAvailableValues;
    private transient boolean errorOnReset;
    private volatile transient boolean locked;
    private int lastIndex;
    private TreeMap<String, T> stringMap;
    static final boolean DEBUG_WRITE = false;

    public UnicodeMap() {
        this.clear();
    }

    public UnicodeMap(UnicodeMap other) {
        this.clear();
        this.putAll(other);
    }

    public UnicodeMap<T> clear() {
        if (this.locked) {
            throw new UnsupportedOperationException("Attempt to modify locked object");
        }
        this.length = 2;
        this.transitions = new int[]{0, 0x110000, 0, 0, 0, 0, 0, 0, 0, 0};
        this.values = new Object[10];
        this.availableValues.clear();
        this.staleAvailableValues = false;
        this.errorOnReset = false;
        this.lastIndex = 0;
        this.stringMap = null;
        return this;
    }

    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        try {
            UnicodeMap that = (UnicodeMap)other;
            if (this.length != that.length) {
                return false;
            }
            for (int i = 0; i < this.length - 1; ++i) {
                if (this.transitions[i] != that.transitions[i]) {
                    return false;
                }
                if (UnicodeMap.areEqual(this.values[i], that.values[i])) continue;
                return false;
            }
            return true;
        }
        catch (ClassCastException e) {
            return false;
        }
    }

    public static boolean areEqual(Object a, Object b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }

    public int hashCode() {
        int result = this.length;
        for (int i = 0; i < this.length - 1; ++i) {
            result = 37 * result + this.transitions[i];
            result = 37 * result;
            if (this.values[i] == null) continue;
            result += this.values[i].hashCode();
        }
        if (this.stringMap != null) {
            result = 37 * result + this.stringMap.hashCode();
        }
        return result;
    }

    @Override
    public UnicodeMap<T> cloneAsThawed() {
        UnicodeMap<T> that = new UnicodeMap<T>();
        that.length = this.length;
        that.transitions = (int[])this.transitions.clone();
        that.values = (Object[])this.values.clone();
        that.availableValues = new LinkedHashSet<T>(this.availableValues);
        that.locked = false;
        that.stringMap = this.stringMap == null ? null : (TreeMap)this.stringMap.clone();
        return that;
    }

    void _checkInvariants() {
        int i;
        if (this.length < 2 || this.length > this.transitions.length || this.transitions.length != this.values.length) {
            throw new IllegalArgumentException("Invariant failed: Lengths bad");
        }
        for (i = 1; i < this.length - 1; ++i) {
            if (!UnicodeMap.areEqual(this.values[i - 1], this.values[i])) continue;
            throw new IllegalArgumentException("Invariant failed: values shared at \t" + Utility.hex(i - 1) + ": <" + this.values[i - 1] + ">\t" + Utility.hex(i) + ": <" + this.values[i] + ">");
        }
        if (this.transitions[0] != 0 || this.transitions[this.length - 1] != 0x110000) {
            throw new IllegalArgumentException("Invariant failed: bounds set wrong");
        }
        for (i = 1; i < this.length - 1; ++i) {
            if (this.transitions[i - 1] < this.transitions[i]) continue;
            throw new IllegalArgumentException("Invariant failed: not monotonic\t" + Utility.hex(i - 1) + ": " + this.transitions[i - 1] + "\t" + Utility.hex(i) + ": " + this.transitions[i]);
        }
    }

    private int _findIndex(int c) {
        int lo = 0;
        int hi = this.length - 1;
        int i = lo + hi >>> 1;
        while (i != lo) {
            if (c < this.transitions[i]) {
                hi = i;
            } else {
                lo = i;
            }
            i = lo + hi >>> 1;
        }
        return lo;
    }

    private void _checkFind(int codepoint, int value) {
        int other = this.__findIndex(codepoint);
        if (other != value) {
            throw new IllegalArgumentException("Invariant failed: binary search\t" + Utility.hex(codepoint) + ": " + value + "\tshould be: " + other);
        }
    }

    private int __findIndex(int codepoint) {
        for (int i = this.length - 1; i > 0; --i) {
            if (this.transitions[i] > codepoint) continue;
            return i;
        }
        return 0;
    }

    private void _removeAt(int index, int count) {
        for (int i = index + count; i < this.length; ++i) {
            this.transitions[i - count] = this.transitions[i];
            this.values[i - count] = this.values[i];
        }
        this.length -= count;
    }

    private void _insertGapAt(int index, int count) {
        int newLength = this.length + count;
        int[] oldtransitions = this.transitions;
        T[] oldvalues = this.values;
        if (newLength > this.transitions.length) {
            int allocation = (int)(10L + (long)newLength * 200L / 100L);
            this.transitions = new int[allocation];
            this.values = new Object[allocation];
            for (int i = 0; i < index; ++i) {
                this.transitions[i] = oldtransitions[i];
                this.values[i] = oldvalues[i];
            }
        }
        for (int i = this.length - 1; i >= index; --i) {
            this.transitions[i + count] = oldtransitions[i];
            this.values[i + count] = oldvalues[i];
        }
        this.length = newLength;
    }

    private UnicodeMap _put(int codepoint, T value) {
        int baseIndex = this.transitions[this.lastIndex] <= codepoint && codepoint < this.transitions[this.lastIndex + 1] ? this.lastIndex : this._findIndex(codepoint);
        int limitIndex = baseIndex + 1;
        if (UnicodeMap.areEqual(this.values[baseIndex], value)) {
            return this;
        }
        if (this.locked) {
            throw new UnsupportedOperationException("Attempt to modify locked object");
        }
        if (this.errorOnReset && this.values[baseIndex] != null) {
            throw new UnsupportedOperationException("Attempt to reset value for " + Utility.hex(codepoint) + " when that is disallowed. Old: " + this.values[baseIndex] + "; New: " + value);
        }
        this.staleAvailableValues = true;
        this.availableValues.add(value);
        int baseCP = this.transitions[baseIndex];
        int limitCP = this.transitions[limitIndex];
        if (baseCP == codepoint) {
            boolean connectsWithPrevious;
            boolean bl = connectsWithPrevious = baseIndex != 0 && UnicodeMap.areEqual(value, this.values[baseIndex - 1]);
            if (limitCP == codepoint + 1) {
                boolean connectsWithFollowing;
                boolean bl2 = connectsWithFollowing = baseIndex < this.length - 2 && UnicodeMap.areEqual(value, this.values[limitIndex]);
                if (connectsWithPrevious) {
                    if (connectsWithFollowing) {
                        this._removeAt(baseIndex, 2);
                    } else {
                        this._removeAt(baseIndex, 1);
                    }
                    --baseIndex;
                } else if (connectsWithFollowing) {
                    this._removeAt(baseIndex, 1);
                    this.transitions[baseIndex] = codepoint;
                } else {
                    this.values[baseIndex] = value;
                }
            } else if (connectsWithPrevious) {
                int n = baseIndex;
                this.transitions[n] = this.transitions[n] + 1;
            } else {
                this.transitions[baseIndex] = codepoint + 1;
                this._insertGapAt(baseIndex, 1);
                this.values[baseIndex] = value;
                this.transitions[baseIndex] = codepoint;
            }
        } else if (limitCP == codepoint + 1) {
            boolean connectsWithFollowing;
            boolean bl = connectsWithFollowing = baseIndex < this.length - 2 && UnicodeMap.areEqual(value, this.values[limitIndex]);
            if (connectsWithFollowing) {
                int n = limitIndex;
                this.transitions[n] = this.transitions[n] - 1;
                return this;
            }
            this._insertGapAt(limitIndex, 1);
            this.transitions[limitIndex] = codepoint;
            this.values[limitIndex] = value;
        } else {
            this._insertGapAt(++baseIndex, 2);
            this.transitions[baseIndex] = codepoint;
            this.values[baseIndex] = value;
            this.transitions[baseIndex + 1] = codepoint + 1;
            this.values[baseIndex + 1] = this.values[baseIndex - 1];
        }
        this.lastIndex = baseIndex;
        return this;
    }

    private UnicodeMap _putAll(int startCodePoint, int endCodePoint, T value) {
        for (int i = startCodePoint; i <= endCodePoint; ++i) {
            this._put(i, value);
        }
        return this;
    }

    public UnicodeMap<T> put(int codepoint, T value) {
        if (codepoint < 0 || codepoint > 0x10FFFF) {
            throw new IllegalArgumentException("Codepoint out of range: " + codepoint);
        }
        this._put(codepoint, value);
        return this;
    }

    public UnicodeMap<T> put(String string, T value) {
        int v = UnicodeSet.getSingleCodePoint(string);
        if (v == Integer.MAX_VALUE) {
            if (this.locked) {
                throw new UnsupportedOperationException("Attempt to modify locked object");
            }
            if (value != null) {
                if (this.stringMap == null) {
                    this.stringMap = new TreeMap();
                }
                this.stringMap.put(string, value);
                this.staleAvailableValues = true;
            } else if (this.stringMap != null && this.stringMap.remove(string) != null) {
                this.staleAvailableValues = true;
            }
            return this;
        }
        return this.put(v, value);
    }

    public UnicodeMap<T> putAll(UnicodeSet codepoints, T value) {
        UnicodeSetIterator it = new UnicodeSetIterator(codepoints);
        while (it.nextRange()) {
            if (it.string == null) {
                this._putAll(it.codepoint, it.codepointEnd, value);
                continue;
            }
            this.put(it.string, value);
        }
        return this;
    }

    public UnicodeMap<T> putAll(int startCodePoint, int endCodePoint, T value) {
        if (this.locked) {
            throw new UnsupportedOperationException("Attempt to modify locked object");
        }
        if (startCodePoint < 0 || endCodePoint > 0x10FFFF) {
            throw new IllegalArgumentException("Codepoint out of range: " + Utility.hex(startCodePoint) + ".." + Utility.hex(endCodePoint));
        }
        return this._putAll(startCodePoint, endCodePoint, value);
    }

    public UnicodeMap<T> putAll(UnicodeMap<T> unicodeMap) {
        for (int i = 0; i < unicodeMap.length; ++i) {
            T value = unicodeMap.values[i];
            if (value == null) continue;
            this._putAll(unicodeMap.transitions[i], unicodeMap.transitions[i + 1] - 1, value);
        }
        if (unicodeMap.stringMap != null && !unicodeMap.stringMap.isEmpty()) {
            if (this.stringMap == null) {
                this.stringMap = new TreeMap();
            }
            this.stringMap.putAll(unicodeMap.stringMap);
        }
        return this;
    }

    public UnicodeMap<T> putAllFiltered(UnicodeMap<T> prop, UnicodeSet filter) {
        UnicodeSetIterator it = new UnicodeSetIterator(filter);
        while (it.next()) {
            T value;
            if (it.codepoint == -1 || (value = prop.getValue(it.codepoint)) == null) continue;
            this._put(it.codepoint, value);
        }
        for (String key : filter.strings()) {
            T value = prop.get(key);
            if (value == null) continue;
            this.put(key, value);
        }
        return this;
    }

    public UnicodeMap<T> setMissing(T value) {
        if (!this.getAvailableValues().contains(value)) {
            this.staleAvailableValues = true;
            this.availableValues.add(value);
            for (int i = 0; i < this.length; ++i) {
                if (this.values[i] != null) continue;
                this.values[i] = value;
            }
            return this;
        }
        return this.putAll(this.keySet(null), value);
    }

    public UnicodeSet keySet(T value, UnicodeSet result) {
        if (result == null) {
            result = new UnicodeSet();
        }
        for (int i = 0; i < this.length - 1; ++i) {
            if (!UnicodeMap.areEqual(value, this.values[i])) continue;
            result.add(this.transitions[i], this.transitions[i + 1] - 1);
        }
        if (value != null && this.stringMap != null) {
            for (String key : this.stringMap.keySet()) {
                T newValue = this.stringMap.get(key);
                if (!value.equals(newValue)) continue;
                result.add(key);
            }
        }
        return result;
    }

    public UnicodeSet keySet(T value) {
        return this.keySet(value, null);
    }

    public UnicodeSet keySet() {
        UnicodeSet result = new UnicodeSet();
        for (int i = 0; i < this.length - 1; ++i) {
            if (this.values[i] == null) continue;
            result.add(this.transitions[i], this.transitions[i + 1] - 1);
        }
        if (this.stringMap != null) {
            result.addAll((Iterable<?>)this.stringMap.keySet());
        }
        return result;
    }

    public <U extends Collection<T>> U values(U result) {
        if (this.staleAvailableValues) {
            HashSet<T> temp = new HashSet<T>();
            for (int i = 0; i < this.length - 1; ++i) {
                if (this.values[i] == null) continue;
                temp.add(this.values[i]);
            }
            this.availableValues.retainAll(temp);
            if (this.stringMap != null) {
                this.availableValues.addAll(this.stringMap.values());
            }
            this.staleAvailableValues = false;
        }
        if (result == null) {
            result = new LinkedHashSet(this.availableValues.size());
        }
        result.addAll(this.availableValues);
        return result;
    }

    public Set<T> values() {
        return this.getAvailableValues(null);
    }

    public T get(int codepoint) {
        if (codepoint < 0 || codepoint > 0x10FFFF) {
            throw new IllegalArgumentException("Codepoint out of range: " + codepoint);
        }
        return this.values[this._findIndex(codepoint)];
    }

    public T get(String value) {
        if (UTF16.hasMoreCodePointsThan(value, 1)) {
            if (this.stringMap == null) {
                return null;
            }
            return this.stringMap.get(value);
        }
        return this.getValue(UTF16.charAt(value, 0));
    }

    @Override
    public String transform(String source) {
        int cp;
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < source.length(); i += UTF16.getCharCount(cp)) {
            cp = UTF16.charAt(source, i);
            T mResult = this.getValue(cp);
            if (mResult != null) {
                result.append(mResult);
                continue;
            }
            UTF16.append(result, cp);
        }
        return result.toString();
    }

    public UnicodeMap<T> composeWith(UnicodeMap<T> other, Composer<T> composer) {
        for (T value : other.getAvailableValues()) {
            UnicodeSet set = other.keySet(value);
            this.composeWith(set, value, composer);
        }
        return this;
    }

    public UnicodeMap<T> composeWith(UnicodeSet set, T value, Composer<T> composer) {
        UnicodeSetIterator it = new UnicodeSetIterator(set);
        while (it.next()) {
            T v3;
            int i = it.codepoint;
            if (i == -1) {
                T v32;
                String s = it.string;
                T v1 = this.getValue(s);
                if (v1 == (v32 = composer.compose(-1, s, v1, value)) || v1 != null && v1.equals(v32)) continue;
                this.put(s, v32);
                continue;
            }
            T v1 = this.getValue(i);
            if (v1 == (v3 = composer.compose(i, null, v1, value)) || v1 != null && v1.equals(v3)) continue;
            this.put(i, v3);
        }
        return this;
    }

    public String toString() {
        return this.toString(null);
    }

    public String toString(Comparator<T> collected) {
        StringBuffer result;
        block5: {
            block4: {
                result = new StringBuffer();
                if (collected != null) break block4;
                for (int i = 0; i < this.length - 1; ++i) {
                    T value = this.values[i];
                    if (value == null) continue;
                    int start = this.transitions[i];
                    int end = this.transitions[i + 1] - 1;
                    result.append(Utility.hex(start));
                    if (start != end) {
                        result.append("-").append(Utility.hex(end));
                    }
                    result.append("=").append(value.toString()).append("\n");
                }
                if (this.stringMap == null) break block5;
                for (String s : this.stringMap.keySet()) {
                    result.append(Utility.hex(s)).append("=").append(this.stringMap.get(s).toString()).append("\n");
                }
                break block5;
            }
            Set set = this.values(new TreeSet<T>(collected));
            for (Object value : set) {
                UnicodeSet s = this.keySet(value);
                result.append(value).append("=").append(s.toString()).append("\n");
            }
        }
        return result.toString();
    }

    public boolean getErrorOnReset() {
        return this.errorOnReset;
    }

    public UnicodeMap<T> setErrorOnReset(boolean errorOnReset) {
        this.errorOnReset = errorOnReset;
        return this;
    }

    @Override
    public boolean isFrozen() {
        return this.locked;
    }

    @Override
    public UnicodeMap<T> freeze() {
        this.locked = true;
        return this;
    }

    public static int findCommonPrefix(String last, String s) {
        int minLen = Math.min(last.length(), s.length());
        for (int i = 0; i < minLen; ++i) {
            if (last.charAt(i) == s.charAt(i)) continue;
            return i;
        }
        return minLen;
    }

    public int getRangeCount() {
        return this.length - 1;
    }

    public int getRangeStart(int range) {
        return this.transitions[range];
    }

    public int getRangeEnd(int range) {
        return this.transitions[range + 1] - 1;
    }

    public T getRangeValue(int range) {
        return this.values[range];
    }

    public Set<String> getNonRangeStrings() {
        if (this.stringMap == null || this.stringMap.isEmpty()) {
            return null;
        }
        return Collections.unmodifiableSet(this.stringMap.keySet());
    }

    public boolean containsKey(String key) {
        return this.getValue(key) != null;
    }

    public boolean containsKey(int key) {
        return this.getValue(key) != null;
    }

    public boolean containsValue(T value) {
        return this.getAvailableValues().contains(value);
    }

    public boolean isEmpty() {
        return this.size() == 0;
    }

    public UnicodeMap<T> putAll(Map<? extends String, ? extends T> map) {
        for (String string : map.keySet()) {
            this.put(string, map.get(string));
        }
        return this;
    }

    public UnicodeMap<T> putAllIn(Map<? super String, ? super T> map) {
        for (String key : this.keySet()) {
            map.put(key, this.get(key));
        }
        return this;
    }

    public <U extends Map<String, T>> U putAllInto(U map) {
        for (EntryRange<T> entry : this.entryRanges()) {
            if (entry.string != null) break;
            for (int cp = entry.codepoint; cp <= entry.codepointEnd; ++cp) {
                map.put((String)UTF16.valueOf(cp), entry.value);
            }
        }
        map.putAll(this.stringMap);
        return map;
    }

    public <U extends Map<Integer, T>> U putAllCodepointsInto(U map) {
        for (EntryRange<T> entry : this.entryRanges()) {
            if (entry.string != null) break;
            for (int cp = entry.codepoint; cp <= entry.codepointEnd; ++cp) {
                map.put(cp, entry.value);
            }
        }
        return map;
    }

    public UnicodeMap<T> remove(String key) {
        return this.put(key, null);
    }

    public UnicodeMap<T> remove(int key) {
        return this.put(key, null);
    }

    public int size() {
        int result = this.stringMap == null ? 0 : this.stringMap.size();
        for (int i = 0; i < this.length - 1; ++i) {
            T value = this.values[i];
            if (value == null) continue;
            result += this.transitions[i + 1] - this.transitions[i];
        }
        return result;
    }

    public Iterable<Map.Entry<String, T>> entrySet() {
        return new EntrySetX();
    }

    public Iterable<EntryRange<T>> entryRanges() {
        return new EntryRanges();
    }

    @Override
    public Iterator<String> iterator() {
        return this.keySet().iterator();
    }

    public T getValue(String key) {
        return this.get(key);
    }

    public T getValue(int key) {
        return this.get(key);
    }

    public Collection<T> getAvailableValues() {
        return this.values();
    }

    public <U extends Collection<T>> U getAvailableValues(U result) {
        return this.values(result);
    }

    public UnicodeSet getSet(T value) {
        return this.keySet(value);
    }

    public UnicodeSet getSet(T value, UnicodeSet result) {
        return this.keySet(value, result);
    }

    public final UnicodeMap<T> removeAll(UnicodeSet set) {
        return this.putAll(set, null);
    }

    public final UnicodeMap<T> removeAll(UnicodeMap<T> reference) {
        return this.removeRetainAll(reference, true);
    }

    public final UnicodeMap<T> retainAll(UnicodeSet set) {
        UnicodeSet toNuke = new UnicodeSet();
        for (EntryRange<T> ae : this.entryRanges()) {
            if (ae.string != null) {
                if (set.contains(ae.string)) continue;
                toNuke.add(ae.string);
                continue;
            }
            for (int i = ae.codepoint; i <= ae.codepointEnd; ++i) {
                if (set.contains(i)) continue;
                toNuke.add(i);
            }
        }
        return this.putAll(toNuke, null);
    }

    public final UnicodeMap<T> retainAll(UnicodeMap<T> reference) {
        return this.removeRetainAll(reference, false);
    }

    private final UnicodeMap<T> removeRetainAll(UnicodeMap<T> reference, boolean remove) {
        UnicodeSet toNuke = new UnicodeSet();
        for (EntryRange<T> ae : this.entryRanges()) {
            if (ae.string != null) {
                if (ae.value.equals(reference.get(ae.string)) != remove) continue;
                toNuke.add(ae.string);
                continue;
            }
            for (int i = ae.codepoint; i <= ae.codepointEnd; ++i) {
                if (ae.value.equals(reference.get(i)) != remove) continue;
                toNuke.add(i);
            }
        }
        return this.putAll(toNuke, null);
    }

    public final Set<String> stringKeys() {
        return this.getNonRangeStrings();
    }

    public <U extends Map<T, UnicodeSet>> U addInverseTo(U target) {
        for (T value : this.values()) {
            UnicodeSet uset = this.getSet(value);
            target.put(value, (UnicodeSet)uset);
        }
        return target;
    }

    public static <T> Map<T, UnicodeSet> freeze(Map<T, UnicodeSet> target) {
        for (UnicodeSet entry : target.values()) {
            entry.freeze();
        }
        return Collections.unmodifiableMap(target);
    }

    public UnicodeMap<T> putAllInverse(Map<T, UnicodeSet> source) {
        for (Map.Entry<T, UnicodeSet> entry : source.entrySet()) {
            this.putAll(entry.getValue(), entry.getKey());
        }
        return this;
    }

    private class EntryRanges
    implements Iterable<EntryRange<T>>,
    Iterator<EntryRange<T>> {
        private int pos;
        private EntryRange<T> result = new EntryRange();
        private int lastRealRange;
        private Iterator<Map.Entry<String, T>> stringIterator;

        private EntryRanges() {
            this.lastRealRange = UnicodeMap.this.values[UnicodeMap.this.length - 2] == null ? UnicodeMap.this.length - 2 : UnicodeMap.this.length - 1;
            this.stringIterator = UnicodeMap.this.stringMap == null ? null : UnicodeMap.this.stringMap.entrySet().iterator();
        }

        @Override
        public Iterator<EntryRange<T>> iterator() {
            return this;
        }

        @Override
        public boolean hasNext() {
            return this.pos < this.lastRealRange || this.stringIterator != null && this.stringIterator.hasNext();
        }

        @Override
        public EntryRange<T> next() {
            if (this.pos < this.lastRealRange) {
                Object temp = UnicodeMap.this.values[this.pos];
                if (temp == null) {
                    temp = UnicodeMap.this.values[++this.pos];
                }
                this.result.codepoint = UnicodeMap.this.transitions[this.pos];
                this.result.codepointEnd = UnicodeMap.this.transitions[this.pos + 1] - 1;
                this.result.string = null;
                this.result.value = temp;
                ++this.pos;
            } else {
                Map.Entry entry = this.stringIterator.next();
                this.result.codepointEnd = -1;
                this.result.codepoint = -1;
                this.result.string = entry.getKey();
                this.result.value = entry.getValue();
            }
            return this.result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public static class EntryRange<T> {
        public int codepoint;
        public int codepointEnd;
        public String string;
        public T value;

        public String toString() {
            return (this.string != null ? Utility.hex(this.string) : Utility.hex(this.codepoint) + (this.codepoint == this.codepointEnd ? "" : ".." + Utility.hex(this.codepointEnd))) + "=" + this.value;
        }
    }

    private class IteratorX
    implements Iterator<Map.Entry<String, T>> {
        Iterator<String> iterator;

        private IteratorX() {
            this.iterator = UnicodeMap.this.keySet().iterator();
        }

        @Override
        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override
        public Map.Entry<String, T> next() {
            String key = this.iterator.next();
            return new ImmutableEntry(key, UnicodeMap.this.get(key));
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class EntrySetX
    implements Iterable<Map.Entry<String, T>> {
        private EntrySetX() {
        }

        @Override
        public Iterator<Map.Entry<String, T>> iterator() {
            return new IteratorX();
        }

        public String toString() {
            StringBuffer b = new StringBuffer();
            for (Map.Entry item : this) {
                b.append(item.toString()).append(' ');
            }
            return b.toString();
        }
    }

    public static abstract class Composer<T> {
        public abstract T compose(int var1, String var2, T var3, T var4);
    }
}

