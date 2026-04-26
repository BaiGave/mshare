/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.attachment;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={SerializableChunkData.class})
abstract class SerializableChunkDataMixin {
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("SerializableChunkDataMixin");
    @Unique
    private @Nullable CompoundTag attachmentNbtData;

    SerializableChunkDataMixin() {
    }

    @Inject(method={"parse"}, at={@At(value="RETURN")})
    private static void storeAttachmentNbtData(LevelHeightAccessor heightLimitView, PalettedContainerFactory arg, CompoundTag chunkData, CallbackInfoReturnable<SerializableChunkData> cir, @Share(value="attachmentDataNbt") LocalRef<CompoundTag> attachmentDataNbt) {
        SerializableChunkData serializer = cir.getReturnValue();
        if (serializer == null) {
            return;
        }
        CompoundTag attachmentNbtData = chunkData.getCompound("fabric:attachments").orElse(null);
        if (attachmentNbtData != null) {
            ((SerializableChunkDataMixin)((Object)serializer)).attachmentNbtData = attachmentNbtData;
        }
    }

    @Inject(method={"read"}, at={@At(value="RETURN")})
    private void setAttachmentDataInChunk(ServerLevel serverLevel, PoiManager pointOfInterestStorage, RegionStorageInfo storageKey, ChunkPos chunkPos, CallbackInfoReturnable<ProtoChunk> cir) {
        ProtoChunk chunk = cir.getReturnValue();
        if (chunk != null && this.attachmentNbtData != null) {
            CompoundTag attachmentNbtData = new CompoundTag();
            attachmentNbtData.put("fabric:attachments", this.attachmentNbtData);
            try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(LOGGER);){
                ValueInput input = TagValueInput.create((ProblemReporter)reporter, (HolderLookup.Provider)serverLevel.registryAccess(), attachmentNbtData);
                ((AttachmentTargetImpl)((Object)chunk)).fabric_readAttachmentsFromNbt(input);
            }
        }
    }

    @Inject(method={"copyOf"}, at={@At(value="RETURN")})
    private static void storeAttachmentNbtData(ServerLevel level, ChunkAccess chunk, CallbackInfoReturnable<SerializableChunkData> cir) {
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(LOGGER);){
            TagValueOutput output = TagValueOutput.createWithContext(reporter, level.registryAccess());
            ((AttachmentTargetImpl)((Object)chunk)).fabric_writeAttachmentsToNbt(output);
            CompoundTag attachmentNbtData = output.buildResult().getCompound("fabric:attachments").orElse(null);
            if (attachmentNbtData != null) {
                ((SerializableChunkDataMixin)((Object)cir.getReturnValue())).attachmentNbtData = attachmentNbtData;
            }
        }
    }

    @Inject(method={"write"}, at={@At(value="RETURN")})
    private void writeChunkAttachments(CallbackInfoReturnable<CompoundTag> cir) {
        if (this.attachmentNbtData != null) {
            cir.getReturnValue().put("fabric:attachments", this.attachmentNbtData);
        }
    }
}

