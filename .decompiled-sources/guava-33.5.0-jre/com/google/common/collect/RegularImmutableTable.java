/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.DenseImmutableTable;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.IndexedImmutableSet;
import com.google.common.collect.SparseImmutableTable;
import com.google.common.collect.Table;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import org.jspecify.annotations.Nullable;

@GwtCompatible
abstract class RegularImmutableTable<R, C, V>
extends ImmutableTable<R, C, V> {
    RegularImmutableTable() {
    }

    abstract Table.Cell<R, C, V> getCell(int var1);

    @Override
    final ImmutableSet<Table.Cell<R, C, V>> createCellSet() {
        return this.isEmpty() ? ImmutableSet.of() : new CellSet();
    }

    abstract V getValue(int var1);

    @Override
    final ImmutableCollection<V> createValues() {
        return this.isEmpty() ? ImmutableList.of() : new Values();
    }

    static <R, C, V> RegularImmutableTable<R, C, V> forCells(List<Table.Cell<R, C, V>> cells, @Nullable Comparator<? super R> rowComparator, @Nullable Comparator<? super C> columnComparator) {
        Preconditions.checkNotNull(cells);
        if (rowComparator != null || columnComparator != null) {
            Comparator comparator = (cell1, cell2) -> {
                int rowCompare;
                int n = rowCompare = rowComparator == null ? 0 : rowComparator.compare((Object)cell1.getRowKey(), (Object)cell2.getRowKey());
                if (rowCompare != 0) {
                    return rowCompare;
                }
                return columnComparator == null ? 0 : columnComparator.compare((Object)cell1.getColumnKey(), (Object)cell2.getColumnKey());
            };
            Collections.sort(cells, comparator);
        }
        return RegularImmutableTable.forCellsInternal(cells, rowComparator, columnComparator);
    }

    static <R, C, V> RegularImmutableTable<R, C, V> forCells(Iterable<Table.Cell<R, C, V>> cells) {
        return RegularImmutableTable.forCellsInternal(cells, null, null);
    }

    private static <R, C, V> RegularImmutableTable<R, C, V> forCellsInternal(Iterable<Table.Cell<R, C, V>> cells, @Nullable Comparator<? super R> rowComparator, @Nullable Comparator<? super C> columnComparator) {
        LinkedHashSet<R> rowSpaceBuilder = new LinkedHashSet<R>();
        LinkedHashSet<C> columnSpaceBuilder = new LinkedHashSet<C>();
        ImmutableList<Table.Cell<R, C, V>> cellList = ImmutableList.copyOf(cells);
        for (Table.Cell<R, C, V> cell : cells) {
            rowSpaceBuilder.add(cell.getRowKey());
            columnSpaceBuilder.add(cell.getColumnKey());
        }
        ImmutableSet rowSpace = rowComparator == null ? ImmutableSet.copyOf(rowSpaceBuilder) : ImmutableSet.copyOf(ImmutableList.sortedCopyOf(rowComparator, rowSpaceBuilder));
        ImmutableSet columnSpace = columnComparator == null ? ImmutableSet.copyOf(columnSpaceBuilder) : ImmutableSet.copyOf(ImmutableList.sortedCopyOf(columnComparator, columnSpaceBuilder));
        return RegularImmutableTable.forOrderedComponents(cellList, rowSpace, columnSpace);
    }

    static <R, C, V> RegularImmutableTable<R, C, V> forOrderedComponents(ImmutableList<Table.Cell<R, C, V>> cellList, ImmutableSet<R> rowSpace, ImmutableSet<C> columnSpace) {
        return (long)cellList.size() > (long)rowSpace.size() * (long)columnSpace.size() / 2L ? new DenseImmutableTable<R, C, V>(cellList, rowSpace, columnSpace) : new SparseImmutableTable<R, C, V>(cellList, rowSpace, columnSpace);
    }

    final void checkNoDuplicate(R rowKey, C columnKey, @Nullable V existingValue, V newValue) {
        Preconditions.checkArgument(existingValue == null, "Duplicate key: (row=%s, column=%s), values: [%s, %s].", rowKey, columnKey, newValue, existingValue);
    }

    @Override
    @J2ktIncompatible
    @GwtIncompatible
    abstract Object writeReplace();

    private final class CellSet
    extends IndexedImmutableSet<Table.Cell<R, C, V>> {
        private CellSet() {
        }

        @Override
        public int size() {
            return RegularImmutableTable.this.size();
        }

        @Override
        Table.Cell<R, C, V> get(int index) {
            return RegularImmutableTable.this.getCell(index);
        }

        @Override
        public boolean contains(@Nullable Object object) {
            if (object instanceof Table.Cell) {
                Table.Cell cell = (Table.Cell)object;
                Object value = RegularImmutableTable.this.get(cell.getRowKey(), cell.getColumnKey());
                return value != null && value.equals(cell.getValue());
            }
            return false;
        }

        @Override
        boolean isPartialView() {
            return false;
        }

        @Override
        @J2ktIncompatible
        @GwtIncompatible
        Object writeReplace() {
            return super.writeReplace();
        }
    }

    private final class Values
    extends ImmutableList<V> {
        private Values() {
        }

        @Override
        public int size() {
            return RegularImmutableTable.this.size();
        }

        @Override
        public V get(int index) {
            return RegularImmutableTable.this.getValue(index);
        }

        @Override
        boolean isPartialView() {
            return true;
        }

        @Override
        @J2ktIncompatible
        @GwtIncompatible
        Object writeReplace() {
            return super.writeReplace();
        }
    }
}

