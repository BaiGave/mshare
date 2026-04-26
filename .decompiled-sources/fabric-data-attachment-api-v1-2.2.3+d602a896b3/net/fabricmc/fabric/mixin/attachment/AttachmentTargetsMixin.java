/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.attachment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.impl.attachment.AttachmentSerializingImpl;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.fabricmc.fabric.impl.attachment.AttachmentTypeImpl;
import net.fabricmc.fabric.impl.attachment.DataAccessorHandler;
import net.fabricmc.fabric.impl.attachment.GlobalAttachmentsImpl;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentChange;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentSync;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentTargetInfo;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={BlockEntity.class, Entity.class, Level.class, ChunkAccess.class, GlobalAttachmentsImpl.class})
abstract class AttachmentTargetsMixin
implements AttachmentTargetImpl {
    @Unique
    private @Nullable IdentityHashMap<AttachmentType<?>, Object> dataAttachments = null;
    @Unique
    private @Nullable IdentityHashMap<AttachmentType<?>, AttachmentChange> syncedAttachments = null;
    @Unique
    private @Nullable Set<AttachmentType<?>> deferredSyncedAttachments = null;
    @Unique
    private @Nullable IdentityHashMap<AttachmentType<?>, Event<AttachmentTarget.OnAttachedSet<?>>> attachedChangedListeners = null;

    AttachmentTargetsMixin() {
    }

    public <T> @Nullable T getAttached(AttachmentType<T> type) {
        return (T)(this.dataAttachments == null ? null : this.dataAttachments.get(type));
    }

    public <T> @Nullable T setAttached(AttachmentType<T> type, @Nullable T value) {
        Event<AttachmentTarget.OnAttachedSet<?>> event;
        Object oldValue;
        if (value == null) {
            oldValue = this.dataAttachments == null ? null : this.dataAttachments.remove(type);
        } else {
            if (this.dataAttachments == null) {
                this.dataAttachments = new IdentityHashMap();
            }
            oldValue = this.dataAttachments.put(type, value);
        }
        if (this.attachedChangedListeners != null && (event = this.attachedChangedListeners.get(type)) != null) {
            event.invoker().onAttachedSet(oldValue, value);
        }
        if (!Objects.equals(oldValue, value)) {
            this.fabric_markChanged(type);
            if (this.fabric_shouldTryToSync() && type.isSynced()) {
                AttachmentChange change = AttachmentChange.create(this.fabric_getSyncTargetInfo(), type, value, this.fabric_getRegistryAccess());
                this.acknowledgeSyncedEntry(type, change);
                this.fabric_syncChange(type, change);
            }
        }
        return (T)oldValue;
    }

    @Override
    public boolean hasAttached(AttachmentType<?> type) {
        return this.dataAttachments != null && this.dataAttachments.containsKey(type);
    }

    @Override
    public <A> Event<AttachmentTarget.OnAttachedSet<A>> onAttachedSet(AttachmentType<A> type) {
        if (this.attachedChangedListeners == null) {
            this.attachedChangedListeners = new IdentityHashMap();
        }
        return this.attachedChangedListeners.computeIfAbsent(type, t -> EventFactory.createArrayBacked(AttachmentTarget.OnAttachedSet.class, listeners -> (oldValue, newValue) -> {
            for (AttachmentTarget.OnAttachedSet listener : listeners) {
                listener.onAttachedSet(oldValue, newValue);
            }
        }));
    }

    @Override
    public void fabric_writeAttachmentsToNbt(ValueOutput output) {
        AttachmentSerializingImpl.serializeAttachmentData(output, this.dataAttachments);
    }

    @Override
    public void fabric_readAttachmentsFromNbt(ValueInput input) {
        if (DataAccessorHandler.APPLYING_DATA_CHANGE.isBound()) {
            return;
        }
        IdentityHashMap<AttachmentType<?>, Object> fromNbt = AttachmentSerializingImpl.deserializeAttachmentData(input);
        if (fromNbt == null) {
            return;
        }
        this.dataAttachments = fromNbt;
        if (this.fabric_shouldTryToSync() && this.dataAttachments != null) {
            this.dataAttachments.forEach((type, value) -> {
                if (type.isSynced()) {
                    this.acknowledgeSynced((AttachmentType<?>)type, value, input.lookup());
                }
            });
            this.fabric_clearDeferredSyncChanges();
        }
    }

    @Override
    public boolean fabric_hasPersistentAttachments() {
        return AttachmentSerializingImpl.hasPersistentAttachments(this.dataAttachments);
    }

    @Override
    public Map<AttachmentType<?>, ?> fabric_getAttachments() {
        return this.dataAttachments;
    }

    @Unique
    private void acknowledgeSynced(AttachmentType<?> type, Object value, HolderLookup.Provider registries) {
        RegistryAccess ra;
        RegistryAccess registryAccess = registries instanceof RegistryAccess ? (ra = (RegistryAccess)registries) : this.fabric_getRegistryAccess();
        this.acknowledgeSyncedEntry(type, AttachmentChange.create(this.fabric_getSyncTargetInfo(), type, value, registryAccess));
    }

    @Unique
    private void acknowledgeSyncedEntry(AttachmentType<?> type, @Nullable AttachmentChange change) {
        if (change == null) {
            if (this.syncedAttachments == null) {
                return;
            }
            this.syncedAttachments.remove(type);
            if (this.fabric_shouldDeferSync()) {
                this.deferredSyncedAttachments.add(type);
            }
        } else {
            if (this.syncedAttachments == null) {
                this.syncedAttachments = new IdentityHashMap();
            }
            this.syncedAttachments.put(type, change);
            if (this.fabric_shouldDeferSync()) {
                if (this.deferredSyncedAttachments == null) {
                    this.deferredSyncedAttachments = Collections.newSetFromMap(new IdentityHashMap());
                }
                this.deferredSyncedAttachments.add(type);
            }
        }
    }

    @Override
    public void fabric_computeInitialSyncChanges(ServerPlayer player, Consumer<AttachmentChange> changeOutput) {
        if (this.syncedAttachments == null) {
            return;
        }
        for (Map.Entry<AttachmentType<?>, AttachmentChange> entry : this.syncedAttachments.entrySet()) {
            if (!((AttachmentTypeImpl)entry.getKey()).syncPredicate().test(this, player)) continue;
            changeOutput.accept(entry.getValue());
        }
    }

    @Override
    public void fabric_sendAndClearDeferredSyncChanges(List<ServerPlayer> players) {
        if (this.syncedAttachments == null || this.deferredSyncedAttachments == null || this.deferredSyncedAttachments.isEmpty()) {
            return;
        }
        List<AttachmentChange> deferredChanges = this.deferredSyncedAttachments.stream().map(type -> {
            AttachmentChange change = this.syncedAttachments.get(type);
            if (change == null) {
                change = AttachmentChange.create(this.fabric_getSyncTargetInfo(), type, null, this.fabric_getRegistryAccess());
            }
            return change;
        }).toList();
        for (ServerPlayer player : players) {
            ArrayList<AttachmentChange> syncableChanges = new ArrayList<AttachmentChange>();
            for (AttachmentChange change : deferredChanges) {
                if (!((AttachmentTypeImpl)change.type()).syncPredicate().test(this, player)) continue;
                syncableChanges.add(change);
            }
            if (syncableChanges.isEmpty()) continue;
            AttachmentSync.trySync(syncableChanges, player);
        }
        this.deferredSyncedAttachments.clear();
    }

    @Override
    public void fabric_clearDeferredSyncChanges() {
        if (this.deferredSyncedAttachments != null) {
            this.deferredSyncedAttachments.clear();
        }
    }

    @Override
    public <T> void fabric_updateSyncTarget(AttachmentTargetInfo<T> oldTargetInfo, AttachmentTargetInfo<T> newTargetInfo) {
        if (this.syncedAttachments == null) {
            return;
        }
        this.syncedAttachments.replaceAll((attachmentType, attachmentChange) -> {
            if (attachmentChange.targetInfo().equals(oldTargetInfo)) {
                return attachmentChange.withNewTarget(newTargetInfo);
            }
            return attachmentChange;
        });
    }
}

