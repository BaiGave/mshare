/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.StandardRowSortedTable;
import com.google.common.collect.StandardTable;
import com.google.common.collect.UnmodifiableIterator;
import com.google.errorprone.annotations.InlineMe;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public class TreeBasedTable<R, C, V>
extends StandardRowSortedTable<R, C, V> {
    private final Comparator<? super C> columnComparator;
    @GwtIncompatible
    @J2ktIncompatible
    private static final long serialVersionUID = 0L;

    public static <R extends Comparable, C extends Comparable, V> TreeBasedTable<R, C, V> create() {
        return new TreeBasedTable(Ordering.natural(), Ordering.natural());
    }

    public static <R, C, V> TreeBasedTable<R, C, V> create(Comparator<? super R> rowComparator, Comparator<? super C> columnComparator) {
        Preconditions.checkNotNull(rowComparator);
        Preconditions.checkNotNull(columnComparator);
        return new TreeBasedTable<R, C, V>(rowComparator, columnComparator);
    }

    public static <R, C, V> TreeBasedTable<R, C, V> create(TreeBasedTable<R, C, ? extends V> table) {
        TreeBasedTable result = new TreeBasedTable(Objects.requireNonNull(table.rowKeySet().comparator()), table.columnComparator());
        result.putAll(table);
        return result;
    }

    TreeBasedTable(Comparator<? super R> rowComparator, Comparator<? super C> columnComparator) {
        super(new TreeMap(rowComparator), new Factory(columnComparator));
        this.columnComparator = columnComparator;
    }

    @Deprecated
    @InlineMe(replacement="requireNonNull(this.rowKeySet().comparator())", staticImports={"java.util.Objects.requireNonNull"})
    public final Comparator<? super R> rowComparator() {
        return Objects.requireNonNull(this.rowKeySet().comparator());
    }

    @Deprecated
    public Comparator<? super C> columnComparator() {
        return this.columnComparator;
    }

    @Override
    public SortedMap<C, V> row(R rowKey) {
        return new TreeRow(rowKey);
    }

    @Override
    Iterator<C> createColumnKeyIterator() {
        final Comparator<C> comparator = this.columnComparator();
        final UnmodifiableIterator<C> merged = Iterators.mergeSorted(Iterables.transform(this.backingMap.values(), input -> input.keySet().iterator()), comparator);
        return new AbstractIterator<C>(this){
            @Nullable C lastValue;
            final /* synthetic */ TreeBasedTable this$0;
            {
                this.this$0 = this$0;
            }

            @Override
            protected @Nullable C computeNext() {
                while (merged.hasNext()) {
                    Object next = merged.next();
                    boolean duplicate = this.lastValue != null && comparator.compare(next, this.lastValue) == 0;
                    if (duplicate) continue;
                    this.lastValue = next;
                    return this.lastValue;
                }
                this.lastValue = null;
                return this.endOfData();
            }
        };
    }

    private static final class Factory<C, V>
    implements Supplier<Map<C, V>>,
    Serializable {
        final Comparator<? super C> comparator;
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        Factory(Comparator<? super C> comparator) {
            this.comparator = comparator;
        }

        @Override
        public Map<C, V> get() {
            return new TreeMap(this.comparator);
        }
    }

    private final class TreeRow
    extends StandardTable.Row
    implements SortedMap<C, V> {
        final @Nullable C lowerBound;
        final @Nullable C upperBound;
        transient @Nullable SortedMap<C, V> wholeRow;

        TreeRow(R rowKey) {
            this(rowKey, null, null);
        }

        TreeRow(@Nullable R rowKey, @Nullable C lowerBound, C upperBound) {
            super(rowKey);
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
            Preconditions.checkArgument(lowerBound == null || upperBound == null || this.compare(lowerBound, upperBound) <= 0);
        }

        @Override
        public SortedSet<C> keySet() {
            return new Maps.SortedKeySet(this);
        }

        @Override
        public Comparator<? super C> comparator() {
            return TreeBasedTable.this.columnComparator();
        }

        int compare(Object a, Object b) {
            Comparator cmp = this.comparator();
            return cmp.compare(a, b);
        }

        boolean rangeContains(@Nullable Object o) {
            return !(o == null || this.lowerBound != null && this.compare(this.lowerBound, o) > 0 || this.upperBound != null && this.compare(this.upperBound, o) <= 0);
        }

        @Override
        public SortedMap<C, V> subMap(C fromKey, C toKey) {
            Preconditions.checkArgument(this.rangeContains(Preconditions.checkNotNull(fromKey)) && this.rangeContains(Preconditions.checkNotNull(toKey)));
            return new TreeRow(this.rowKey, fromKey, toKey);
        }

        @Override
        public SortedMap<C, V> headMap(C toKey) {
            Preconditions.checkArgument(this.rangeContains(Preconditions.checkNotNull(toKey)));
            return new TreeRow(this.rowKey, this.lowerBound, toKey);
        }

        @Override
        public SortedMap<C, V> tailMap(C fromKey) {
            Preconditions.checkArgument(this.rangeContains(Preconditions.checkNotNull(fromKey)));
            return new TreeRow(this.rowKey, fromKey, this.upperBound);
        }

        @Override
        public C firstKey() {
            this.updateBackingRowMapField();
            if (this.backingRowMap == null) {
                throw new NoSuchElementException();
            }
            return ((SortedMap)this.backingRowMap).firstKey();
        }

        @Override
        public C lastKey() {
            this.updateBackingRowMapField();
            if (this.backingRowMap == null) {
                throw new NoSuchElementException();
            }
            return ((SortedMap)this.backingRowMap).lastKey();
        }

        void updateWholeRowField() {
            if (this.wholeRow == null || this.wholeRow.isEmpty() && TreeBasedTable.this.backingMap.containsKey(this.rowKey)) {
                this.wholeRow = (SortedMap)TreeBasedTable.this.backingMap.get(this.rowKey);
            }
        }

        @Nullable SortedMap<C, V> computeBackingRowMap() {
            this.updateWholeRowField();
            SortedMap map = this.wholeRow;
            if (map != null) {
                if (this.lowerBound != null) {
                    map = map.tailMap(this.lowerBound);
                }
                if (this.upperBound != null) {
                    map = map.headMap(this.upperBound);
                }
                return map;
            }
            return null;
        }

        @Override
        void maintainEmptyInvariant() {
            this.updateWholeRowField();
            if (this.wholeRow != null && this.wholeRow.isEmpty()) {
                TreeBasedTable.this.backingMap.remove(this.rowKey);
                this.wholeRow = null;
                this.backingRowMap = null;
            }
        }

        @Override
        public boolean containsKey(@Nullable Object key) {
            return this.rangeContains(key) && super.containsKey(key);
        }

        @Override
        public @Nullable V put(C key, V value) {
            Preconditions.checkArgument(this.rangeContains(Preconditions.checkNotNull(key)));
            return super.put(key, value);
        }
    }
}

