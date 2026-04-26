/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.attachment;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttachmentSavedData
extends SavedData {
    private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentSavedData.class);
    public static final Identifier ID = Identifier.fromNamespaceAndPath("fabric", "attachments");
    private final AttachmentTargetImpl target;
    private final boolean wasSerialized;

    public AttachmentSavedData(AttachmentTarget target) {
        this.target = (AttachmentTargetImpl)target;
        this.wasSerialized = this.target.fabric_hasPersistentAttachments();
    }

    public static Codec<AttachmentSavedData> codec(MinecraftServer server) {
        return AttachmentSavedData.codec((AttachmentTargetImpl)((Object)server.globalAttachments()), () -> "AttachmentSavedData @ global server attachments");
    }

    public static Codec<AttachmentSavedData> codec(ServerLevel level) {
        return AttachmentSavedData.codec((AttachmentTargetImpl)((Object)level), () -> "AttachmentSavedData @ " + String.valueOf(level.dimension().identifier()));
    }

    private static Codec<AttachmentSavedData> codec(final AttachmentTargetImpl target, final ProblemReporter.PathElement reporterContext) {
        return Codec.of(new Encoder<AttachmentSavedData>(){

            @Override
            public <T> DataResult<T> encode(AttachmentSavedData input, DynamicOps<T> ops, T prefix) {
                try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(reporterContext, LOGGER);){
                    TagValueOutput output = TagValueOutput.createWithoutContext(reporter);
                    target.fabric_writeAttachmentsToNbt(output);
                    DataResult<T> dataResult = DataResult.success(NbtOps.INSTANCE.convertTo(ops, output.buildResult()));
                    return dataResult;
                }
            }
        }, new Decoder<AttachmentSavedData>(){

            @Override
            public <T> DataResult<Pair<AttachmentSavedData, T>> decode(DynamicOps<T> ops, T input) {
                try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(reporterContext, LOGGER);){
                    ValueInput valueInput = TagValueInput.create((ProblemReporter)reporter, (HolderLookup.Provider)target.fabric_getRegistryAccess(), (CompoundTag)ops.convertTo(NbtOps.INSTANCE, input));
                    target.fabric_readAttachmentsFromNbt(valueInput);
                    DataResult<Pair<AttachmentSavedData, T>> dataResult = DataResult.success(Pair.of(new AttachmentSavedData(target), ops.empty()));
                    return dataResult;
                }
            }
        });
    }

    @Override
    public boolean isDirty() {
        return this.wasSerialized || this.target.fabric_hasPersistentAttachments();
    }
}

