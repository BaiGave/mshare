/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.attachment.sync;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.GlobalAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.jspecify.annotations.Nullable;

public sealed interface AttachmentTargetInfo<T> {
    public static final int MAX_SIZE_IN_BYTES = 9;
    public static final StreamCodec<ByteBuf, AttachmentTargetInfo<?>> PACKET_CODEC = ByteBufCodecs.BYTE.dispatch(AttachmentTargetInfo::getId, Type::streamCodecFromId);

    public Type<T> getType();

    default public byte getId() {
        return this.getType().id;
    }

    public @Nullable AttachmentTarget getTarget(Level var1);

    public void appendDebugInformation(MutableComponent var1);

    public record Type<T>(byte id, StreamCodec<ByteBuf, ? extends AttachmentTargetInfo<T>> streamCodec) {
        static Byte2ObjectMap<Type<?>> TYPES = new Byte2ObjectArrayMap();
        static Type<BlockEntity> BLOCK_ENTITY = new Type(0, BlockEntityTarget.PACKET_CODEC);
        static Type<Entity> ENTITY = new Type(1, EntityTarget.PACKET_CODEC);
        static Type<ChunkAccess> CHUNK = new Type(2, ChunkTarget.PACKET_CODEC);
        static Type<Level> WORLD = new Type(3, LevelTarget.PACKET_CODEC);
        static Type<GlobalAttachments> GLOBAL = new Type(4, GlobalTarget.PACKET_CODEC);

        public Type(byte id, StreamCodec<ByteBuf, ? extends AttachmentTargetInfo<T>> streamCodec) {
            TYPES.put(id, (Type<?>)this);
            this.id = id;
            this.streamCodec = streamCodec;
        }

        static StreamCodec<ByteBuf, ? extends AttachmentTargetInfo<?>> streamCodecFromId(byte id) {
            return ((Type)Type.TYPES.get((byte)id)).streamCodec;
        }
    }

    public static final class GlobalTarget
    implements AttachmentTargetInfo<GlobalAttachments> {
        public static final GlobalTarget INSTANCE = new GlobalTarget();
        static final StreamCodec<ByteBuf, GlobalTarget> PACKET_CODEC = StreamCodec.unit(INSTANCE);

        private GlobalTarget() {
        }

        @Override
        public Type<GlobalAttachments> getType() {
            return Type.GLOBAL;
        }

        @Override
        public AttachmentTarget getTarget(Level level) {
            return level.globalAttachments();
        }

        @Override
        public void appendDebugInformation(MutableComponent component) {
            component.append(Component.translatable("fabric-data-attachment-api-v1.unknown-target.target-type", Component.translatable("fabric-data-attachment-api-v1.unknown-target.target-type.global").withStyle(ChatFormatting.YELLOW))).append(CommonComponents.NEW_LINE);
        }
    }

    public static final class LevelTarget
    implements AttachmentTargetInfo<Level> {
        public static final LevelTarget INSTANCE = new LevelTarget();
        static final StreamCodec<ByteBuf, LevelTarget> PACKET_CODEC = StreamCodec.unit(INSTANCE);

        private LevelTarget() {
        }

        @Override
        public Type<Level> getType() {
            return Type.WORLD;
        }

        @Override
        public AttachmentTarget getTarget(Level level) {
            return level;
        }

        @Override
        public void appendDebugInformation(MutableComponent component) {
            component.append(Component.translatable("fabric-data-attachment-api-v1.unknown-target.target-type", Component.translatable("fabric-data-attachment-api-v1.unknown-target.target-type.level").withStyle(ChatFormatting.YELLOW))).append(CommonComponents.NEW_LINE);
        }
    }

    public record ChunkTarget(ChunkPos pos) implements AttachmentTargetInfo<ChunkAccess>
    {
        static final StreamCodec<ByteBuf, ChunkTarget> PACKET_CODEC = ByteBufCodecs.VAR_LONG.map(ChunkPos::unpack, ChunkPos::pack).map(ChunkTarget::new, ChunkTarget::pos);

        @Override
        public Type<ChunkAccess> getType() {
            return Type.CHUNK;
        }

        @Override
        public AttachmentTarget getTarget(Level level) {
            return level.getChunk(this.pos.x(), this.pos.z(), ChunkStatus.FULL, false);
        }

        @Override
        public void appendDebugInformation(MutableComponent component) {
            component.append(Component.translatable("fabric-data-attachment-api-v1.unknown-target.target-type", Component.translatable("fabric-data-attachment-api-v1.unknown-target.target-type.chunk").withStyle(ChatFormatting.YELLOW))).append(CommonComponents.NEW_LINE);
            component.append(Component.translatable("fabric-data-attachment-api-v1.unknown-target.chunk-position", Component.literal(this.pos.x() + ", " + this.pos.z()).withStyle(ChatFormatting.YELLOW))).append(CommonComponents.NEW_LINE);
        }
    }

    public record EntityTarget(int networkId) implements AttachmentTargetInfo<Entity>
    {
        static final StreamCodec<ByteBuf, EntityTarget> PACKET_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, EntityTarget::networkId, EntityTarget::new);

        @Override
        public Type<Entity> getType() {
            return Type.ENTITY;
        }

        @Override
        public AttachmentTarget getTarget(Level level) {
            return level.getEntity(this.networkId);
        }

        @Override
        public void appendDebugInformation(MutableComponent component) {
            component.append(Component.translatable("fabric-data-attachment-api-v1.unknown-target.target-type", Component.translatable("fabric-data-attachment-api-v1.unknown-target.target-type.entity").withStyle(ChatFormatting.YELLOW))).append(CommonComponents.NEW_LINE);
            component.append(Component.translatable("fabric-data-attachment-api-v1.unknown-target.entity-network-id", Component.literal(String.valueOf(this.networkId)).withStyle(ChatFormatting.YELLOW))).append(CommonComponents.NEW_LINE);
        }
    }

    public record BlockEntityTarget(BlockPos pos) implements AttachmentTargetInfo<BlockEntity>
    {
        static final StreamCodec<ByteBuf, BlockEntityTarget> PACKET_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, BlockEntityTarget::pos, BlockEntityTarget::new);

        @Override
        public Type<BlockEntity> getType() {
            return Type.BLOCK_ENTITY;
        }

        @Override
        public AttachmentTarget getTarget(Level level) {
            return level.getBlockEntity(this.pos);
        }

        @Override
        public void appendDebugInformation(MutableComponent component) {
            component.append(Component.translatable("fabric-data-attachment-api-v1.unknown-target.target-type", Component.translatable("fabric-data-attachment-api-v1.unknown-target.target-type.block-entity").withStyle(ChatFormatting.YELLOW))).append(CommonComponents.NEW_LINE);
            component.append(Component.translatable("fabric-data-attachment-api-v1.unknown-target.block-entity-position", Component.literal(this.pos.toShortString()).withStyle(ChatFormatting.YELLOW))).append(CommonComponents.NEW_LINE);
        }
    }
}

