/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib.minecraft.report;

import com.google.gson.annotations.SerializedName;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReportChatMessage(@SerializedName(value="index") int index, @SerializedName(value="profileId") UUID profileId, @SerializedName(value="sessionId") UUID sessionId, @SerializedName(value="timestamp") Instant timestamp, @SerializedName(value="salt") long salt, @SerializedName(value="lastSeen") List<ByteBuffer> lastSeen, @SerializedName(value="message") String message, @SerializedName(value="signature") ByteBuffer signature, @SerializedName(value="messageReported") boolean messageReported) {
}

