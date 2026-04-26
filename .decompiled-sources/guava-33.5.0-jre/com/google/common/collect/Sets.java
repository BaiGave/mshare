/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.AbstractIndexedListIterator;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.BoundType;
import com.google.common.collect.CartesianList;
import com.google.common.collect.CollectCollectors;
import com.google.common.collect.CollectPreconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ForwardingCollection;
import com.google.common.collect.ForwardingNavigableSet;
import com.google.common.collect.ForwardingSortedSet;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableEnumSet;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.collect.ParametricNullness;
import com.google.common.collect.Platform;
import com.google.common.collect.Range;
import com.google.common.collect.Synchronized;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.math.IntMath;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotCall;
import com.google.errorprone.annotations.InlineMe;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public final class Sets {
    private Sets() {
    }

    public static <E extends Enum<E>> ImmutableSet<E> immutableEnumSet(E anElement, E ... otherElements) {
        return ImmutableEnumSet.asImmutable(EnumSet.of(anElement, otherElements));
    }

    public static <E extends Enum<E>> ImmutableSet<E> immutableEnumSet(Iterable<E> elements) {
        if (elements instanceof ImmutableEnumSet) {
            return (ImmutableEnumSet)elements;
        }
        if (elements instanceof Collection) {
            Collection collection = (Collection)elements;
            if (collection.isEmpty()) {
                return ImmutableSet.of();
            }
            return ImmutableEnumSet.asImmutable(EnumSet.copyOf(collection));
        }
        Iterator<E> itr = elements.iterator();
        if (itr.hasNext()) {
            EnumSet<Enum> enumSet = EnumSet.of((Enum)itr.next());
            Iterators.addAll(enumSet, itr);
            return ImmutableEnumSet.asImmutable(enumSet);
        }
        return ImmutableSet.of();
    }

    public static <E extends Enum<E>> Collector<E, ?, ImmutableSet<E>> toImmutableEnumSet() {
        return CollectCollectors.toImmutableEnumSet();
    }

    public static <E extends Enum<E>> EnumSet<E> newEnumSet(Iterable<E> iterable, Class<E> elementType) {
        EnumSet<E> set = EnumSet.noneOf(elementType);
        Iterables.addAll(set, iterable);
        return set;
    }

    public static <E> HashSet<E> newHashSet() {
        return new HashSet();
    }

    public static <E> HashSet<E> newHashSet(E ... elements) {
        HashSet<E> set = Sets.newHashSetWithExpectedSize(elements.length);
        Collections.addAll(set, elements);
        return set;
    }

    public static <E> HashSet<E> newHashSet(Iterable<? extends E> elements) {
        return elements instanceof Collection ? new HashSet<E>((Collection)elements) : Sets.newHashSet(elements.iterator());
    }

    public static <E> HashSet<E> newHashSet(Iterator<? extends E> elements) {
        HashSet set = new HashSet();
        Iterators.addAll(set, elements);
        return set;
    }

    public static <E> HashSet<E> newHashSetWithExpectedSize(int expectedSize) {
        return new HashSet(Maps.capacity(expectedSize));
    }

    public static <E> Set<E> newConcurrentHashSet() {
        return Platform.newConcurrentHashSet();
    }

    public static <E> Set<E> newConcurrentHashSet(Iterable<? extends E> elements) {
        Set<E> set = Sets.newConcurrentHashSet();
        Iterables.addAll(set, elements);
        return set;
    }

    public static <E> LinkedHashSet<E> newLinkedHashSet() {
        return new LinkedHashSet();
    }

    public static <E> LinkedHashSet<E> newLinkedHashSet(Iterable<? extends E> elements) {
        if (elements instanceof Collection) {
            return new LinkedHashSet((Collection)elements);
        }
        LinkedHashSet set = new LinkedHashSet();
        Iterables.addAll(set, elements);
        return set;
    }

    public static <E> LinkedHashSet<E> newLinkedHashSetWithExpectedSize(int expectedSize) {
        return new LinkedHashSet(Maps.capacity(expectedSize));
    }

    public static <E extends Comparable> TreeSet<E> newTreeSet() {
        return new TreeSet();
    }

    public static <E extends Comparable> TreeSet<E> newTreeSet(Iterable<? extends E> elements) {
        TreeSet<E> set = Sets.newTreeSet();
        Iterables.addAll(set, elements);
        return set;
    }

    public static <E> TreeSet<E> newTreeSet(Comparator<? super E> comparator) {
        return new TreeSet<E>(Preconditions.checkNotNull(comparator));
    }

    public static <E> Set<E> newIdentityHashSet() {
        return Collections.newSetFromMap(Maps.newIdentityHashMap());
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static <E> CopyOnWriteArraySet<E> newCopyOnWriteArraySet() {
        return new CopyOnWriteArraySet();
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static <E> CopyOnWriteArraySet<E> newCopyOnWriteArraySet(Iterable<? extends E> elements) {
        ArrayList<? extends E> elementsCollection = elements instanceof Collection ? (ArrayList<? extends E>)elements : Lists.newArrayList(elements);
        return new CopyOnWriteArraySet<E>(elementsCollection);
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static <E extends Enum<E>> EnumSet<E> complementOf(Collection<E> collection) {
        if (collection instanceof EnumSet) {
            return EnumSet.complementOf((EnumSet)collection);
        }
        Preconditions.checkArgument(!collection.isEmpty(), "collection is empty; use the other version of this method");
        Class type = ((Enum)collection.iterator().next()).getDeclaringClass();
        return Sets.makeComplementByHand(collection, type);
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static <E extends Enum<E>> EnumSet<E> complementOf(Collection<E> collection, Class<E> type) {
        Preconditions.checkNotNull(collection);
        return collection instanceof EnumSet ? EnumSet.complementOf((EnumSet)collection) : Sets.makeComplementByHand(collection, type);
    }

    @J2ktIncompatible
    @GwtIncompatible
    private static <E extends Enum<E>> EnumSet<E> makeComplementByHand(Collection<E> collection, Class<E> type) {
        EnumSet<E> result = EnumSet.allOf(type);
        result.removeAll(collection);
        return result;
    }

    @Deprecated
    @InlineMe(replacement="Collections.newSetFromMap(map)", imports={"java.util.Collections"})
    public static <E> Set<E> newSetFromMap(Map<E, Boolean> map) {
        return Collections.newSetFromMap(map);
    }

    public static <E> SetView<E> union(final Set<? extends E> set1, final Set<? extends E> set2) {
        Preconditions.checkNotNull(set1, "set1");
        Preconditions.checkNotNull(set2, "set2");
        return new SetView<E>(){

            @Override
            public int size() {
                int size = set1.size();
                for (Object e : set2) {
                    if (set1.contains(e)) continue;
                    ++size;
                }
                return size;
            }

            @Override
            public boolean isEmpty() {
                return set1.isEmpty() && set2.isEmpty();
            }

            @Override
            public UnmodifiableIterator<E> iterator() {
                return new AbstractIterator<E>(this){
                    final Iterator<? extends E> itr1;
                    final Iterator<? extends E> itr2;
                    final /* synthetic */ 1 this$0;
                    {
                        this.this$0 = this$0;
                        this.itr1 = set1.iterator();
                        this.itr2 = set2.iterator();
                    }

                    @Override
                    protected @Nullable E computeNext() {
                        if (this.itr1.hasNext()) {
                            return this.itr1.next();
                        }
                        while (this.itr2.hasNext()) {
                            Object e = this.itr2.next();
                            if (set1.contains(e)) continue;
                            return e;
                        }
                        return this.endOfData();
                    }
                };
            }

            @Override
            public Stream<E> stream() {
                return Stream.concat(set1.stream(), set2.stream().filter(e -> !set1.contains(e)));
            }

            @Override
            public Stream<E> parallelStream() {
                return (Stream)this.stream().parallel();
            }

            @Override
            public boolean contains(@Nullable Object object) {
                return set1.contains(object) || set2.contains(object);
            }

            @Override
            public <S extends Set<E>> S copyInto(S set) {
                set.addAll(set1);
                set.addAll(set2);
                return set;
            }

            @Override
            int minSize() {
                return Math.max(1.minSize(set1), 1.minSize(set2));
            }

            @Override
            int maxSize() {
                return IntMath.saturatedAdd(1.maxSize(set1), 1.maxSize(set2));
            }
        };
    }

    public static <E> SetView<E> intersection(final Set<E> set1, final Set<?> set2) {
        Preconditions.checkNotNull(set1, "set1");
        Preconditions.checkNotNull(set2, "set2");
        return new SetView<E>(){

            @Override
            public UnmodifiableIterator<E> iterator() {
                return new AbstractIterator<E>(this){
                    final Iterator<E> itr;
                    final /* synthetic */ 2 this$0;
                    {
                        this.this$0 = this$0;
                        this.itr = set1.iterator();
                    }

                    @Override
                    protected @Nullable E computeNext() {
                        while (this.itr.hasNext()) {
                            Object e = this.itr.next();
                            if (!set2.contains(e)) continue;
                            return e;
                        }
                        return this.endOfData();
                    }
                };
            }

            @Override
            public Stream<E> stream() {
                return set1.stream().filter(set2::contains);
            }

            @Override
            public Stream<E> parallelStream() {
                return set1.parallelStream().filter(set2::contains);
            }

            @Override
            public int size() {
                int size = 0;
                for (Object e : set1) {
                    if (!set2.contains(e)) continue;
                    ++size;
                }
                return size;
            }

            @Override
            public boolean isEmpty() {
                return Collections.disjoint(set2, set1);
            }

            @Override
            public boolean contains(@Nullable Object object) {
                return set1.contains(object) && set2.contains(object);
            }

            @Override
            public boolean containsAll(Collection<?> collection) {
                return set1.containsAll(collection) && set2.containsAll(collection);
            }

            @Override
            int minSize() {
                return 0;
            }

            @Override
            int maxSize() {
                return Math.min(2.maxSize(set1), 2.maxSize(set2));
            }
        };
    }

    public static <E> SetView<E> difference(final Set<E> set1, final Set<?> set2) {
        Preconditions.checkNotNull(set1, "set1");
        Preconditions.checkNotNull(set2, "set2");
        return new SetView<E>(){

            @Override
            public UnmodifiableIterator<E> iterator() {
                return new AbstractIterator<E>(this){
                    final Iterator<E> itr;
                    final /* synthetic */ 3 this$0;
                    {
                        this.this$0 = this$0;
                        this.itr = set1.iterator();
                    }

                    @Override
                    protected @Nullable E computeNext() {
                        while (this.itr.hasNext()) {
                            Object e = this.itr.next();
                            if (set2.contains(e)) continue;
                            return e;
                        }
                        return this.endOfData();
                    }
                };
            }

            @Override
            public Stream<E> stream() {
                return set1.stream().filter(e -> !set2.contains(e));
            }

            @Override
            public Stream<E> parallelStream() {
                return set1.parallelStream().filter(e -> !set2.contains(e));
            }

            @Override
            public int size() {
                int size = 0;
                for (Object e : set1) {
                    if (set2.contains(e)) continue;
                    ++size;
                }
                return size;
            }

            @Override
            public boolean isEmpty() {
                return set2.containsAll(set1);
            }

            @Override
            public boolean contains(@Nullable Object element) {
                return set1.contains(element) && !set2.contains(element);
            }

            @Override
            int minSize() {
                return Math.max(3.minSize(set1) - 3.maxSize(set2), 0);
            }

            @Override
            int maxSize() {
                return 3.maxSize(set1);
            }
        };
    }

    public static <E> SetView<E> symmetricDifference(final Set<? extends E> set1, final Set<? extends E> set2) {
        Preconditions.checkNotNull(set1, "set1");
        Preconditions.checkNotNull(set2, "set2");
        return new SetView<E>(){

            @Override
            public UnmodifiableIterator<E> iterator() {
                final Iterator itr1 = set1.iterator();
                final Iterator itr2 = set2.iterator();
                return new AbstractIterator<E>(this){
                    final /* synthetic */ 4 this$0;
                    {
                        this.this$0 = this$0;
                    }

                    @Override
                    public @Nullable E computeNext() {
                        while (itr1.hasNext()) {
                            Object elem1 = itr1.next();
                            if (set2.contains(elem1)) continue;
                            return elem1;
                        }
                        while (itr2.hasNext()) {
                            Object elem2 = itr2.next();
                            if (set1.contains(elem2)) continue;
                            return elem2;
                        }
                        return this.endOfData();
                    }
                };
            }

            @Override
            public int size() {
                int size = 0;
                for (Object e : set1) {
                    if (set2.contains(e)) continue;
                    ++size;
                }
                for (Object e : set2) {
                    if (set1.contains(e)) continue;
                    ++size;
                }
                return size;
            }

            @Override
            public boolean isEmpty() {
                return set1.equals(set2);
            }

            @Override
            public boolean contains(@Nullable Object element) {
                return set1.contains(element) ^ set2.contains(element);
            }

            @Override
            int minSize() {
                int difference = 4.minSize(set1) - 4.maxSize(set2);
                return difference >= 0 ? difference : Math.max(4.minSize(set2) - 4.maxSize(set1), 0);
            }

            @Override
            int maxSize() {
                return IntMath.saturatedAdd(4.maxSize(set1), 4.maxSize(set2));
            }
        };
    }

    public static <E> Set<E> filter(Set<E> unfiltered, com.google.common.base.Predicate<? super E> predicate) {
        if (unfiltered instanceof SortedSet) {
            return Sets.filter((SortedSet)unfiltered, predicate);
        }
        if (unfiltered instanceof FilteredSet) {
            FilteredSet filtered = (FilteredSet)unfiltered;
            com.google.common.base.Predicate<? super E> combinedPredicate = Predicates.and(filtered.predicate, predicate);
            return new FilteredSet<E>((Set)filtered.unfiltered, combinedPredicate);
        }
        return new FilteredSet<E>(Preconditions.checkNotNull(unfiltered), Preconditions.checkNotNull(predicate));
    }

    public static <E> SortedSet<E> filter(SortedSet<E> unfiltered, com.google.common.base.Predicate<? super E> predicate) {
        if (unfiltered instanceof FilteredSet) {
            FilteredSet filtered = (FilteredSet)((Object)unfiltered);
            com.google.common.base.Predicate<? super E> combinedPredicate = Predicates.and(filtered.predicate, predicate);
            return new FilteredSortedSet<E>((SortedSet)filtered.unfiltered, combinedPredicate);
        }
        return new FilteredSortedSet<E>(Preconditions.checkNotNull(unfiltered), Preconditions.checkNotNull(predicate));
    }

    @GwtIncompatible
    public static <E> NavigableSet<E> filter(NavigableSet<E> unfiltered, com.google.common.base.Predicate<? super E> predicate) {
        if (unfiltered instanceof FilteredSet) {
            FilteredSet filtered = (FilteredSet)((Object)unfiltered);
            com.google.common.base.Predicate<? super E> combinedPredicate = Predicates.and(filtered.predicate, predicate);
            return new FilteredNavigableSet<E>((NavigableSet)filtered.unfiltered, combinedPredicate);
        }
        return new FilteredNavigableSet<E>(Preconditions.checkNotNull(unfiltered), Preconditions.checkNotNull(predicate));
    }

    public static <B> Set<List<B>> cartesianProduct(List<? extends Set<? extends B>> sets) {
        return CartesianSet.create(sets);
    }

    @SafeVarargs
    public static <B> Set<List<B>> cartesianProduct(Set<? extends B> ... sets) {
        return Sets.cartesianProduct(Arrays.asList(sets));
    }

    public static <E> Set<Set<E>> powerSet(Set<E> set) {
        return new PowerSet<E>(set);
    }

    public static <E> Set<Set<E>> combinations(Set<E> set, final int size) {
        final ImmutableMap<E, Integer> index = Maps.indexMap(set);
        CollectPreconditions.checkNonnegative(size, "size");
        Preconditions.checkArgument(size <= index.size(), "size (%s) must be <= set.size() (%s)", size, index.size());
        if (size == 0) {
            return ImmutableSet.of(ImmutableSet.of());
        }
        if (size == index.size()) {
            return ImmutableSet.of(index.keySet());
        }
        return new AbstractSet<Set<E>>(){

            @Override
            public boolean contains(@Nullable Object o) {
                if (o instanceof Set) {
                    Set s = (Set)o;
                    return s.size() == size && ((AbstractCollection)((Object)index.keySet())).containsAll(s);
                }
                return false;
            }

            @Override
            public Iterator<Set<E>> iterator() {
                return new AbstractIterator<Set<E>>(this){
                    final BitSet bits;
                    final /* synthetic */ 5 this$0;
                    {
                        this.this$0 = this$0;
                        this.bits = new BitSet(index.size());
                    }

                    @Override
                    protected @Nullable Set<E> computeNext() {
                        if (this.bits.isEmpty()) {
                            this.bits.set(0, size);
                        } else {
                            int firstSetBit = this.bits.nextSetBit(0);
                            int bitToFlip = this.bits.nextClearBit(firstSetBit);
                            if (bitToFlip == index.size()) {
                                return (Set)this.endOfData();
                            }
                            this.bits.set(0, bitToFlip - firstSetBit - 1);
                            this.bits.clear(bitToFlip - firstSetBit - 1, bitToFlip);
                            this.bits.set(bitToFlip);
                        }
                        final BitSet copy = (BitSet)this.bits.clone();
                        return new AbstractSet<E>(this){
                            final /* synthetic */ 1 this$1;
                            {
                                this.this$1 = this$1;
                            }

                            @Override
                            public boolean contains(@Nullable Object o) {
                                Integer i = (Integer)index.get(o);
                                return i != null && copy.get(i);
                            }

                            @Override
                            public Iterator<E> iterator() {
                                return new AbstractIterator<E>(this){
                                    int i = -1;
                                    final /* synthetic */ 1 this$2;
                                    {
                                        this.this$2 = this$2;
                                    }

                                    @Override
                                    protected @Nullable E computeNext() {
                                        this.i = copy.nextSetBit(this.i + 1);
                                        if (this.i == -1) {
                                            return this.endOfData();
                                        }
                                        return ((ImmutableCollection)((Object)index.keySet())).asList().get(this.i);
                                    }
                                };
                            }

                            @Override
                            public int size() {
                                return size;
                            }
                        };
                    }
                };
            }

            @Override
            public int size() {
                return IntMath.binomial(index.size(), size);
            }

            @Override
            public String toString() {
                return "Sets.combinations(" + index.keySet() + ", " + size + ")";
            }
        };
    }

    static int hashCodeImpl(Set<?> s) {
        int hashCode = 0;
        for (Object o : s) {
            hashCode += o != null ? o.hashCode() : 0;
            hashCode = ~(~hashCode);
        }
        return hashCode;
    }

    static boolean equalsImpl(Set<?> s, @Nullable Object object) {
        if (s == object) {
            return true;
        }
        if (object instanceof Set) {
            Set o = (Set)object;
            try {
                return s.size() == o.size() && s.containsAll(o);
            }
            catch (ClassCastException | NullPointerException ignored) {
                return false;
            }
        }
        return false;
    }

    public static <E> NavigableSet<E> unmodifiableNavigableSet(NavigableSet<E> set) {
        if (set instanceof ImmutableCollection || set instanceof UnmodifiableNavigableSet) {
            return set;
        }
        return new UnmodifiableNavigableSet<E>(set);
    }

    @GwtIncompatible
    @J2ktIncompatible
    public static <E> NavigableSet<E> synchronizedNavigableSet(NavigableSet<E> navigableSet) {
        return Synchronized.navigableSet(navigableSet);
    }

    static boolean removeAllImpl(Set<?> set, Iterator<?> iterator) {
        boolean changed = false;
        while (iterator.hasNext()) {
            changed |= set.remove(iterator.next());
        }
        return changed;
    }

    static boolean removeAllImpl(Set<?> set, Collection<?> collection) {
        Preconditions.checkNotNull(collection);
        if (collection instanceof Multiset) {
            collection = ((Multiset)collection).elementSet();
        }
        if (collection instanceof Set && collection.size() > set.size()) {
            return Iterators.removeAll(set.iterator(), collection);
        }
        return Sets.removeAllImpl(set, collection.iterator());
    }

    @GwtIncompatible
    public static <K extends Comparable<? super K>> NavigableSet<K> subSet(NavigableSet<K> set, Range<K> range) {
        if (set.comparator() != null && set.comparator() != Ordering.natural() && range.hasLowerBound() && range.hasUpperBound()) {
            Preconditions.checkArgument(set.comparator().compare(range.lowerEndpoint(), range.upperEndpoint()) <= 0, "set is using a custom comparator which is inconsistent with the natural ordering.");
        }
        if (range.hasLowerBound() && range.hasUpperBound()) {
            return set.subSet(range.lowerEndpoint(), range.lowerBoundType() == BoundType.CLOSED, range.upperEndpoint(), range.upperBoundType() == BoundType.CLOSED);
        }
        if (range.hasLowerBound()) {
            return set.tailSet(range.lowerEndpoint(), range.lowerBoundType() == BoundType.CLOSED);
        }
        if (range.hasUpperBound()) {
            return set.headSet(range.upperEndpoint(), range.upperBoundType() == BoundType.CLOSED);
        }
        return Preconditions.checkNotNull(set);
    }

    private static class FilteredSet<E>
    extends Collections2.FilteredCollection<E>
    implements Set<E> {
        FilteredSet(Set<E> unfiltered, com.google.common.base.Predicate<? super E> predicate) {
            super(unfiltered, predicate);
        }

        @Override
        public boolean equals(@Nullable Object object) {
            return Sets.equalsImpl(this, object);
        }

        @Override
        public int hashCode() {
            return Sets.hashCodeImpl(this);
        }
    }

    private static class FilteredSortedSet<E>
    extends FilteredSet<E>
    implements SortedSet<E> {
        FilteredSortedSet(SortedSet<E> unfiltered, com.google.common.base.Predicate<? super E> predicate) {
            super(unfiltered, predicate);
        }

        @Override
        public @Nullable Comparator<? super E> comparator() {
            return ((SortedSet)this.unfiltered).comparator();
        }

        @Override
        public SortedSet<E> subSet(@ParametricNullness E fromElement, @ParametricNullness E toElement) {
            return new FilteredSortedSet<E>(((SortedSet)this.unfiltered).subSet(fromElement, toElement), this.predicate);
        }

        @Override
        public SortedSet<E> headSet(@ParametricNullness E toElement) {
            return new FilteredSortedSet<E>(((SortedSet)this.unfiltered).headSet(toElement), this.predicate);
        }

        @Override
        public SortedSet<E> tailSet(@ParametricNullness E fromElement) {
            return new FilteredSortedSet<E>(((SortedSet)this.unfiltered).tailSet(fromElement), this.predicate);
        }

        @Override
        @ParametricNullness
        public E first() {
            return Iterators.find(this.unfiltered.iterator(), this.predicate);
        }

        @Override
        @ParametricNullness
        public E last() {
            SortedSet sortedUnfiltered = (SortedSet)this.unfiltered;
            Object element;
            while (!this.predicate.apply(element = sortedUnfiltered.last())) {
                sortedUnfiltered = sortedUnfiltered.headSet(element);
            }
            return element;
        }
    }

    @GwtIncompatible
    private static final class FilteredNavigableSet<E>
    extends FilteredSortedSet<E>
    implements NavigableSet<E> {
        FilteredNavigableSet(NavigableSet<E> unfiltered, com.google.common.base.Predicate<? super E> predicate) {
            super(unfiltered, predicate);
        }

        NavigableSet<E> unfiltered() {
            return (NavigableSet)this.unfiltered;
        }

        @Override
        public @Nullable E lower(@ParametricNullness E e) {
            return Iterators.find(this.unfiltered().headSet(e, false).descendingIterator(), this.predicate, null);
        }

        @Override
        public @Nullable E floor(@ParametricNullness E e) {
            return Iterators.find(this.unfiltered().headSet(e, true).descendingIterator(), this.predicate, null);
        }

        @Override
        public @Nullable E ceiling(@ParametricNullness E e) {
            return Iterables.find(this.unfiltered().tailSet(e, true), this.predicate, null);
        }

        @Override
        public @Nullable E higher(@ParametricNullness E e) {
            return Iterables.find(this.unfiltered().tailSet(e, false), this.predicate, null);
        }

        @Override
        public @Nullable E pollFirst() {
            return Iterables.removeFirstMatching(this.unfiltered(), this.predicate);
        }

        @Override
        public @Nullable E pollLast() {
            return Iterables.removeFirstMatching(this.unfiltered().descendingSet(), this.predicate);
        }

        @Override
        public NavigableSet<E> descendingSet() {
            return Sets.filter(this.unfiltered().descendingSet(), this.predicate);
        }

        @Override
        public Iterator<E> descendingIterator() {
            return Iterators.filter(this.unfiltered().descendingIterator(), this.predicate);
        }

        @Override
        @ParametricNullness
        public E last() {
            return Iterators.find(this.unfiltered().descendingIterator(), this.predicate);
        }

        @Override
        public NavigableSet<E> subSet(@ParametricNullness E fromElement, boolean fromInclusive, @ParametricNullness E toElement, boolean toInclusive) {
            return Sets.filter(this.unfiltered().subSet(fromElement, fromInclusive, toElement, toInclusive), this.predicate);
        }

        @Override
        public NavigableSet<E> headSet(@ParametricNullness E toElement, boolean inclusive) {
            return Sets.filter(this.unfiltered().headSet(toElement, inclusive), this.predicate);
        }

        @Override
        public NavigableSet<E> tailSet(@ParametricNullness E fromElement, boolean inclusive) {
            return Sets.filter(this.unfiltered().tailSet(fromElement, inclusive), this.predicate);
        }
    }

    private static final class CartesianSet<E>
    extends ForwardingCollection<List<E>>
    implements Set<List<E>> {
        private final transient ImmutableList<ImmutableSet<E>> axes;
        private final transient CartesianList<E> delegate;

        static <E> Set<List<E>> create(List<? extends Set<? extends E>> sets) {
            ImmutableList.Builder axesBuilder = new ImmutableList.Builder(sets.size());
            for (Set<E> set : sets) {
                ImmutableSet<E> copy = ImmutableSet.copyOf(set);
                if (copy.isEmpty()) {
                    return ImmutableSet.of();
                }
                axesBuilder.add(copy);
            }
            ImmutableCollection axes = axesBuilder.build();
            ImmutableList immutableList = new ImmutableList<List<E>>((ImmutableList)axes){
                final /* synthetic */ ImmutableList val$axes;
                {
                    this.val$axes = immutableList;
                }

                @Override
                public int size() {
                    return this.val$axes.size();
                }

                @Override
                public List<E> get(int index) {
                    return ((ImmutableSet)this.val$axes.get(index)).asList();
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
            };
            return new CartesianSet(axes, new CartesianList(immutableList));
        }

        private CartesianSet(ImmutableList<ImmutableSet<E>> axes, CartesianList<E> delegate) {
            this.axes = axes;
            this.delegate = delegate;
        }

        @Override
        protected Collection<List<E>> delegate() {
            return this.delegate;
        }

        @Override
        public boolean contains(@Nullable Object object) {
            if (!(object instanceof List)) {
                return false;
            }
            List list = (List)object;
            if (list.size() != this.axes.size()) {
                return false;
            }
            int i = 0;
            for (Object o : list) {
                if (!((ImmutableSet)this.axes.get(i)).contains(o)) {
                    return false;
                }
                ++i;
            }
            return true;
        }

        @Override
        public boolean equals(@Nullable Object object) {
            if (object instanceof CartesianSet) {
                CartesianSet that = (CartesianSet)object;
                return this.axes.equals(that.axes);
            }
            if (object instanceof Set) {
                Set that = (Set)object;
                return this.size() == that.size() && this.containsAll(that);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int adjust = this.size() - 1;
            for (int i = 0; i < this.axes.size(); ++i) {
                adjust *= 31;
                adjust = ~(~adjust);
            }
            int hash = 1;
            for (Set set : this.axes) {
                hash = 31 * hash + this.size() / set.size() * set.hashCode();
                hash = ~(~hash);
            }
            return ~(~(hash += adjust));
        }
    }

    private static final class PowerSet<E>
    extends AbstractSet<Set<E>> {
        final ImmutableMap<E, Integer> inputSet;

        PowerSet(Set<E> input) {
            Preconditions.checkArgument(input.size() <= 30, "Too many elements to create power set: %s > 30", input.size());
            this.inputSet = Maps.indexMap(input);
        }

        @Override
        public int size() {
            return 1 << this.inputSet.size();
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public Iterator<Set<E>> iterator() {
            return new AbstractIndexedListIterator<Set<E>>(this.size()){

                @Override
                protected Set<E> get(int setBits) {
                    return new SubSet(inputSet, setBits);
                }
            };
        }

        @Override
        public boolean contains(@Nullable Object obj) {
            if (obj instanceof Set) {
                Set set = (Set)obj;
                return ((AbstractCollection)((Object)this.inputSet.keySet())).containsAll(set);
            }
            return false;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof PowerSet) {
                PowerSet that = (PowerSet)obj;
                return ((ImmutableSet)this.inputSet.keySet()).equals(that.inputSet.keySet());
            }
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return ((ImmutableSet)this.inputSet.keySet()).hashCode() << this.inputSet.size() - 1;
        }

        @Override
        public String toString() {
            return "powerSet(" + this.inputSet + ")";
        }
    }

    static final class UnmodifiableNavigableSet<E>
    extends ForwardingSortedSet<E>
    implements NavigableSet<E>,
    Serializable {
        private final NavigableSet<E> delegate;
        private final SortedSet<E> unmodifiableDelegate;
        @LazyInit
        private transient @Nullable UnmodifiableNavigableSet<E> descendingSet;
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        UnmodifiableNavigableSet(NavigableSet<E> delegate) {
            this.delegate = Preconditions.checkNotNull(delegate);
            this.unmodifiableDelegate = Collections.unmodifiableSortedSet(delegate);
        }

        @Override
        protected SortedSet<E> delegate() {
            return this.unmodifiableDelegate;
        }

        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Stream<E> stream() {
            return this.delegate.stream();
        }

        @Override
        public Stream<E> parallelStream() {
            return this.delegate.parallelStream();
        }

        @Override
        public void forEach(Consumer<? super E> action) {
            this.delegate.forEach(action);
        }

        @Override
        public @Nullable E lower(@ParametricNullness E e) {
            return this.delegate.lower(e);
        }

        @Override
        public @Nullable E floor(@ParametricNullness E e) {
            return this.delegate.floor(e);
        }

        @Override
        public @Nullable E ceiling(@ParametricNullness E e) {
            return this.delegate.ceiling(e);
        }

        @Override
        public @Nullable E higher(@ParametricNullness E e) {
            return this.delegate.higher(e);
        }

        @Override
        public @Nullable E pollFirst() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @Nullable E pollLast() {
            throw new UnsupportedOperationException();
        }

        @Override
        public NavigableSet<E> descendingSet() {
            UnmodifiableNavigableSet<E> result = this.descendingSet;
            if (result == null) {
                result = this.descendingSet = new UnmodifiableNavigableSet<E>(this.delegate.descendingSet());
                result.descendingSet = this;
            }
            return result;
        }

        @Override
        public Iterator<E> descendingIterator() {
            return Iterators.unmodifiableIterator(this.delegate.descendingIterator());
        }

        @Override
        public NavigableSet<E> subSet(@ParametricNullness E fromElement, boolean fromInclusive, @ParametricNullness E toElement, boolean toInclusive) {
            return Sets.unmodifiableNavigableSet(this.delegate.subSet(fromElement, fromInclusive, toElement, toInclusive));
        }

        @Override
        public NavigableSet<E> headSet(@ParametricNullness E toElement, boolean inclusive) {
            return Sets.unmodifiableNavigableSet(this.delegate.headSet(toElement, inclusive));
        }

        @Override
        public NavigableSet<E> tailSet(@ParametricNullness E fromElement, boolean inclusive) {
            return Sets.unmodifiableNavigableSet(this.delegate.tailSet(fromElement, inclusive));
        }
    }

    @GwtIncompatible
    static class DescendingSet<E>
    extends ForwardingNavigableSet<E> {
        private final NavigableSet<E> forward;

        DescendingSet(NavigableSet<E> forward) {
            this.forward = forward;
        }

        @Override
        protected NavigableSet<E> delegate() {
            return this.forward;
        }

        @Override
        public @Nullable E lower(@ParametricNullness E e) {
            return this.forward.higher(e);
        }

        @Override
        public @Nullable E floor(@ParametricNullness E e) {
            return this.forward.ceiling(e);
        }

        @Override
        public @Nullable E ceiling(@ParametricNullness E e) {
            return this.forward.floor(e);
        }

        @Override
        public @Nullable E higher(@ParametricNullness E e) {
            return this.forward.lower(e);
        }

        @Override
        public @Nullable E pollFirst() {
            return this.forward.pollLast();
        }

        @Override
        public @Nullable E pollLast() {
            return this.forward.pollFirst();
        }

        @Override
        public NavigableSet<E> descendingSet() {
            return this.forward;
        }

        @Override
        public Iterator<E> descendingIterator() {
            return this.forward.iterator();
        }

        @Override
        public NavigableSet<E> subSet(@ParametricNullness E fromElement, boolean fromInclusive, @ParametricNullness E toElement, boolean toInclusive) {
            return this.forward.subSet(toElement, toInclusive, fromElement, fromInclusive).descendingSet();
        }

        @Override
        public SortedSet<E> subSet(@ParametricNullness E fromElement, @ParametricNullness E toElement) {
            return this.standardSubSet(fromElement, toElement);
        }

        @Override
        public NavigableSet<E> headSet(@ParametricNullness E toElement, boolean inclusive) {
            return this.forward.tailSet(toElement, inclusive).descendingSet();
        }

        @Override
        public SortedSet<E> headSet(@ParametricNullness E toElement) {
            return this.standardHeadSet(toElement);
        }

        @Override
        public NavigableSet<E> tailSet(@ParametricNullness E fromElement, boolean inclusive) {
            return this.forward.headSet(fromElement, inclusive).descendingSet();
        }

        @Override
        public SortedSet<E> tailSet(@ParametricNullness E fromElement) {
            return this.standardTailSet(fromElement);
        }

        @Override
        public Comparator<? super E> comparator() {
            Comparator forwardComparator = this.forward.comparator();
            if (forwardComparator == null) {
                return Ordering.natural().reverse();
            }
            return DescendingSet.reverse(forwardComparator);
        }

        private static <T> Ordering<T> reverse(Comparator<T> forward) {
            return Ordering.from(forward).reverse();
        }

        @Override
        @ParametricNullness
        public E first() {
            return this.forward.last();
        }

        @Override
        @ParametricNullness
        public E last() {
            return this.forward.first();
        }

        @Override
        public Iterator<E> iterator() {
            return this.forward.descendingIterator();
        }

        @Override
        public @Nullable Object[] toArray() {
            return this.standardToArray();
        }

        @Override
        public <T> T[] toArray(T[] array) {
            return this.standardToArray(array);
        }

        @Override
        public String toString() {
            return this.standardToString();
        }
    }

    private static final class SubSet<E>
    extends AbstractSet<E> {
        private final ImmutableMap<E, Integer> inputSet;
        private final int mask;

        SubSet(ImmutableMap<E, Integer> inputSet, int mask) {
            this.inputSet = inputSet;
            this.mask = mask;
        }

        @Override
        public Iterator<E> iterator() {
            return new UnmodifiableIterator<E>(){
                final ImmutableList<E> elements;
                int remainingSetBits;
                {
                    this.elements = ((ImmutableCollection)((Object)inputSet.keySet())).asList();
                    this.remainingSetBits = mask;
                }

                @Override
                public boolean hasNext() {
                    return this.remainingSetBits != 0;
                }

                @Override
                public E next() {
                    int index = Integer.numberOfTrailingZeros(this.remainingSetBits);
                    if (index == 32) {
                        throw new NoSuchElementException();
                    }
                    this.remainingSetBits &= ~(1 << index);
                    return this.elements.get(index);
                }
            };
        }

        @Override
        public int size() {
            return Integer.bitCount(this.mask);
        }

        @Override
        public boolean contains(@Nullable Object o) {
            Integer index = this.inputSet.get(o);
            return index != null && (this.mask & 1 << index) != 0;
        }
    }

    public static abstract class SetView<E>
    extends AbstractSet<E> {
        private SetView() {
        }

        public ImmutableSet<@NonNull E> immutableCopy() {
            int maxSize = this.maxSize();
            if (maxSize == 0) {
                return ImmutableSet.of();
            }
            ImmutableSet.Builder<@NonNull E> builder = ImmutableSet.builderWithExpectedSize(maxSize);
            for (Object element : this) {
                builder.add(Preconditions.checkNotNull(element));
            }
            return builder.build();
        }

        @CanIgnoreReturnValue
        public <S extends Set<E>> S copyInto(S set) {
            set.addAll(this);
            return set;
        }

        @Override
        @Deprecated
        @CanIgnoreReturnValue
        @DoNotCall(value="Always throws UnsupportedOperationException")
        public final boolean add(@ParametricNullness E e) {
            throw new UnsupportedOperationException();
        }

        @Override
        @Deprecated
        @CanIgnoreReturnValue
        @DoNotCall(value="Always throws UnsupportedOperationException")
        public final boolean remove(@Nullable Object object) {
            throw new UnsupportedOperationException();
        }

        @Override
        @Deprecated
        @CanIgnoreReturnValue
        @DoNotCall(value="Always throws UnsupportedOperationException")
        public final boolean addAll(Collection<? extends E> newElements) {
            throw new UnsupportedOperationException();
        }

        @Override
        @Deprecated
        @CanIgnoreReturnValue
        @DoNotCall(value="Always throws UnsupportedOperationException")
        public final boolean removeAll(Collection<?> oldElements) {
            throw new UnsupportedOperationException();
        }

        @Override
        @Deprecated
        @CanIgnoreReturnValue
        @DoNotCall(value="Always throws UnsupportedOperationException")
        public final boolean removeIf(Predicate<? super E> filter) {
            throw new UnsupportedOperationException();
        }

        @Override
        @Deprecated
        @CanIgnoreReturnValue
        @DoNotCall(value="Always throws UnsupportedOperationException")
        public final boolean retainAll(Collection<?> elementsToKeep) {
            throw new UnsupportedOperationException();
        }

        @Override
        @Deprecated
        @DoNotCall(value="Always throws UnsupportedOperationException")
        public final void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public abstract UnmodifiableIterator<E> iterator();

        @Override
        public boolean equals(@Nullable Object object) {
            if (object == this) {
                return true;
            }
            if (!(object instanceof Set)) {
                return false;
            }
            Set that = (Set)object;
            int thatMaxSize = SetView.maxSize(that);
            if (this.minSize() > thatMaxSize) {
                return false;
            }
            int thatMinSize = SetView.minSize(that);
            if (this.maxSize() < thatMinSize) {
                return false;
            }
            int thisSize = 0;
            for (Object e : this) {
                try {
                    if (!that.contains(e)) {
                        return false;
                    }
                }
                catch (ClassCastException | NullPointerException ignored) {
                    return false;
                }
                ++thisSize;
            }
            if (thisSize == thatMaxSize) {
                return true;
            }
            if (thisSize < thatMinSize) {
                return false;
            }
            int thatSize = 0;
            for (Object unused : that) {
                if (++thatSize <= thisSize) continue;
                return false;
            }
            return true;
        }

        abstract int minSize();

        static int minSize(Set<?> set) {
            return set instanceof SetView ? ((SetView)set).minSize() : set.size();
        }

        abstract int maxSize();

        static int maxSize(Set<?> set) {
            return set instanceof SetView ? ((SetView)set).maxSize() : set.size();
        }
    }

    static abstract class ImprovedAbstractSet<E>
    extends AbstractSet<E> {
        ImprovedAbstractSet() {
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return Sets.removeAllImpl(this, c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return super.retainAll(Preconditions.checkNotNull(c));
        }
    }
}

