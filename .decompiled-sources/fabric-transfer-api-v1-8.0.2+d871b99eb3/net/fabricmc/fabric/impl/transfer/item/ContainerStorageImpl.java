/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.transfer.item;

import com.google.common.collect.MapMaker;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.fabricmc.fabric.impl.transfer.DebugMessages;
import net.fabricmc.fabric.impl.transfer.item.ContainerSlotWrapper;
import net.fabricmc.fabric.impl.transfer.item.PlayerInventoryStorageImpl;
import net.fabricmc.fabric.impl.transfer.item.SidedContainerStorageImpl;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.Nullable;

public class ContainerStorageImpl
extends CombinedStorage<ItemVariant, SingleSlotStorage<ItemVariant>>
implements ContainerStorage {
    private static final Map<Container, ContainerStorageImpl> WRAPPERS = new MapMaker().weakValues().makeMap();
    final Container container;
    final List<ContainerSlotWrapper> backingList;
    final SetChangedParticipant setChangedParticipant = new SetChangedParticipant(this);

    public static ContainerStorage of(Container inventory, @Nullable Direction direction) {
        ContainerStorageImpl storage = WRAPPERS.computeIfAbsent(inventory, inv -> {
            if (inv instanceof Inventory) {
                Inventory playerInventory = (Inventory)inv;
                return new PlayerInventoryStorageImpl(playerInventory);
            }
            return new ContainerStorageImpl((Container)inv);
        });
        storage.resizeSlotList();
        return storage.getSidedWrapper(direction);
    }

    ContainerStorageImpl(Container container) {
        super(Collections.emptyList());
        this.container = container;
        this.backingList = new ArrayList<ContainerSlotWrapper>();
    }

    @Override
    public List<SingleSlotStorage<ItemVariant>> getSlots() {
        return this.parts;
    }

    private void resizeSlotList() {
        int inventorySize = this.container.getContainerSize();
        if (inventorySize != this.parts.size()) {
            while (this.backingList.size() < inventorySize) {
                this.backingList.add(new ContainerSlotWrapper(this, this.backingList.size()));
            }
            this.parts = Collections.unmodifiableList(this.backingList.subList(0, inventorySize));
        }
    }

    private ContainerStorage getSidedWrapper(@Nullable Direction direction) {
        if (this.container instanceof WorldlyContainer && direction != null) {
            return new SidedContainerStorageImpl(this, direction);
        }
        return this;
    }

    @Override
    public String toString() {
        return "ContainerStorage[" + DebugMessages.forInventory(this.container) + "]";
    }

    class SetChangedParticipant
    extends SnapshotParticipant<Boolean> {
        final /* synthetic */ ContainerStorageImpl this$0;

        SetChangedParticipant(ContainerStorageImpl this$0) {
            ContainerStorageImpl containerStorageImpl = this$0;
            Objects.requireNonNull(containerStorageImpl);
            this.this$0 = containerStorageImpl;
        }

        @Override
        protected Boolean createSnapshot() {
            return Boolean.TRUE;
        }

        @Override
        protected void readSnapshot(Boolean snapshot) {
        }

        @Override
        protected void onFinalCommit() {
            this.this$0.container.setChanged();
        }
    }
}

