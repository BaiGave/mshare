/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.resource.pack;

import java.util.Set;
import java.util.function.Predicate;

public interface FabricPack {
    default public boolean fabric$isHidden() {
        return false;
    }

    default public boolean fabric$parentsEnabled(Set<String> enabled) {
        return true;
    }

    default public void fabric$setParentsPredicate(Predicate<Set<String>> predicate) {
    }
}

