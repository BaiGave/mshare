/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.CollectCollectors;
import com.google.common.collect.CollectPreconditions;
import com.google.common.collect.CollectSpliterators;
import com.google.common.collect.Hashing;
import com.google.common.collect.ImmutableAsList;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableEnumSet;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.JdkBackedImmutableSet;
import com.google.common.collect.RegularImmutableAsList;
import com.google.common.collect.RegularImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.SingletonImmutableSet;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.math.IntMath;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.google.j2objc.annotations.RetainedWith;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collector;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public abstract class ImmutableSet<E>
extends ImmutableCollection<E>
implements Set<E> {
    static final int SPLITERATOR_CHARACTERISTICS = 1297;
    static final int MAX_TABLE_SIZE = 0x40000000;
    private static final double DESIRED_LOAD_FACTOR = 0.7;
    private static final int CUTOFF = 0x2CCCCCCC;
    @GwtIncompatible
    @J2ktIncompatible
    private static final long serialVersionUID = -889275714L;

    public static <E> Collector<E, ?, ImmutableSet<E>> toImmutableSet() {
        return CollectCollectors.toImmutableSet();
    }

    public static <E> ImmutableSet<E> of() {
        return RegularImmutableSet.EMPTY;
    }

    public static <E> ImmutableSet<E> of(E e1) {
        return new SingletonImmutableSet<E>(e1);
    }

    public static <E> ImmutableSet<E> of(E e1, E e2) {
        return new RegularSetBuilderImpl<E>(2).add(e1).add(e2).review().build();
    }

    public static <E> ImmutableSet<E> of(E e1, E e2, E e3) {
        return new RegularSetBuilderImpl<E>(3).add(e1).add(e2).add(e3).review().build();
    }

    public static <E> ImmutableSet<E> of(E e1, E e2, E e3, E e4) {
        return new RegularSetBuilderImpl<E>(4).add(e1).add(e2).add(e3).add(e4).review().build();
    }

    public static <E> ImmutableSet<E> of(E e1, E e2, E e3, E e4, E e5) {
        return new RegularSetBuilderImpl<E>(5).add(e1).add(e2).add(e3).add(e4).add(e5).review().build();
    }

    @SafeVarargs
    public static <E> ImmutableSet<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E ... others) {
        Preconditions.checkArgument(others.length <= 0x7FFFFFF9, "the total number of elements must fit in an int");
        SetBuilderImpl builder = new RegularSetBuilderImpl<E>(6 + others.length);
        builder = builder.add(e1).add(e2).add(e3).add(e4).add(e5).add(e6);
        for (int i = 0; i < others.length; ++i) {
            builder = builder.add(others[i]);
        }
        return builder.review().build();
    }

    public static <E> ImmutableSet<E> copyOf(Collection<? extends E> elements) {
        if (elements instanceof ImmutableSet && !(elements instanceof SortedSet)) {
            ImmutableSet set = (ImmutableSet)elements;
            if (!set.isPartialView()) {
                return set;
            }
        } else if (elements instanceof EnumSet) {
            return ImmutableSet.copyOfEnumSet((EnumSet)elements);
        }
        if (elements.isEmpty()) {
            return ImmutableSet.of();
        }
        Object[] array = elements.toArray();
        int expectedSize = elements instanceof Set ? array.length : ImmutableSet.estimatedSizeForUnknownDuplication(array.length);
        return ImmutableSet.fromArrayWithExpectedSize(array, expectedSize);
    }

    public static <E> ImmutableSet<E> copyOf(Iterable<? extends E> elements) {
        return elements instanceof Collection ? ImmutableSet.copyOf((Collection)elements) : ImmutableSet.copyOf(elements.iterator());
    }

    public static <E> ImmutableSet<E> copyOf(Iterator<? extends E> elements) {
        if (!elements.hasNext()) {
            return ImmutableSet.of();
        }
        E first = elements.next();
        if (!elements.hasNext()) {
            return ImmutableSet.of(first);
        }
        return ((Builder)((Builder)new Builder().add((Object)first)).addAll(elements)).build();
    }

    public static <E> ImmutableSet<E> copyOf(E[] elements) {
        return ImmutableSet.fromArrayWithExpectedSize(elements, ImmutableSet.estimatedSizeForUnknownDuplication(elements.length));
    }

    private static <E> ImmutableSet<E> fromArrayWithExpectedSize(E[] elements, int expectedSize) {
        switch (elements.length) {
            case 0: {
                return ImmutableSet.of();
            }
            case 1: {
                return ImmutableSet.of(elements[0]);
            }
        }
        SetBuilderImpl builder = new RegularSetBuilderImpl<E>(expectedSize);
        for (int i = 0; i < elements.length; ++i) {
            builder = ((SetBuilderImpl)builder).add(elements[i]);
        }
        return ((SetBuilderImpl)builder).review().build();
    }

    private static ImmutableSet copyOfEnumSet(EnumSet<?> enumSet) {
        return ImmutableEnumSet.asImmutable(EnumSet.copyOf(enumSet));
    }

    ImmutableSet() {
    }

    boolean isHashCodeFast() {
        return false;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof ImmutableSet && this.isHashCodeFast() && ((ImmutableSet)object).isHashCodeFast() && this.hashCode() != object.hashCode()) {
            return false;
        }
        return Sets.equalsImpl(this, object);
    }

    @Override
    public int hashCode() {
        return Sets.hashCodeImpl(this);
    }

    @Override
    public abstract UnmodifiableIterator<E> iterator();

    @Override
    @J2ktIncompatible
    Object writeReplace() {
        return new SerializedForm(this.toArray());
    }

    @J2ktIncompatible
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Use SerializedForm");
    }

    public static <E> Builder<E> builder() {
        return new Builder();
    }

    public static <E> Builder<E> builderWithExpectedSize(int expectedSize) {
        CollectPreconditions.checkNonnegative(expectedSize, "expectedSize");
        return new Builder(expectedSize);
    }

    static int chooseTableSize(int setSize) {
        if ((setSize = Math.max(setSize, 2)) < 0x2CCCCCCC) {
            int tableSize = Integer.highestOneBit(setSize - 1) << 1;
            while ((double)tableSize * 0.7 < (double)setSize) {
                tableSize <<= 1;
            }
            return tableSize;
        }
        Preconditions.checkArgument(setSize < 0x40000000, "collection too large");
        return 0x40000000;
    }

    private static int estimatedSizeForUnknownDuplication(int inputElementsIncludingAnyDuplicates) {
        if (inputElementsIncludingAnyDuplicates < 4) {
            return inputElementsIncludingAnyDuplicates;
        }
        return Math.max(4, IntMath.sqrt(inputElementsIncludingAnyDuplicates, RoundingMode.CEILING));
    }

    private static final class RegularSetBuilderImpl<E>
    extends SetBuilderImpl<E> {
        private @Nullable Object @Nullable [] hashTable;
        private int maxRunBeforeFallback;
        private int expandTableThreshold;
        private int hashCode;
        static final int MAX_RUN_MULTIPLIER = 13;

        RegularSetBuilderImpl(int expectedCapacity) {
            super(expectedCapacity);
            this.hashTable = null;
            this.maxRunBeforeFallback = 0;
            this.expandTableThreshold = 0;
        }

        RegularSetBuilderImpl(RegularSetBuilderImpl<E> toCopy) {
            super(toCopy);
            this.hashTable = toCopy.hashTable == null ? null : (Object[])toCopy.hashTable.clone();
            this.maxRunBeforeFallback = toCopy.maxRunBeforeFallback;
            this.expandTableThreshold = toCopy.expandTableThreshold;
            this.hashCode = toCopy.hashCode;
        }

        @Override
        SetBuilderImpl<E> add(E e) {
            Preconditions.checkNotNull(e);
            if (this.hashTable == null) {
                if (this.distinct == 0) {
                    this.addDedupedElement(e);
                    return this;
                }
                this.ensureTableCapacity(this.dedupedElements.length);
                Object elem = this.dedupedElements[0];
                --this.distinct;
                return this.insertInHashTable(elem).add(e);
            }
            return this.insertInHashTable(e);
        }

        private SetBuilderImpl<E> insertInHashTable(E e) {
            Objects.requireNonNull(this.hashTable);
            int eHash = e.hashCode();
            int i0 = Hashing.smear(eHash);
            int mask = this.hashTable.length - 1;
            int i = i0;
            while (i - i0 < this.maxRunBeforeFallback) {
                int index = i & mask;
                Object tableEntry = this.hashTable[index];
                if (tableEntry == null) {
                    this.addDedupedElement(e);
                    this.hashTable[index] = e;
                    this.hashCode += eHash;
                    this.ensureTableCapacity(this.distinct);
                    return this;
                }
                if (tableEntry.equals(e)) {
                    return this;
                }
                ++i;
            }
            return new JdkBackedSetBuilderImpl<E>(this).add(e);
        }

        @Override
        SetBuilderImpl<E> copy() {
            return new RegularSetBuilderImpl<E>(this);
        }

        @Override
        SetBuilderImpl<E> review() {
            if (this.hashTable == null) {
                return this;
            }
            int targetTableSize = ImmutableSet.chooseTableSize(this.distinct);
            if (targetTableSize * 2 < this.hashTable.length) {
                this.hashTable = RegularSetBuilderImpl.rebuildHashTable(targetTableSize, this.dedupedElements, this.distinct);
                this.maxRunBeforeFallback = RegularSetBuilderImpl.maxRunBeforeFallback(targetTableSize);
                this.expandTableThreshold = (int)(0.7 * (double)targetTableSize);
            }
            return RegularSetBuilderImpl.hashFloodingDetected(this.hashTable) ? new JdkBackedSetBuilderImpl(this) : this;
        }

        @Override
        ImmutableSet<E> build() {
            switch (this.distinct) {
                case 0: {
                    return ImmutableSet.of();
                }
                case 1: {
                    return ImmutableSet.of(Objects.requireNonNull(this.dedupedElements[0]));
                }
            }
            Object[] elements = this.distinct == this.dedupedElements.length ? this.dedupedElements : Arrays.copyOf(this.dedupedElements, this.distinct);
            return new RegularImmutableSet(elements, this.hashCode, Objects.requireNonNull(this.hashTable), this.hashTable.length - 1);
        }

        static @Nullable Object[] rebuildHashTable(int newTableSize, Object[] elements, int n) {
            @Nullable Object[] hashTable = new Object[newTableSize];
            int mask = hashTable.length - 1;
            for (int i = 0; i < n; ++i) {
                int j0;
                Object e = Objects.requireNonNull(elements[i]);
                int j = j0 = Hashing.smear(e.hashCode());
                while (true) {
                    int index;
                    if (hashTable[index = j & mask] == null) break;
                    ++j;
                }
                hashTable[index] = e;
            }
            return hashTable;
        }

        void ensureTableCapacity(int minCapacity) {
            int newTableSize;
            if (this.hashTable == null) {
                newTableSize = ImmutableSet.chooseTableSize(minCapacity);
                this.hashTable = new Object[newTableSize];
            } else if (minCapacity > this.expandTableThreshold && this.hashTable.length < 0x40000000) {
                newTableSize = this.hashTable.length * 2;
                this.hashTable = RegularSetBuilderImpl.rebuildHashTable(newTableSize, this.dedupedElements, this.distinct);
            } else {
                return;
            }
            this.maxRunBeforeFallback = RegularSetBuilderImpl.maxRunBeforeFallback(newTableSize);
            this.expandTableThreshold = (int)(0.7 * (double)newTableSize);
        }

        static boolean hashFloodingDetected(@Nullable Object[] hashTable) {
            int maxRunBeforeFallback = RegularSetBuilderImpl.maxRunBeforeFallback(hashTable.length);
            int mask = hashTable.length - 1;
            int knownRunStart = 0;
            int knownRunEnd = 0;
            block0: while (knownRunStart < hashTable.length) {
                if (knownRunStart == knownRunEnd && hashTable[knownRunStart] == null) {
                    if (hashTable[knownRunStart + maxRunBeforeFallback - 1 & mask] == null) {
                        knownRunStart += maxRunBeforeFallback;
                    }
                    knownRunEnd = ++knownRunStart;
                    continue;
                }
                for (int j = knownRunStart + maxRunBeforeFallback - 1; j >= knownRunEnd; --j) {
                    if (hashTable[j & mask] != null) continue;
                    knownRunEnd = knownRunStart + maxRunBeforeFallback;
                    knownRunStart = j + 1;
                    continue block0;
                }
                return true;
            }
            return false;
        }

        static int maxRunBeforeFallback(int tableSize) {
            return 13 * IntMath.log2(tableSize, RoundingMode.UNNECESSARY);
        }
    }

    private static abstract class SetBuilderImpl<E> {
        E[] dedupedElements;
        int distinct;

        SetBuilderImpl(int expectedCapacity) {
            this.dedupedElements = new Object[expectedCapacity];
            this.distinct = 0;
        }

        SetBuilderImpl(SetBuilderImpl<E> toCopy) {
            this.dedupedElements = Arrays.copyOf(toCopy.dedupedElements, toCopy.dedupedElements.length);
            this.distinct = toCopy.distinct;
        }

        private void ensureCapacity(int minCapacity) {
            if (minCapacity > this.dedupedElements.length) {
                int newCapacity = ImmutableCollection.Builder.expandedCapacity(this.dedupedElements.length, minCapacity);
                this.dedupedElements = Arrays.copyOf(this.dedupedElements, newCapacity);
            }
        }

        final void addDedupedElement(E e) {
            this.ensureCapacity(this.distinct + 1);
            this.dedupedElements[this.distinct++] = e;
        }

        abstract SetBuilderImpl<E> add(E var1);

        final SetBuilderImpl<E> combine(SetBuilderImpl<E> other) {
            SetBuilderImpl<E> result = this;
            for (int i = 0; i < other.distinct; ++i) {
                result = result.add(Objects.requireNonNull(other.dedupedElements[i]));
            }
            return result;
        }

        abstract SetBuilderImpl<E> copy();

        SetBuilderImpl<E> review() {
            return this;
        }

        abstract ImmutableSet<E> build();
    }

    public static class Builder<E>
    extends ImmutableCollection.Builder<E> {
        private @Nullable SetBuilderImpl<E> impl;
        boolean forceCopy;

        public Builder() {
            this(0);
        }

        Builder(int capacity) {
            this.impl = capacity > 0 ? new RegularSetBuilderImpl(capacity) : EmptySetBuilderImpl.instance();
        }

        Builder(boolean subclass) {
            this.impl = null;
        }

        @VisibleForTesting
        void forceJdk() {
            Objects.requireNonNull(this.impl);
            this.impl = new JdkBackedSetBuilderImpl<E>(this.impl);
        }

        final void copyIfNecessary() {
            if (this.forceCopy) {
                this.copy();
                this.forceCopy = false;
            }
        }

        void copy() {
            Objects.requireNonNull(this.impl);
            this.impl = this.impl.copy();
        }

        @Override
        @CanIgnoreReturnValue
        public Builder<E> add(E element) {
            Objects.requireNonNull(this.impl);
            Preconditions.checkNotNull(element);
            this.copyIfNecessary();
            this.impl = this.impl.add(element);
            return this;
        }

        @Override
        @CanIgnoreReturnValue
        public Builder<E> add(E ... elements) {
            super.add(elements);
            return this;
        }

        @Override
        @CanIgnoreReturnValue
        public Builder<E> addAll(Iterable<? extends E> elements) {
            super.addAll(elements);
            return this;
        }

        @Override
        @CanIgnoreReturnValue
        public Builder<E> addAll(Iterator<? extends E> elements) {
            super.addAll(elements);
            return this;
        }

        @CanIgnoreReturnValue
        Builder<E> combine(Builder<E> other) {
            Objects.requireNonNull(this.impl);
            Objects.requireNonNull(other.impl);
            this.copyIfNecessary();
            this.impl = this.impl.combine(other.impl);
            return this;
        }

        @Override
        public ImmutableSet<E> build() {
            Objects.requireNonNull(this.impl);
            this.forceCopy = true;
            this.impl = this.impl.review();
            return this.impl.build();
        }
    }

    @J2ktIncompatible
    private static final class SerializedForm
    implements Serializable {
        final Object[] elements;
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        SerializedForm(Object[] elements) {
            this.elements = elements;
        }

        Object readResolve() {
            return ImmutableSet.copyOf(this.elements);
        }
    }

    private static final class JdkBackedSetBuilderImpl<E>
    extends SetBuilderImpl<E> {
        private final Set<Object> delegate;

        JdkBackedSetBuilderImpl(SetBuilderImpl<E> toCopy) {
            super(toCopy);
            this.delegate = Sets.newHashSetWithExpectedSize(this.distinct);
            for (int i = 0; i < this.distinct; ++i) {
                this.delegate.add(Objects.requireNonNull(this.dedupedElements[i]));
            }
        }

        @Override
        SetBuilderImpl<E> add(E e) {
            Preconditions.checkNotNull(e);
            if (this.delegate.add(e)) {
                this.addDedupedElement(e);
            }
            return this;
        }

        @Override
        SetBuilderImpl<E> copy() {
            return new JdkBackedSetBuilderImpl<E>(this);
        }

        @Override
        ImmutableSet<E> build() {
            switch (this.distinct) {
                case 0: {
                    return ImmutableSet.of();
                }
                case 1: {
                    return ImmutableSet.of(Objects.requireNonNull(this.dedupedElements[0]));
                }
            }
            return new JdkBackedImmutableSet(this.delegate, ImmutableList.asImmutableList(this.dedupedElements, this.distinct));
        }
    }

    private static final class EmptySetBuilderImpl<E>
    extends SetBuilderImpl<E> {
        private static final EmptySetBuilderImpl<Object> INSTANCE = new EmptySetBuilderImpl();

        static <E> SetBuilderImpl<E> instance() {
            return INSTANCE;
        }

        private EmptySetBuilderImpl() {
            super(0);
        }

        @Override
        SetBuilderImpl<E> add(E e) {
            return new RegularSetBuilderImpl<E>(4).add(e);
        }

        @Override
        SetBuilderImpl<E> copy() {
            return this;
        }

        @Override
        ImmutableSet<E> build() {
            return ImmutableSet.of();
        }
    }

    static abstract class Indexed<E>
    extends CachingAsList<E> {
        Indexed() {
        }

        abstract E get(int var1);

        @Override
        public UnmodifiableIterator<E> iterator() {
            return this.asList().iterator();
        }

        @Override
        public Spliterator<E> spliterator() {
            return CollectSpliterators.indexed(this.size(), 1297, this::get);
        }

        @Override
        public void forEach(Consumer<? super E> consumer) {
            Preconditions.checkNotNull(consumer);
            int n = this.size();
            for (int i = 0; i < n; ++i) {
                consumer.accept(this.get(i));
            }
        }

        @Override
        int copyIntoArray(@Nullable Object[] dst, int offset) {
            return this.asList().copyIntoArray(dst, offset);
        }

        @Override
        ImmutableList<E> createAsList() {
            return new ImmutableAsList<E>(){

                @Override
                public E get(int index) {
                    return this.get(index);
                }

                @Override
                Indexed<E> delegateCollection() {
                    return this;
                }

                @Override
                @J2ktIncompatible
                @GwtIncompatible
                Object writeReplace() {
                    return super.writeReplace();
                }
            };
        }

        @Override
        @J2ktIncompatible
        @GwtIncompatible
        Object writeReplace() {
            return super.writeReplace();
        }
    }

    static abstract class CachingAsList<E>
    extends ImmutableSet<E> {
        @LazyInit
        @RetainedWith
        private transient @Nullable ImmutableList<E> asList;

        CachingAsList() {
        }

        @Override
        public ImmutableList<E> asList() {
            ImmutableList<E> result = this.asList;
            if (result == null) {
                this.asList = this.createAsList();
                return this.asList;
            }
            return result;
        }

        ImmutableList<E> createAsList() {
            return new RegularImmutableAsList(this, this.toArray());
        }

        @Override
        @J2ktIncompatible
        @GwtIncompatible
        Object writeReplace() {
            return super.writeReplace();
        }
    }
}

