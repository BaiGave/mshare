/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.item;

import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.impl.transfer.item.ContainerStorageImpl;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

@ApiStatus.NonExtendable
public interface ContainerStorage
extends SlottedStorage<ItemVariant> {
    public static ContainerStorage of(Container container, @Nullable Direction direction) {
        Objects.requireNonNull(container, "Null container is not supported.");
        return ContainerStorageImpl.of(container, direction);
    }

    @Override
    public @UnmodifiableView List<SingleSlotStorage<ItemVariant>> getSlots();

    @Override
    default public int getSlotCount() {
        return this.getSlots().size();
    }

    @Override
    default public SingleSlotStorage<ItemVariant> getSlot(int slot) {
        return this.getSlots().get(slot);
    }
}

