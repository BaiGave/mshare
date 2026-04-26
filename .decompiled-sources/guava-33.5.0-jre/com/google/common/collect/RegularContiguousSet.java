/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractSequentialIterator;
import com.google.common.collect.BoundType;
import com.google.common.collect.Collections2;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.EmptyContiguousSet;
import com.google.common.collect.ImmutableAsList;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

@GwtCompatible
final class RegularContiguousSet<C extends Comparable>
extends ContiguousSet<C> {
    private final Range<C> range;
    @GwtIncompatible
    @J2ktIncompatible
    private static final long serialVersionUID = 0L;

    RegularContiguousSet(Range<C> range, DiscreteDomain<C> domain) {
        super(domain);
        this.range = range;
    }

    private ContiguousSet<C> intersectionInCurrentDomain(Range<C> other) {
        return this.range.isConnected(other) ? ContiguousSet.create(this.range.intersection(other), this.domain) : new EmptyContiguousSet(this.domain);
    }

    @Override
    ContiguousSet<C> headSetImpl(C toElement, boolean inclusive) {
        return this.intersectionInCurrentDomain(Range.upTo(toElement, BoundType.forBoolean(inclusive)));
    }

    @Override
    ContiguousSet<C> subSetImpl(C fromElement, boolean fromInclusive, C toElement, boolean toInclusive) {
        if (fromElement.compareTo(toElement) == 0 && !fromInclusive && !toInclusive) {
            return new EmptyContiguousSet(this.domain);
        }
        return this.intersectionInCurrentDomain(Range.range(fromElement, BoundType.forBoolean(fromInclusive), toElement, BoundType.forBoolean(toInclusive)));
    }

    @Override
    ContiguousSet<C> tailSetImpl(C fromElement, boolean inclusive) {
        return this.intersectionInCurrentDomain(Range.downTo(fromElement, BoundType.forBoolean(inclusive)));
    }

    @Override
    @GwtIncompatible
    int indexOf(@Nullable Object target) {
        if (!this.contains(target)) {
            return -1;
        }
        Comparable c = (Comparable)Objects.requireNonNull(target);
        return (int)this.domain.distance(this.first(), c);
    }

    @Override
    public UnmodifiableIterator<C> iterator() {
        return new AbstractSequentialIterator<C>((Comparable)this.first()){
            final C last;
            {
                this.last = RegularContiguousSet.this.last();
            }

            @Override
            protected @Nullable C computeNext(C previous) {
                return RegularContiguousSet.equalsOrThrow(previous, this.last) ? null : (Object)RegularContiguousSet.this.domain.next(previous);
            }
        };
    }

    @Override
    @GwtIncompatible
    public UnmodifiableIterator<C> descendingIterator() {
        return new AbstractSequentialIterator<C>((Comparable)this.last()){
            final C first;
            {
                this.first = RegularContiguousSet.this.first();
            }

            @Override
            protected @Nullable C computeNext(C previous) {
                return RegularContiguousSet.equalsOrThrow(previous, this.first) ? null : (Object)RegularContiguousSet.this.domain.previous(previous);
            }
        };
    }

    private static boolean equalsOrThrow(Comparable<?> left, @Nullable Comparable<?> right) {
        return right != null && Range.compareOrThrow(left, right) == 0;
    }

    @Override
    boolean isPartialView() {
        return false;
    }

    @Override
    public C first() {
        return (C)((Comparable)Objects.requireNonNull(this.range.lowerBound.leastValueAbove(this.domain)));
    }

    @Override
    public C last() {
        return (C)((Comparable)Objects.requireNonNull(this.range.upperBound.greatestValueBelow(this.domain)));
    }

    @Override
    ImmutableList<C> createAsList() {
        if (this.domain.supportsFastOffset) {
            return new ImmutableAsList<C>(){

                @Override
                ImmutableSortedSet<C> delegateCollection() {
                    return RegularContiguousSet.this;
                }

                @Override
                public C get(int i) {
                    Preconditions.checkElementIndex(i, this.size());
                    return RegularContiguousSet.this.domain.offset(RegularContiguousSet.this.first(), i);
                }

                @Override
                @J2ktIncompatible
                @GwtIncompatible
                Object writeReplace() {
                    return super.writeReplace();
                }
            };
        }
        return super.createAsList();
    }

    @Override
    public int size() {
        long distance = this.domain.distance(this.first(), this.last());
        return distance >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)distance + 1;
    }

    @Override
    public boolean contains(@Nullable Object object) {
        if (object == null) {
            return false;
        }
        try {
            Comparable c = (Comparable)object;
            return this.range.contains(c);
        }
        catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public boolean containsAll(Collection<?> targets) {
        return Collections2.containsAllImpl(this, targets);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ContiguousSet<C> intersection(ContiguousSet<C> other) {
        Comparable upperEndpoint;
        Preconditions.checkNotNull(other);
        Preconditions.checkArgument(this.domain.equals(other.domain));
        if (other.isEmpty()) {
            return other;
        }
        Comparable lowerEndpoint = Ordering.natural().max(this.first(), (Comparable)other.first());
        return lowerEndpoint.compareTo(upperEndpoint = Ordering.natural().min(this.last(), (Comparable)other.last())) <= 0 ? ContiguousSet.create(Range.closed(lowerEndpoint, upperEndpoint), this.domain) : new EmptyContiguousSet(this.domain);
    }

    @Override
    public Range<C> range() {
        return this.range(BoundType.CLOSED, BoundType.CLOSED);
    }

    @Override
    public Range<C> range(BoundType lowerBoundType, BoundType upperBoundType) {
        return Range.create(this.range.lowerBound.withLowerBoundType(lowerBoundType, this.domain), this.range.upperBound.withUpperBoundType(upperBoundType, this.domain));
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof RegularContiguousSet) {
            RegularContiguousSet that = (RegularContiguousSet)object;
            if (this.domain.equals(that.domain)) {
                return this.first().equals(that.first()) && this.last().equals(that.last());
            }
        }
        return super.equals(object);
    }

    @Override
    public int hashCode() {
        return Sets.hashCodeImpl(this);
    }

    @Override
    @GwtIncompatible
    @J2ktIncompatible
    Object writeReplace() {
        return new SerializedForm(this.range, this.domain);
    }

    @GwtIncompatible
    @J2ktIncompatible
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Use SerializedForm");
    }

    @GwtIncompatible
    @J2ktIncompatible
    private static final class SerializedForm<C extends Comparable>
    implements Serializable {
        final Range<C> range;
        final DiscreteDomain<C> domain;

        private SerializedForm(Range<C> range, DiscreteDomain<C> domain) {
            this.range = range;
            this.domain = domain;
        }

        private Object readResolve() {
            return new RegularContiguousSet<C>(this.range, this.domain);
        }
    }
}

