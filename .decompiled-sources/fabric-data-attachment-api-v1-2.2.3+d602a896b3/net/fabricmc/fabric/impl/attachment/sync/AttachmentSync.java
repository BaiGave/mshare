/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.attachment.sync;

import io.netty.buffer.ByteBufUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.impl.attachment.AttachmentEntrypoint;
import net.fabricmc.fabric.impl.attachment.AttachmentRegistryImpl;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentChange;
import net.fabricmc.fabric.impl.attachment.sync.clientbound.ClientboundAttachmentSyncPayload;
import net.fabricmc.fabric.impl.attachment.sync.clientbound.ClientboundRequestAcceptedAttachmentsPayload;
import net.fabricmc.fabric.impl.attachment.sync.serverbound.ServerboundAcceptedAttachmentsPayload;
import net.fabricmc.fabric.mixin.attachment.ClientboundCustomPayloadPacketAccessor;
import net.minecraft.network.VarInt;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ConfigurationTask;

public class AttachmentSync
implements ModInitializer {
    public static final int MAX_IDENTIFIER_SIZE = 256;
    public static final int MAX_PADDING_SIZE_IN_BYTES = 265;
    public static final int DEFAULT_MAX_DATA_SIZE;
    public static final int DEFAULT_ATTACHMENT_SYNC_PACKET_SIZE;
    private static final PacketContext.Key<Set<Identifier>> SUPPORTED_ATTACHMENTS_KEY;

    public static ServerboundAcceptedAttachmentsPayload createResponsePayload() {
        return new ServerboundAcceptedAttachmentsPayload(AttachmentRegistryImpl.getSyncableAttachments());
    }

    public static void trySync(AttachmentChange change, ServerPlayer player) {
        if (player.connection == null) {
            return;
        }
        Set supported = player.connection.getPacketContext().orElse(SUPPORTED_ATTACHMENTS_KEY, Set.of());
        if (supported.contains(change.type().identifier())) {
            ServerPlayNetworking.send(player, new ClientboundAttachmentSyncPayload(change));
        }
    }

    public static void trySync(List<AttachmentChange> changes, ServerPlayer player) {
        if (changes.size() == 1) {
            AttachmentSync.trySync(changes.getFirst(), player);
            return;
        }
        Set supported = player.connection.getPacketContext().orElse(SUPPORTED_ATTACHMENTS_KEY, Set.of());
        ArrayList<Packet<? super ClientGamePacketListener>> syncableChanges = new ArrayList<Packet<? super ClientGamePacketListener>>();
        changes.forEach(change -> {
            if (supported.contains(change.type().identifier())) {
                syncableChanges.add(ServerPlayNetworking.createClientboundPacket(new ClientboundAttachmentSyncPayload((AttachmentChange)change)));
            }
        });
        if (!syncableChanges.isEmpty()) {
            ServerPlayNetworking.getSender(player).sendPacket(new ClientboundBundlePacket((Iterable<Packet<? super ClientGamePacketListener>>)syncableChanges));
        }
    }

    private static Set<Identifier> decodeResponsePayload(ServerboundAcceptedAttachmentsPayload payload) {
        Set<Identifier> atts = payload.acceptedAttachments();
        Set<Identifier> syncable = AttachmentRegistryImpl.getSyncableAttachments();
        atts.retainAll(syncable);
        if (atts.size() < syncable.size()) {
            AttachmentEntrypoint.LOGGER.warn("Client does not support the syncable attachments {}", (Object)syncable.stream().filter(id -> !atts.contains(id)).map(Identifier::toString).collect(Collectors.joining(", ")));
        }
        return atts;
    }

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.serverboundConfiguration().register(ServerboundAcceptedAttachmentsPayload.ID, ServerboundAcceptedAttachmentsPayload.CODEC);
        PayloadTypeRegistry.clientboundConfiguration().register(ClientboundRequestAcceptedAttachmentsPayload.ID, ClientboundRequestAcceptedAttachmentsPayload.CODEC);
        ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
            if (ServerConfigurationNetworking.canSend(handler, ClientboundRequestAcceptedAttachmentsPayload.PACKET_ID)) {
                handler.addTask(new AttachmentSyncTask());
            } else {
                AttachmentEntrypoint.LOGGER.debug("Couldn't send attachment configuration packet to client, as the client cannot receive the payload.");
            }
        });
        ServerConfigurationNetworking.registerGlobalReceiver(ServerboundAcceptedAttachmentsPayload.ID, (payload, context) -> {
            Set<Identifier> supportedAttachments = AttachmentSync.decodeResponsePayload(payload);
            context.packetListener().getPacketContext().set(SUPPORTED_ATTACHMENTS_KEY, supportedAttachments);
            context.packetListener().completeTask(AttachmentSyncTask.KEY);
        });
        PayloadTypeRegistry.clientboundPlay().registerLarge(ClientboundAttachmentSyncPayload.TYPE, ClientboundAttachmentSyncPayload.CODEC, AttachmentRegistryImpl::getMaxSyncPacketSize);
        ServerPlayerEvents.JOIN.register(player -> {
            ArrayList<AttachmentChange> changes = new ArrayList<AttachmentChange>();
            ((AttachmentTargetImpl)((Object)player.level().globalAttachments())).fabric_computeInitialSyncChanges(player, changes::add);
            ((AttachmentTargetImpl)((Object)player.level())).fabric_computeInitialSyncChanges(player, changes::add);
            ((AttachmentTargetImpl)((Object)player)).fabric_computeInitialSyncChanges(player, changes::add);
            if (!changes.isEmpty()) {
                AttachmentSync.trySync(changes, player);
            }
        });
        ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) -> {
            ArrayList<AttachmentChange> changes = new ArrayList<AttachmentChange>();
            ((AttachmentTargetImpl)((Object)destination)).fabric_computeInitialSyncChanges(player, changes::add);
            if (!changes.isEmpty()) {
                AttachmentSync.trySync(changes, player);
            }
        });
        EntityTrackingEvents.START_TRACKING.register((trackedEntity, player) -> {
            ArrayList<AttachmentChange> changes = new ArrayList<AttachmentChange>();
            ((AttachmentTargetImpl)((Object)trackedEntity)).fabric_computeInitialSyncChanges(player, changes::add);
            if (!changes.isEmpty()) {
                AttachmentSync.trySync(changes, player);
            }
        });
    }

    static {
        SUPPORTED_ATTACHMENTS_KEY = PacketContext.key(Identifier.fromNamespaceAndPath("fabric", "supported_attachments"));
        int identifierSize = ByteBufUtil.utf8MaxBytes(ClientboundAttachmentSyncPayload.PACKET_ID.toString());
        int networkingApiPaddingSize = VarInt.getByteSize(identifierSize) + identifierSize + 10;
        DEFAULT_MAX_DATA_SIZE = ClientboundCustomPayloadPacketAccessor.getMaxPayloadSize() - 265 - networkingApiPaddingSize;
        DEFAULT_ATTACHMENT_SYNC_PACKET_SIZE = 265 + DEFAULT_MAX_DATA_SIZE;
    }

    private record AttachmentSyncTask() implements ConfigurationTask
    {
        public static final ConfigurationTask.Type KEY = new ConfigurationTask.Type(ClientboundRequestAcceptedAttachmentsPayload.PACKET_ID.toString());

        @Override
        public void start(Consumer<Packet<?>> sender) {
            sender.accept(ServerConfigurationNetworking.createClientboundPacket(ClientboundRequestAcceptedAttachmentsPayload.INSTANCE));
        }

        @Override
        public ConfigurationTask.Type type() {
            return KEY;
        }
    }
}

