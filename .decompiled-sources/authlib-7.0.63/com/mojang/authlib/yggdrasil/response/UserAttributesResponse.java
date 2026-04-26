/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib.yggdrasil.response;

import com.google.gson.annotations.SerializedName;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

public record UserAttributesResponse(@SerializedName(value="privileges") @Nullable Privileges privileges, @SerializedName(value="profanityFilterPreferences") @Nullable ProfanityFilterPreferences profanityFilterPreferences, @SerializedName(value="banStatus") @Nullable BanStatus banStatus) {

    public record Privileges(@SerializedName(value="onlineChat") @Nullable Privilege onlineChat, @SerializedName(value="multiplayerServer") @Nullable Privilege multiplayerServer, @SerializedName(value="multiplayerRealms") @Nullable Privilege multiplayerRealms, @SerializedName(value="telemetry") @Nullable Privilege telemetry, @SerializedName(value="optionalTelemetry") @Nullable Privilege optionalTelemetry) {
        public boolean getOnlineChat() {
            return this.onlineChat != null && this.onlineChat.enabled;
        }

        public boolean getMultiplayerServer() {
            return this.multiplayerServer != null && this.multiplayerServer.enabled;
        }

        public boolean getMultiplayerRealms() {
            return this.multiplayerRealms != null && this.multiplayerRealms.enabled;
        }

        public boolean getTelemetry() {
            return this.telemetry != null && this.telemetry.enabled;
        }

        public boolean getOptionalTelemetry() {
            return this.optionalTelemetry != null && this.optionalTelemetry.enabled;
        }

        public record Privilege(@SerializedName(value="enabled") boolean enabled) {
        }
    }

    public record ProfanityFilterPreferences(@SerializedName(value="profanityFilterOn") boolean enabled) {
    }

    public record BanStatus(@SerializedName(value="bannedScopes") Map<String, BannedScope> bannedScopes) {

        public record BannedScope(@SerializedName(value="banId") UUID banId, @SerializedName(value="expires") @Nullable Instant expires, @SerializedName(value="reason") String reason, @SerializedName(value="reasonMessage") @Nullable String reasonMessage) {
        }
    }
}

