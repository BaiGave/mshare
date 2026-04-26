/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.storage;

import java.util.Objects;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;

public interface TransferVariant<O>
extends DataComponentHolder,
TypedInstance<O> {
    public boolean isBlank();

    public O getObject();

    public DataComponentPatch getComponentsPatch();

    @Override
    public DataComponentMap getComponents();

    default public boolean hasComponents() {
        return !this.getComponentsPatch().isEmpty();
    }

    default public boolean componentsMatch(DataComponentPatch other) {
        return Objects.equals(this.getComponentsPatch(), other);
    }

    default public boolean isOf(O object) {
        return this.getObject() == object;
    }

    default public TransferVariant<O> withComponents(DataComponentPatch patch) {
        throw new UnsupportedOperationException("withComponents is not supported by this TransferVariant");
    }
}

