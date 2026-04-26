/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.AbstractList;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import org.jspecify.annotations.Nullable;

@GwtCompatible
final class CartesianList<E>
extends AbstractList<List<E>>
implements RandomAccess {
    private final transient ImmutableList<List<E>> axes;
    private final transient int[] axesSizeProduct;

    static <E> List<List<E>> create(List<? extends List<? extends E>> lists) {
        ImmutableList.Builder axesBuilder = new ImmutableList.Builder(lists.size());
        for (List<E> list : lists) {
            ImmutableList<E> copy = ImmutableList.copyOf(list);
            if (copy.isEmpty()) {
                return ImmutableList.of();
            }
            axesBuilder.add(copy);
        }
        return new CartesianList<E>(axesBuilder.build());
    }

    CartesianList(ImmutableList<List<E>> axes) {
        this.axes = axes;
        int[] axesSizeProduct = new int[axes.size() + 1];
        axesSizeProduct[axes.size()] = 1;
        try {
            for (int i = axes.size() - 1; i >= 0; --i) {
                axesSizeProduct[i] = Math.multiplyExact(axesSizeProduct[i + 1], ((List)axes.get(i)).size());
            }
        }
        catch (ArithmeticException e) {
            throw new IllegalArgumentException("Cartesian product too large; must have size at most Integer.MAX_VALUE");
        }
        this.axesSizeProduct = axesSizeProduct;
    }

    private int getAxisIndexForProductIndex(int index, int axis) {
        return index / this.axesSizeProduct[axis + 1] % ((List)this.axes.get(axis)).size();
    }

    @Override
    public int indexOf(@Nullable Object o) {
        if (!(o instanceof List)) {
            return -1;
        }
        List list = (List)o;
        if (list.size() != this.axes.size()) {
            return -1;
        }
        ListIterator itr = list.listIterator();
        int computedIndex = 0;
        while (itr.hasNext()) {
            int axisIndex = itr.nextIndex();
            int elemIndex = ((List)this.axes.get(axisIndex)).indexOf(itr.next());
            if (elemIndex == -1) {
                return -1;
            }
            computedIndex += elemIndex * this.axesSizeProduct[axisIndex + 1];
        }
        return computedIndex;
    }

    @Override
    public int lastIndexOf(@Nullable Object o) {
        if (!(o instanceof List)) {
            return -1;
        }
        List list = (List)o;
        if (list.size() != this.axes.size()) {
            return -1;
        }
        ListIterator itr = list.listIterator();
        int computedIndex = 0;
        while (itr.hasNext()) {
            int axisIndex = itr.nextIndex();
            int elemIndex = ((List)this.axes.get(axisIndex)).lastIndexOf(itr.next());
            if (elemIndex == -1) {
                return -1;
            }
            computedIndex += elemIndex * this.axesSizeProduct[axisIndex + 1];
        }
        return computedIndex;
    }

    @Override
    public ImmutableList<E> get(final int index) {
        Preconditions.checkElementIndex(index, this.size());
        return new ImmutableList<E>(this){
            final /* synthetic */ CartesianList this$0;
            {
                this.this$0 = this$0;
            }

            @Override
            public int size() {
                return this.this$0.axes.size();
            }

            @Override
            public E get(int axis) {
                Preconditions.checkElementIndex(axis, this.size());
                int axisIndex = this.this$0.getAxisIndexForProductIndex(index, axis);
                return ((List)this.this$0.axes.get(axis)).get(axisIndex);
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
    }

    @Override
    public int size() {
        return this.axesSizeProduct[0];
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
            if (!((List)this.axes.get(i)).contains(o)) {
                return false;
            }
            ++i;
        }
        return true;
    }
}

