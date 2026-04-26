/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.ForwardingSet;
import com.google.common.collect.ForwardingSortedMap;
import com.google.common.collect.ParametricNullness;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public abstract class ForwardingSortedSet<E>
extends ForwardingSet<E>
implements SortedSet<E> {
    protected ForwardingSortedSet() {
    }

    @Override
    protected abstract SortedSet<E> delegate();

    @Override
    public @Nullable Comparator<? super E> comparator() {
        return this.delegate().comparator();
    }

    @Override
    @ParametricNullness
    public E first() {
        return this.delegate().first();
    }

    @Override
    public SortedSet<E> headSet(@ParametricNullness E toElement) {
        return this.delegate().headSet(toElement);
    }

    @Override
    @ParametricNullness
    public E last() {
        return this.delegate().last();
    }

    @Override
    public SortedSet<E> subSet(@ParametricNullness E fromElement, @ParametricNullness E toElement) {
        return this.delegate().subSet(fromElement, toElement);
    }

    @Override
    public SortedSet<E> tailSet(@ParametricNullness E fromElement) {
        return this.delegate().tailSet(fromElement);
    }

    /*
     * Issues handling annotations - annotations may be inaccurate
     */
    @Override
    protected boolean standardContains(@Nullable Object object) {
        try {
            @Nullable ForwardingSortedSet self = this;
            Object ceiling = self.tailSet(object).first();
            return ForwardingSortedMap.unsafeCompare(this.comparator(), ceiling, object) == 0;
        }
        catch (ClassCastException | NullPointerException | NoSuchElementException e) {
            return false;
        }
    }

    /*
     * Issues handling annotations - annotations may be inaccurate
     */
    @Override
    protected boolean standardRemove(@Nullable Object object) {
        try {
            @Nullable ForwardingSortedSet self = this;
            Iterator iterator = self.tailSet(object).iterator();
            if (iterator.hasNext()) {
                Object ceiling = iterator.next();
                if (ForwardingSortedMap.unsafeCompare(this.comparator(), ceiling, object) == 0) {
                    iterator.remove();
                    return true;
                }
            }
        }
        catch (ClassCastException | NullPointerException e) {
            return false;
        }
        return false;
    }

    protected SortedSet<E> standardSubSet(@ParametricNullness E fromElement, @ParametricNullness E toElement) {
        return this.tailSet(fromElement).headSet(toElement);
    }
}

