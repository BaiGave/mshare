/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.internal.map;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.TriConsumer;

public class UnmodifiableArrayBackedMap
extends AbstractMap<String, String>
implements ReadOnlyStringMap {
    private static final long serialVersionUID = 6849423432534211514L;
    public static final UnmodifiableArrayBackedMap EMPTY_MAP = new UnmodifiableArrayBackedMap(0);
    private static final int NUM_FIXED_ARRAY_ENTRIES = 1;
    private Object[] backingArray;
    private int numEntries;

    private static int getArrayIndexForKey(int entryIndex) {
        return 2 * entryIndex + 1;
    }

    private static int getArrayIndexForValue(int entryIndex) {
        return 2 * entryIndex + 1 + 1;
    }

    public static UnmodifiableArrayBackedMap getMap(Object[] backingArray) {
        if (backingArray == null || backingArray.length == 1) {
            return EMPTY_MAP;
        }
        return new UnmodifiableArrayBackedMap(backingArray);
    }

    private UnmodifiableArrayBackedMap(int capacity) {
        this.backingArray = new Object[capacity * 2 + 1];
        this.backingArray[0] = 0;
    }

    private UnmodifiableArrayBackedMap(Object[] backingArray) {
        this.numEntries = backingArray == null ? 0 : (Integer)backingArray[0];
        this.backingArray = backingArray;
    }

    UnmodifiableArrayBackedMap(UnmodifiableArrayBackedMap other) {
        this.backingArray = other.backingArray;
        this.numEntries = other.numEntries;
    }

    private void add(String key, String value) {
        this.backingArray[UnmodifiableArrayBackedMap.getArrayIndexForKey((int)this.numEntries)] = key;
        this.backingArray[UnmodifiableArrayBackedMap.getArrayIndexForValue((int)this.numEntries)] = value;
        ++this.numEntries;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Instance cannot be cleared, reuse EMPTY_MAP instead.");
    }

    @Override
    public boolean containsKey(Object key) {
        return this.containsKey((String)key);
    }

    @Override
    public boolean containsKey(String key) {
        int hashCode = key.hashCode();
        for (int i = 0; i < this.numEntries; ++i) {
            if (this.backingArray[UnmodifiableArrayBackedMap.getArrayIndexForKey(i)].hashCode() != hashCode || !this.backingArray[UnmodifiableArrayBackedMap.getArrayIndexForKey(i)].equals(key)) continue;
            return true;
        }
        return false;
    }

    public Object[] getBackingArray() {
        return this.backingArray;
    }

    @Override
    public boolean containsValue(Object value) {
        for (int i = 0; i < this.numEntries; ++i) {
            Object valueInMap = this.backingArray[UnmodifiableArrayBackedMap.getArrayIndexForValue(i)];
            if (!(value == null ? valueInMap == null : value.equals(valueInMap))) continue;
            return true;
        }
        return false;
    }

    public UnmodifiableArrayBackedMap copyAndPut(String key, String value) {
        UnmodifiableArrayBackedMap newMap = new UnmodifiableArrayBackedMap(this.numEntries + 1);
        if (this.numEntries > 0) {
            System.arraycopy(this.backingArray, 1, newMap.backingArray, 1, this.numEntries * 2);
            newMap.numEntries = this.numEntries;
        }
        newMap.addOrOverwriteKey(key, value);
        newMap.updateNumEntriesInArray();
        return newMap;
    }

    public UnmodifiableArrayBackedMap copyAndPutAll(Map<String, String> entriesToAdd) {
        UnmodifiableArrayBackedMap newMap = new UnmodifiableArrayBackedMap(this.numEntries + entriesToAdd.size());
        if (this.numEntries > 0) {
            System.arraycopy(this.backingArray, 0, newMap.backingArray, 0, this.numEntries * 2 + 1);
            newMap.numEntries = this.numEntries;
        }
        for (Map.Entry<String, String> entry : entriesToAdd.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (!this.isEmpty()) {
                newMap.addOrOverwriteKey(key, value);
                continue;
            }
            newMap.add(key, value);
        }
        newMap.updateNumEntriesInArray();
        return newMap;
    }

    public UnmodifiableArrayBackedMap copyAndRemove(String key) {
        int indexToRemove = -1;
        for (int oldIndex = this.numEntries - 1; oldIndex >= 0; --oldIndex) {
            if (this.backingArray[UnmodifiableArrayBackedMap.getArrayIndexForKey(oldIndex)].hashCode() != key.hashCode() || !this.backingArray[UnmodifiableArrayBackedMap.getArrayIndexForKey(oldIndex)].equals(key)) continue;
            indexToRemove = oldIndex;
            break;
        }
        if (indexToRemove == -1) {
            return this;
        }
        if (this.numEntries == 1) {
            return EMPTY_MAP;
        }
        UnmodifiableArrayBackedMap newMap = new UnmodifiableArrayBackedMap(this.numEntries);
        if (indexToRemove > 0) {
            System.arraycopy(this.backingArray, 1, newMap.backingArray, 1, indexToRemove * 2);
        }
        if (indexToRemove + 1 < this.numEntries) {
            int nextIndexToCopy = indexToRemove + 1;
            int numRemainingEntries = this.numEntries - nextIndexToCopy;
            System.arraycopy(this.backingArray, UnmodifiableArrayBackedMap.getArrayIndexForKey(nextIndexToCopy), newMap.backingArray, UnmodifiableArrayBackedMap.getArrayIndexForKey(indexToRemove), numRemainingEntries * 2);
        }
        newMap.numEntries = this.numEntries - 1;
        newMap.updateNumEntriesInArray();
        return newMap;
    }

    public UnmodifiableArrayBackedMap copyAndRemoveAll(Iterable<String> keysToRemoveIterable) {
        HashSet<String> keysToRemove;
        if (this.isEmpty()) {
            return EMPTY_MAP;
        }
        if (keysToRemoveIterable instanceof Set) {
            keysToRemove = (HashSet<String>)keysToRemoveIterable;
        } else {
            keysToRemove = new HashSet<String>();
            for (String key : keysToRemoveIterable) {
                keysToRemove.add(key);
            }
        }
        UnmodifiableArrayBackedMap oldMap = this;
        int oldMapEntryCount = oldMap.numEntries;
        UnmodifiableArrayBackedMap newMap = new UnmodifiableArrayBackedMap(oldMapEntryCount);
        if (keysToRemove.isEmpty()) {
            System.arraycopy(oldMap.backingArray, 0, newMap.backingArray, 0, oldMapEntryCount * 2);
            newMap.numEntries = oldMapEntryCount;
            return this;
        }
        int newMapEntryIndex = 0;
        for (int oldMapEntryIndex = 0; oldMapEntryIndex < oldMapEntryCount; ++oldMapEntryIndex) {
            int oldMapKeyIndex = UnmodifiableArrayBackedMap.getArrayIndexForKey(oldMapEntryIndex);
            Object key = oldMap.backingArray[oldMapKeyIndex];
            boolean removed = keysToRemove.contains(key);
            if (removed) continue;
            int oldMapValueIndex = UnmodifiableArrayBackedMap.getArrayIndexForValue(oldMapEntryIndex);
            Object value = oldMap.backingArray[oldMapValueIndex];
            int newMapKeyIndex = UnmodifiableArrayBackedMap.getArrayIndexForKey(newMapEntryIndex);
            int newMapValueIndex = UnmodifiableArrayBackedMap.getArrayIndexForValue(newMapEntryIndex);
            newMap.backingArray[newMapKeyIndex] = key;
            newMap.backingArray[newMapValueIndex] = value;
            ++newMapEntryIndex;
        }
        newMap.numEntries = newMapEntryIndex;
        newMap.updateNumEntriesInArray();
        return newMap;
    }

    private void updateNumEntriesInArray() {
        this.backingArray[0] = this.numEntries;
    }

    @Override
    public void forEach(java.util.function.BiConsumer<? super String, ? super String> action) {
        for (int i = 0; i < this.numEntries; ++i) {
            String key = (String)this.backingArray[UnmodifiableArrayBackedMap.getArrayIndexForKey(i)];
            String value = (String)this.backingArray[UnmodifiableArrayBackedMap.getArrayIndexForValue(i)];
            action.accept(key, value);
        }
    }

    @Override
    public <V> void forEach(BiConsumer<String, ? super V> action) {
        for (int i = 0; i < this.numEntries; ++i) {
            String key = (String)this.backingArray[UnmodifiableArrayBackedMap.getArrayIndexForKey(i)];
            Object value = this.backingArray[UnmodifiableArrayBackedMap.getArrayIndexForValue(i)];
            action.accept(key, value);
        }
    }

    @Override
    public <V, S> void forEach(TriConsumer<String, ? super V, S> action, S state) {
        for (int i = 0; i < this.numEntries; ++i) {
            String key = (String)this.backingArray[UnmodifiableArrayBackedMap.getArrayIndexForKey(i)];
            Object value = this.backingArray[UnmodifiableArrayBackedMap.getArrayIndexForValue(i)];
            action.accept(key, value, state);
        }
    }

    @Override
    public Set<Map.Entry<String, String>> entrySet() {
        return new UnmodifiableEntrySet();
    }

    @Override
    public String get(Object key) {
        return (String)this.getValue((String)key);
    }

    @Override
    public <V> V getValue(String key) {
        if (this.numEntries == 0) {
            return null;
        }
        int hashCode = key.hashCode();
        for (int i = 0; i < this.numEntries; ++i) {
            if (this.backingArray[UnmodifiableArrayBackedMap.getArrayIndexForKey(i)].hashCode() != hashCode || !this.backingArray[UnmodifiableArrayBackedMap.getArrayIndexForKey(i)].equals(key)) continue;
            return (V)this.backingArray[UnmodifiableArrayBackedMap.getArrayIndexForValue(i)];
        }
        return null;
    }

    private void addOrOverwriteKey(String key, String value) {
        int keyHashCode = key.hashCode();
        for (int i = 0; i < this.numEntries; ++i) {
            if (this.backingArray[UnmodifiableArrayBackedMap.getArrayIndexForKey(i)].hashCode() != keyHashCode || !this.backingArray[UnmodifiableArrayBackedMap.getArrayIndexForKey(i)].equals(key)) continue;
            this.backingArray[UnmodifiableArrayBackedMap.getArrayIndexForValue((int)i)] = value;
            return;
        }
        this.add(key, value);
    }

    @Override
    public String put(String key, String value) {
        throw new UnsupportedOperationException("put() is not supported, use copyAndPut instead");
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        throw new UnsupportedOperationException("putAll() is not supported, use copyAndPutAll instead");
    }

    @Override
    public String remove(Object key) {
        throw new UnsupportedOperationException("remove() is not supported, use copyAndRemove instead");
    }

    @Override
    public int size() {
        return this.numEntries;
    }

    @Override
    public Map<String, String> toMap() {
        return this;
    }

    private class UnmodifiableEntrySet
    extends AbstractSet<Map.Entry<String, String>> {
        private UnmodifiableEntrySet() {
        }

        @Override
        public boolean add(Map.Entry<String, String> e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection<? extends Map.Entry<String, String>> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator<Map.Entry<String, String>> iterator() {
            return new UnmodifiableEntryIterator();
        }

        @Override
        public int size() {
            return UnmodifiableArrayBackedMap.this.numEntries;
        }
    }

    private class UnmodifiableEntryIterator
    implements Iterator<Map.Entry<String, String>> {
        private int index;

        private UnmodifiableEntryIterator() {
        }

        @Override
        public boolean hasNext() {
            return this.index < UnmodifiableArrayBackedMap.this.numEntries;
        }

        @Override
        public Map.Entry<String, String> next() {
            return new UnmodifiableEntry(this.index++);
        }
    }

    private class UnmodifiableEntry
    implements Map.Entry<String, String> {
        private int index;

        public UnmodifiableEntry(int index) {
            this.index = index;
        }

        @Override
        public String getKey() {
            return (String)UnmodifiableArrayBackedMap.this.backingArray[UnmodifiableArrayBackedMap.getArrayIndexForKey(this.index)];
        }

        @Override
        public String getValue() {
            return (String)UnmodifiableArrayBackedMap.this.backingArray[UnmodifiableArrayBackedMap.getArrayIndexForValue(this.index)];
        }

        @Override
        public int hashCode() {
            String key = (String)UnmodifiableArrayBackedMap.this.backingArray[UnmodifiableArrayBackedMap.getArrayIndexForKey(this.index)];
            String value = (String)UnmodifiableArrayBackedMap.this.backingArray[UnmodifiableArrayBackedMap.getArrayIndexForValue(this.index)];
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        @Override
        public String setValue(String value) {
            throw new UnsupportedOperationException("Cannot update Entry instances in UnmodifiableArrayBackedMap");
        }
    }
}

