/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.function;

import java.util.function.Predicate;

public class Predicates {
    private static final Predicate<?> TRUE = t -> true;
    private static final Predicate<?> FALSE = t -> false;

    public static <T> Predicate<T> falsePredicate() {
        return FALSE;
    }

    public static <T> Predicate<T> truePredicate() {
        return TRUE;
    }

    private Predicates() {
    }
}

