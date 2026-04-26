/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.specs;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;

public interface IVec<T>
extends Serializable,
Cloneable {
    public int size();

    public void shrinkTo(int var1);

    public void pop();

    public void ensure(int var1);

    public IVec<T> push(T var1);

    public void clear();

    public T last();

    public T get(int var1);

    public void set(int var1, T var2);

    public void remove(T var1);

    public void removeFromLast(T var1);

    public T delete(int var1);

    public void copyTo(IVec<T> var1);

    public <E> void copyTo(E[] var1);

    public void moveTo(IVec<T> var1);

    public void moveTo(int var1, int var2);

    public void sort(Comparator<T> var1);

    public boolean isEmpty();

    public Iterator<T> iterator();
}

