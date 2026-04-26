/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Supplier;
import com.google.common.collect.CollectPreconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.StandardTable;
import com.google.common.collect.Table;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@GwtCompatible
public class HashBasedTable<R, C, V>
extends StandardTable<R, C, V> {
    @GwtIncompatible
    @J2ktIncompatible
    private static final long serialVersionUID = 0L;

    public static <R, C, V> HashBasedTable<R, C, V> create() {
        return new HashBasedTable(new LinkedHashMap(), new Factory(0));
    }

    public static <R, C, V> HashBasedTable<R, C, V> create(int expectedRows, int expectedCellsPerRow) {
        CollectPreconditions.checkNonnegative(expectedCellsPerRow, "expectedCellsPerRow");
        LinkedHashMap backingMap = Maps.newLinkedHashMapWithExpectedSize(expectedRows);
        return new HashBasedTable(backingMap, new Factory(expectedCellsPerRow));
    }

    public static <R, C, V> HashBasedTable<R, C, V> create(Table<? extends R, ? extends C, ? extends V> table) {
        HashBasedTable<R, C, V> result = HashBasedTable.create();
        result.putAll((Table)table);
        return result;
    }

    HashBasedTable(Map<R, Map<C, V>> backingMap, Factory<C, V> factory) {
        super(backingMap, factory);
    }

    private static final class Factory<C, V>
    implements Supplier<Map<C, V>>,
    Serializable {
        final int expectedSize;
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        Factory(int expectedSize) {
            this.expectedSize = expectedSize;
        }

        @Override
        public Map<C, V> get() {
            return Maps.newLinkedHashMapWithExpectedSize(this.expectedSize);
        }
    }
}

