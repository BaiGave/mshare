/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.attachment;

import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.impl.attachment.AttachmentTypeImpl;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentSync;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AttachmentRegistryImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger("fabric-data-attachment-api-v1");
    private static final Map<Identifier, AttachmentType<?>> attachmentRegistry = new HashMap();
    private static final Set<Identifier> syncableAttachments = new HashSet<Identifier>();
    private static final Set<Identifier> syncableView = Collections.unmodifiableSet(syncableAttachments);
    private static int maxSyncPacketSize = AttachmentSync.DEFAULT_ATTACHMENT_SYNC_PACKET_SIZE;

    public static <A> void register(Identifier id, AttachmentType<A> attachmentType) {
        AttachmentType<A> existing = attachmentRegistry.put(id, attachmentType);
        if (existing != null) {
            LOGGER.warn("Encountered duplicate type registration for id {}", (Object)id);
            if (existing.isSynced() && !attachmentType.isSynced()) {
                syncableAttachments.remove(id);
            } else if (!existing.isSynced() && attachmentType.isSynced()) {
                syncableAttachments.add(id);
            }
        } else if (attachmentType.isSynced()) {
            syncableAttachments.add(id);
        }
    }

    public static @Nullable AttachmentType<?> get(Identifier id) {
        return attachmentRegistry.get(id);
    }

    public static Set<Identifier> getSyncableAttachments() {
        return syncableView;
    }

    public static <A> AttachmentRegistry.Builder<A> builder() {
        return new BuilderImpl();
    }

    public static int getMaxSyncPacketSize() {
        if (maxSyncPacketSize == -1) {
            throw new IllegalStateException("getMaxSyncPacketSize should only be called ONCE!");
        }
        int maxSize = maxSyncPacketSize;
        maxSyncPacketSize = -1;
        return maxSize;
    }

    public static class BuilderImpl<A>
    implements AttachmentRegistry.Builder<A> {
        private @Nullable Supplier<A> defaultInitializer = null;
        private @Nullable Codec<A> persistenceCodec = null;
        private @Nullable StreamCodec<? super RegistryFriendlyByteBuf, A> streamCodec = null;
        private @Nullable AttachmentSyncPredicate syncPredicate = null;
        private boolean copyOnDeath = false;
        private int maxSyncSize = -1;

        @Override
        public AttachmentRegistry.Builder<A> persistent(Codec<A> codec) {
            Objects.requireNonNull(codec, "codec cannot be null");
            this.persistenceCodec = codec;
            return this;
        }

        @Override
        public AttachmentRegistry.Builder<A> copyOnDeath() {
            this.copyOnDeath = true;
            return this;
        }

        @Override
        public AttachmentRegistry.Builder<A> initializer(Supplier<A> initializer) {
            Objects.requireNonNull(initializer, "initializer cannot be null");
            this.defaultInitializer = initializer;
            return this;
        }

        @Override
        public AttachmentRegistry.Builder<A> syncWith(StreamCodec<? super RegistryFriendlyByteBuf, A> streamCodec, AttachmentSyncPredicate syncPredicate) {
            Objects.requireNonNull(streamCodec, "stream codec cannot be null");
            Objects.requireNonNull(syncPredicate, "sync predicate cannot be null");
            this.streamCodec = streamCodec;
            this.syncPredicate = syncPredicate;
            return this;
        }

        @Override
        public AttachmentRegistry.Builder<A> syncWith(StreamCodec<? super RegistryFriendlyByteBuf, A> streamCodec, AttachmentSyncPredicate syncPredicate, int maxSyncSize) {
            if (maxSyncSize < 0) {
                throw new IllegalArgumentException("maxSyncSize must be positive!");
            }
            this.syncWith(streamCodec, syncPredicate);
            this.maxSyncSize = maxSyncSize;
            return this;
        }

        @Override
        public AttachmentType<A> buildAndRegister(Identifier id) {
            Objects.requireNonNull(id, "identifier cannot be null");
            if (this.syncPredicate != null && id.toString().length() > 256) {
                throw new IllegalArgumentException("Identifier length is too long for a synced attachment type (was %d, maximum is %d)".formatted(id.toString().length(), 256));
            }
            if (this.maxSyncSize <= AttachmentSync.DEFAULT_MAX_DATA_SIZE) {
                this.maxSyncSize = AttachmentSync.DEFAULT_MAX_DATA_SIZE;
            } else {
                if (maxSyncPacketSize == -1) {
                    throw new IllegalStateException("Large attachment " + String.valueOf(id) + " registered too late! Must be registered during mod initialization.");
                }
                int newMaxPacketSize = this.maxSyncSize + 265;
                newMaxPacketSize = newMaxPacketSize < 0 ? Integer.MAX_VALUE : newMaxPacketSize;
                maxSyncPacketSize = Math.max(newMaxPacketSize, maxSyncPacketSize);
            }
            AttachmentTypeImpl<A> attachment = new AttachmentTypeImpl<A>(id, this.defaultInitializer, this.persistenceCodec, this.streamCodec, this.syncPredicate, this.copyOnDeath, this.maxSyncSize);
            AttachmentRegistryImpl.register(id, attachment);
            return attachment;
        }
    }
}

