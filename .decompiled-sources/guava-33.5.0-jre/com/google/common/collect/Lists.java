/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.CartesianList;
import com.google.common.collect.CollectPreconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.ParametricNullness;
import com.google.common.collect.TransformedListIterator;
import com.google.common.math.IntMath;
import com.google.common.primitives.Ints;
import com.google.errorprone.annotations.InlineMe;
import java.io.Serializable;
import java.math.RoundingMode;
import java.util.AbstractList;
import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public final class Lists {
    private Lists() {
    }

    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList();
    }

    @SafeVarargs
    public static <E> ArrayList<E> newArrayList(E ... elements) {
        Preconditions.checkNotNull(elements);
        int capacity = Lists.computeArrayListCapacity(elements.length);
        ArrayList list = new ArrayList(capacity);
        Collections.addAll(list, elements);
        return list;
    }

    public static <E> ArrayList<E> newArrayList(Iterable<? extends E> elements) {
        Preconditions.checkNotNull(elements);
        return elements instanceof Collection ? new ArrayList<E>((Collection)elements) : Lists.newArrayList(elements.iterator());
    }

    public static <E> ArrayList<E> newArrayList(Iterator<? extends E> elements) {
        ArrayList list = new ArrayList();
        Iterators.addAll(list, elements);
        return list;
    }

    @VisibleForTesting
    static int computeArrayListCapacity(int arraySize) {
        CollectPreconditions.checkNonnegative(arraySize, "arraySize");
        return Ints.saturatedCast(5L + (long)arraySize + (long)(arraySize / 10));
    }

    public static <E> ArrayList<E> newArrayListWithCapacity(int initialArraySize) {
        CollectPreconditions.checkNonnegative(initialArraySize, "initialArraySize");
        return new ArrayList(initialArraySize);
    }

    public static <E> ArrayList<E> newArrayListWithExpectedSize(int estimatedSize) {
        return new ArrayList(Lists.computeArrayListCapacity(estimatedSize));
    }

    public static <E> LinkedList<E> newLinkedList() {
        return new LinkedList();
    }

    public static <E> LinkedList<E> newLinkedList(Iterable<? extends E> elements) {
        LinkedList list = new LinkedList();
        Iterables.addAll(list, elements);
        return list;
    }

    @J2ktIncompatible
    @GwtIncompatible
    @InlineMe(replacement="new CopyOnWriteArrayList<>()", imports={"java.util.concurrent.CopyOnWriteArrayList"})
    public static <E> CopyOnWriteArrayList<E> newCopyOnWriteArrayList() {
        return new CopyOnWriteArrayList();
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static <E> CopyOnWriteArrayList<E> newCopyOnWriteArrayList(Iterable<? extends E> elements) {
        ArrayList<? extends E> elementsCollection = elements instanceof Collection ? (ArrayList<? extends E>)elements : Lists.newArrayList(elements);
        return new CopyOnWriteArrayList<E>(elementsCollection);
    }

    public static <E> List<E> asList(@ParametricNullness E first, E[] rest) {
        return new OnePlusArrayList<E>(first, rest);
    }

    public static <E> List<E> asList(@ParametricNullness E first, @ParametricNullness E second, E[] rest) {
        return new TwoPlusArrayList<E>(first, second, rest);
    }

    public static <B> List<List<B>> cartesianProduct(List<? extends List<? extends B>> lists) {
        return CartesianList.create(lists);
    }

    @SafeVarargs
    public static <B> List<List<B>> cartesianProduct(List<? extends B> ... lists) {
        return Lists.cartesianProduct(Arrays.asList(lists));
    }

    public static <F, T> List<T> transform(List<F> fromList, Function<? super F, ? extends T> function) {
        return fromList instanceof RandomAccess ? new TransformingRandomAccessList<F, T>(fromList, function) : new TransformingSequentialList<F, T>(fromList, function);
    }

    public static <T> List<List<T>> partition(List<T> list, int size) {
        Preconditions.checkNotNull(list);
        Preconditions.checkArgument(size > 0);
        return list instanceof RandomAccess ? new RandomAccessPartition<T>(list, size) : new Partition<T>(list, size);
    }

    public static ImmutableList<Character> charactersOf(String string) {
        return new StringAsImmutableList(Preconditions.checkNotNull(string));
    }

    public static List<Character> charactersOf(CharSequence sequence) {
        return new CharSequenceAsList(Preconditions.checkNotNull(sequence));
    }

    public static <T> List<T> reverse(List<T> list) {
        if (list instanceof ImmutableList) {
            ImmutableList reversed;
            ImmutableList result = reversed = ((ImmutableList)list).reverse();
            return result;
        }
        if (list instanceof ReverseList) {
            return ((ReverseList)list).getForwardList();
        }
        if (list instanceof RandomAccess) {
            return new RandomAccessReverseList<T>(list);
        }
        return new ReverseList<T>(list);
    }

    static int hashCodeImpl(List<?> list) {
        int hashCode = 1;
        for (Object o : list) {
            hashCode = 31 * hashCode + (o == null ? 0 : o.hashCode());
            hashCode = ~(~hashCode);
        }
        return hashCode;
    }

    static boolean equalsImpl(List<?> thisList, @Nullable Object other) {
        if (other == Preconditions.checkNotNull(thisList)) {
            return true;
        }
        if (!(other instanceof List)) {
            return false;
        }
        List otherList = (List)other;
        int size = thisList.size();
        if (size != otherList.size()) {
            return false;
        }
        if (thisList instanceof RandomAccess && otherList instanceof RandomAccess) {
            for (int i = 0; i < size; ++i) {
                if (Objects.equals(thisList.get(i), otherList.get(i))) continue;
                return false;
            }
            return true;
        }
        return Iterators.elementsEqual(thisList.iterator(), otherList.iterator());
    }

    static <E> boolean addAllImpl(List<E> list, int index, Iterable<? extends E> elements) {
        boolean changed = false;
        ListIterator<E> listIterator = list.listIterator(index);
        for (E e : elements) {
            listIterator.add(e);
            changed = true;
        }
        return changed;
    }

    static int indexOfImpl(List<?> list, @Nullable Object element) {
        if (list instanceof RandomAccess) {
            return Lists.indexOfRandomAccess(list, element);
        }
        ListIterator<?> listIterator = list.listIterator();
        while (listIterator.hasNext()) {
            if (!Objects.equals(element, listIterator.next())) continue;
            return listIterator.previousIndex();
        }
        return -1;
    }

    private static int indexOfRandomAccess(List<?> list, @Nullable Object element) {
        int size = list.size();
        if (element == null) {
            for (int i = 0; i < size; ++i) {
                if (list.get(i) != null) continue;
                return i;
            }
        } else {
            for (int i = 0; i < size; ++i) {
                if (!element.equals(list.get(i))) continue;
                return i;
            }
        }
        return -1;
    }

    static int lastIndexOfImpl(List<?> list, @Nullable Object element) {
        if (list instanceof RandomAccess) {
            return Lists.lastIndexOfRandomAccess(list, element);
        }
        ListIterator<?> listIterator = list.listIterator(list.size());
        while (listIterator.hasPrevious()) {
            if (!Objects.equals(element, listIterator.previous())) continue;
            return listIterator.nextIndex();
        }
        return -1;
    }

    private static int lastIndexOfRandomAccess(List<?> list, @Nullable Object element) {
        if (element == null) {
            for (int i = list.size() - 1; i >= 0; --i) {
                if (list.get(i) != null) continue;
                return i;
            }
        } else {
            for (int i = list.size() - 1; i >= 0; --i) {
                if (!element.equals(list.get(i))) continue;
                return i;
            }
        }
        return -1;
    }

    static <E> ListIterator<E> listIteratorImpl(List<E> list, int index) {
        return new AbstractListWrapper<E>(list).listIterator(index);
    }

    static <E> List<E> subListImpl(List<E> list, int fromIndex, int toIndex) {
        AbstractListWrapper wrapper = list instanceof RandomAccess ? new RandomAccessListWrapper<E>((List)list){
            @GwtIncompatible
            @J2ktIncompatible
            private static final long serialVersionUID = 0L;

            @Override
            public ListIterator<E> listIterator(int index) {
                return this.backingList.listIterator(index);
            }
        } : new AbstractListWrapper<E>((List)list){
            @GwtIncompatible
            @J2ktIncompatible
            private static final long serialVersionUID = 0L;

            @Override
            public ListIterator<E> listIterator(int index) {
                return this.backingList.listIterator(index);
            }
        };
        return wrapper.subList(fromIndex, toIndex);
    }

    private static final class OnePlusArrayList<E>
    extends AbstractList<E>
    implements Serializable,
    RandomAccess {
        @ParametricNullness
        final E first;
        final E[] rest;
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        OnePlusArrayList(@ParametricNullness E first, E[] rest) {
            this.first = first;
            this.rest = Preconditions.checkNotNull(rest);
        }

        @Override
        public int size() {
            return IntMath.saturatedAdd(this.rest.length, 1);
        }

        @Override
        @ParametricNullness
        public E get(int index) {
            Preconditions.checkElementIndex(index, this.size());
            return index == 0 ? this.first : this.rest[index - 1];
        }
    }

    private static final class TwoPlusArrayList<E>
    extends AbstractList<E>
    implements Serializable,
    RandomAccess {
        @ParametricNullness
        final E first;
        @ParametricNullness
        final E second;
        final E[] rest;
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        TwoPlusArrayList(@ParametricNullness E first, @ParametricNullness E second, E[] rest) {
            this.first = first;
            this.second = second;
            this.rest = Preconditions.checkNotNull(rest);
        }

        @Override
        public int size() {
            return IntMath.saturatedAdd(this.rest.length, 2);
        }

        @Override
        @ParametricNullness
        public E get(int index) {
            switch (index) {
                case 0: {
                    return this.first;
                }
                case 1: {
                    return this.second;
                }
            }
            Preconditions.checkElementIndex(index, this.size());
            return this.rest[index - 2];
        }
    }

    private static final class TransformingRandomAccessList<F, T>
    extends AbstractList<T>
    implements RandomAccess,
    Serializable {
        final List<F> fromList;
        final Function<? super F, ? extends T> function;
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        TransformingRandomAccessList(List<F> fromList, Function<? super F, ? extends T> function) {
            this.fromList = Preconditions.checkNotNull(fromList);
            this.function = Preconditions.checkNotNull(function);
        }

        @Override
        protected void removeRange(int fromIndex, int toIndex) {
            this.fromList.subList(fromIndex, toIndex).clear();
        }

        @Override
        @ParametricNullness
        public T get(int index) {
            return this.function.apply(this.fromList.get(index));
        }

        @Override
        public Iterator<T> iterator() {
            return this.listIterator();
        }

        @Override
        public ListIterator<T> listIterator(int index) {
            return new TransformedListIterator<F, T>(this.fromList.listIterator(index)){

                @Override
                T transform(F from) {
                    return function.apply(from);
                }
            };
        }

        @Override
        public boolean isEmpty() {
            return this.fromList.isEmpty();
        }

        @Override
        public boolean removeIf(Predicate<? super T> filter) {
            Preconditions.checkNotNull(filter);
            return this.fromList.removeIf((? super E element) -> filter.test((T)this.function.apply(element)));
        }

        @Override
        @ParametricNullness
        public T remove(int index) {
            return this.function.apply(this.fromList.remove(index));
        }

        @Override
        public int size() {
            return this.fromList.size();
        }
    }

    private static final class TransformingSequentialList<F, T>
    extends AbstractSequentialList<T>
    implements Serializable {
        final List<F> fromList;
        final Function<? super F, ? extends T> function;
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        TransformingSequentialList(List<F> fromList, Function<? super F, ? extends T> function) {
            this.fromList = Preconditions.checkNotNull(fromList);
            this.function = Preconditions.checkNotNull(function);
        }

        @Override
        protected void removeRange(int fromIndex, int toIndex) {
            this.fromList.subList(fromIndex, toIndex).clear();
        }

        @Override
        public int size() {
            return this.fromList.size();
        }

        @Override
        public boolean isEmpty() {
            return this.fromList.isEmpty();
        }

        @Override
        public ListIterator<T> listIterator(int index) {
            return new TransformedListIterator<F, T>(this.fromList.listIterator(index)){

                @Override
                @ParametricNullness
                T transform(@ParametricNullness F from) {
                    return function.apply(from);
                }
            };
        }

        @Override
        public boolean removeIf(Predicate<? super T> filter) {
            Preconditions.checkNotNull(filter);
            return this.fromList.removeIf((? super E element) -> filter.test((T)this.function.apply(element)));
        }
    }

    private static final class RandomAccessPartition<T>
    extends Partition<T>
    implements RandomAccess {
        RandomAccessPartition(List<T> list, int size) {
            super(list, size);
        }
    }

    private static class Partition<T>
    extends AbstractList<List<T>> {
        final List<T> list;
        final int size;

        Partition(List<T> list, int size) {
            this.list = list;
            this.size = size;
        }

        @Override
        public List<T> get(int index) {
            Preconditions.checkElementIndex(index, this.size());
            int start = index * this.size;
            int end = Math.min(start + this.size, this.list.size());
            return this.list.subList(start, end);
        }

        @Override
        public int size() {
            return IntMath.divide(this.list.size(), this.size, RoundingMode.CEILING);
        }

        @Override
        public boolean isEmpty() {
            return this.list.isEmpty();
        }
    }

    private static final class StringAsImmutableList
    extends ImmutableList<Character> {
        private final String string;

        StringAsImmutableList(String string) {
            this.string = string;
        }

        @Override
        public int indexOf(@Nullable Object object) {
            return object instanceof Character ? this.string.indexOf(((Character)object).charValue()) : -1;
        }

        @Override
        public int lastIndexOf(@Nullable Object object) {
            return object instanceof Character ? this.string.lastIndexOf(((Character)object).charValue()) : -1;
        }

        @Override
        public ImmutableList<Character> subList(int fromIndex, int toIndex) {
            Preconditions.checkPositionIndexes(fromIndex, toIndex, this.size());
            return Lists.charactersOf(this.string.substring(fromIndex, toIndex));
        }

        @Override
        boolean isPartialView() {
            return false;
        }

        @Override
        public Character get(int index) {
            Preconditions.checkElementIndex(index, this.size());
            return Character.valueOf(this.string.charAt(index));
        }

        @Override
        public int size() {
            return this.string.length();
        }

        @Override
        @J2ktIncompatible
        @GwtIncompatible
        Object writeReplace() {
            return super.writeReplace();
        }
    }

    private static final class CharSequenceAsList
    extends AbstractList<Character> {
        private final CharSequence sequence;

        CharSequenceAsList(CharSequence sequence) {
            this.sequence = sequence;
        }

        @Override
        public Character get(int index) {
            Preconditions.checkElementIndex(index, this.size());
            return Character.valueOf(this.sequence.charAt(index));
        }

        @Override
        public int size() {
            return this.sequence.length();
        }
    }

    private static class ReverseList<T>
    extends AbstractList<T> {
        private final List<T> forwardList;

        ReverseList(List<T> forwardList) {
            this.forwardList = Preconditions.checkNotNull(forwardList);
        }

        List<T> getForwardList() {
            return this.forwardList;
        }

        private int reverseIndex(int index) {
            int size = this.size();
            Preconditions.checkElementIndex(index, size);
            return size - 1 - index;
        }

        private int reversePosition(int index) {
            int size = this.size();
            Preconditions.checkPositionIndex(index, size);
            return size - index;
        }

        @Override
        public void add(int index, @ParametricNullness T element) {
            this.forwardList.add(this.reversePosition(index), element);
        }

        @Override
        public void clear() {
            this.forwardList.clear();
        }

        @Override
        @ParametricNullness
        public T remove(int index) {
            return this.forwardList.remove(this.reverseIndex(index));
        }

        @Override
        protected void removeRange(int fromIndex, int toIndex) {
            this.subList(fromIndex, toIndex).clear();
        }

        @Override
        @ParametricNullness
        public T set(int index, @ParametricNullness T element) {
            return this.forwardList.set(this.reverseIndex(index), element);
        }

        @Override
        @ParametricNullness
        public T get(int index) {
            return this.forwardList.get(this.reverseIndex(index));
        }

        @Override
        public int size() {
            return this.forwardList.size();
        }

        @Override
        public List<T> subList(int fromIndex, int toIndex) {
            Preconditions.checkPositionIndexes(fromIndex, toIndex, this.size());
            return Lists.reverse(this.forwardList.subList(this.reversePosition(toIndex), this.reversePosition(fromIndex)));
        }

        @Override
        public Iterator<T> iterator() {
            return this.listIterator();
        }

        @Override
        public ListIterator<T> listIterator(int index) {
            int start = this.reversePosition(index);
            final ListIterator<T> forwardIterator = this.forwardList.listIterator(start);
            return new ListIterator<T>(){
                boolean canRemoveOrSet;
                final /* synthetic */ ReverseList this$0;
                {
                    this.this$0 = this$0;
                }

                @Override
                public void add(@ParametricNullness T e) {
                    forwardIterator.add(e);
                    forwardIterator.previous();
                    this.canRemoveOrSet = false;
                }

                @Override
                public boolean hasNext() {
                    return forwardIterator.hasPrevious();
                }

                @Override
                public boolean hasPrevious() {
                    return forwardIterator.hasNext();
                }

                @Override
                @ParametricNullness
                public T next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    this.canRemoveOrSet = true;
                    return forwardIterator.previous();
                }

                @Override
                public int nextIndex() {
                    return this.this$0.reversePosition(forwardIterator.nextIndex());
                }

                @Override
                @ParametricNullness
                public T previous() {
                    if (!this.hasPrevious()) {
                        throw new NoSuchElementException();
                    }
                    this.canRemoveOrSet = true;
                    return forwardIterator.next();
                }

                @Override
                public int previousIndex() {
                    return this.nextIndex() - 1;
                }

                @Override
                public void remove() {
                    CollectPreconditions.checkRemove(this.canRemoveOrSet);
                    forwardIterator.remove();
                    this.canRemoveOrSet = false;
                }

                @Override
                public void set(@ParametricNullness T e) {
                    Preconditions.checkState(this.canRemoveOrSet);
                    forwardIterator.set(e);
                }
            };
        }
    }

    private static final class RandomAccessReverseList<T>
    extends ReverseList<T>
    implements RandomAccess {
        RandomAccessReverseList(List<T> forwardList) {
            super(forwardList);
        }
    }

    private static class AbstractListWrapper<E>
    extends AbstractList<E> {
        final List<E> backingList;

        AbstractListWrapper(List<E> backingList) {
            this.backingList = Preconditions.checkNotNull(backingList);
        }

        @Override
        public void add(int index, @ParametricNullness E element) {
            this.backingList.add(index, element);
        }

        @Override
        public boolean addAll(int index, Collection<? extends E> c) {
            return this.backingList.addAll(index, c);
        }

        @Override
        @ParametricNullness
        public E get(int index) {
            return this.backingList.get(index);
        }

        @Override
        @ParametricNullness
        public E remove(int index) {
            return this.backingList.remove(index);
        }

        @Override
        @ParametricNullness
        public E set(int index, @ParametricNullness E element) {
            return this.backingList.set(index, element);
        }

        @Override
        public boolean contains(@Nullable Object o) {
            return this.backingList.contains(o);
        }

        @Override
        public int size() {
            return this.backingList.size();
        }
    }

    private static class RandomAccessListWrapper<E>
    extends AbstractListWrapper<E>
    implements RandomAccess {
        RandomAccessListWrapper(List<E> backingList) {
            super(backingList);
        }
    }
}

