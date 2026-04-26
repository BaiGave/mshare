/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.realmsclient.dto;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.ValueObject;
import com.mojang.realmsclient.util.JsonUtils;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class Backup
extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    public final String backupId;
    public final Instant lastModified;
    public final long size;
    public boolean uploadedVersion;
    public final Map<String, String> metadata;
    public final Map<String, String> changeList = new HashMap<String, String>();

    private Backup(String backupId, Instant lastModified, long size, Map<String, String> metadata) {
        this.backupId = backupId;
        this.lastModified = lastModified;
        this.size = size;
        this.metadata = metadata;
    }

    public ZonedDateTime lastModifiedDate() {
        return ZonedDateTime.ofInstant(this.lastModified, ZoneId.systemDefault());
    }

    public static @Nullable Backup parse(JsonElement node) {
        JsonObject object = node.getAsJsonObject();
        try {
            String backupId = JsonUtils.getStringOr("backupId", object, "");
            Instant lastModifiedDate = JsonUtils.getDateOr("lastModifiedDate", object);
            long size = JsonUtils.getLongOr("size", object, 0L);
            HashMap<String, String> metadata = new HashMap<String, String>();
            if (object.has("metadata")) {
                JsonObject metadataObject = object.getAsJsonObject("metadata");
                Set<Map.Entry<String, JsonElement>> jsonElementSet = metadataObject.entrySet();
                for (Map.Entry<String, JsonElement> elem : jsonElementSet) {
                    if (elem.getValue().isJsonNull()) continue;
                    metadata.put(elem.getKey(), elem.getValue().getAsString());
                }
            }
            return new Backup(backupId, lastModifiedDate, size, metadata);
        }
        catch (Exception e) {
            LOGGER.error("Could not parse Backup", e);
            return null;
        }
    }
}

