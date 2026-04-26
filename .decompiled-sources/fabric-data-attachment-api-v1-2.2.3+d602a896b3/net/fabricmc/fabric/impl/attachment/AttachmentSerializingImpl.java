/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.impl.attachment.AttachmentRegistryImpl;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttachmentSerializingImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger("fabric-data-attachment-api-v1");
    private static final Codec<AttachmentType<?>> TYPE_CODEC = Identifier.CODEC.comapFlatMap(id -> {
        AttachmentType<?> type = AttachmentRegistryImpl.get(id);
        return type == null ? DataResult.error(() -> "Found unknown attachment type " + String.valueOf(id)) : (type.persistenceCodec() == null ? DataResult.error(() -> "Found non-permanent attachment type " + String.valueOf(id)) : DataResult.success(type));
    }, AttachmentType::identifier);
    private static final Codec<IdentityHashMap<AttachmentType<?>, Object>> CODEC = Codec.dispatchedMap(TYPE_CODEC, AttachmentType::persistenceCodec).promotePartial(error -> LOGGER.warn("Skipping invalid attachments: {}", error)).xmap(IdentityHashMap::new, Function.identity());

    public static void serializeAttachmentData(ValueOutput output, @Nullable IdentityHashMap<AttachmentType<?>, Object> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return;
        }
        IdentityHashMap attachmentsToSerialize = attachments.entrySet().stream().filter(entry -> ((AttachmentType)entry.getKey()).persistenceCodec() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, IdentityHashMap::new));
        if (attachmentsToSerialize.isEmpty()) {
            return;
        }
        output.store("fabric:attachments", CODEC, attachmentsToSerialize);
    }

    public static @Nullable IdentityHashMap<AttachmentType<?>, Object> deserializeAttachmentData(@Nullable ValueInput data) {
        return data == null ? null : (IdentityHashMap)data.read("fabric:attachments", CODEC).filter(m -> !m.isEmpty()).orElse(null);
    }

    public static boolean hasPersistentAttachments(@Nullable IdentityHashMap<AttachmentType<?>, ?> map) {
        if (map == null) {
            return false;
        }
        for (AttachmentType<?> type : map.keySet()) {
            if (!type.isPersistent()) continue;
            return true;
        }
        return false;
    }
}

