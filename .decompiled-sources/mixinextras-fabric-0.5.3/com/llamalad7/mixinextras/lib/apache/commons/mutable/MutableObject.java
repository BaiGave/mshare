/*
 * Decompiled with CFR 0.152.
 */
package com.llamalad7.mixinextras.lib.apache.commons.mutable;

import java.io.Serializable;

public class MutableObject<T>
implements Serializable {
    private T value;

    public T getValue() {
        return this.value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (this.getClass() == obj.getClass()) {
            MutableObject that = (MutableObject)obj;
            return this.value.equals(that.value);
        }
        return false;
    }

    public int hashCode() {
        return this.value == null ? 0 : this.value.hashCode();
    }

    public String toString() {
        return this.value == null ? "null" : this.value.toString();
    }
}

