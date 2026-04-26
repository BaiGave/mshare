/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib.minecraft;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.BanDetails;
import com.mojang.authlib.minecraft.TelemetrySession;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.authlib.yggdrasil.request.AbuseReportRequest;
import com.mojang.authlib.yggdrasil.response.KeyPairResponse;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;

public interface UserApiService {
    public static final UserProperties OFFLINE_PROPERTIES = new UserProperties(Set.of(UserFlag.CHAT_ALLOWED, UserFlag.REALMS_ALLOWED, UserFlag.SERVERS_ALLOWED), Map.of());
    public static final UserApiService OFFLINE = new UserApiService(){

        @Override
        public UserProperties fetchProperties() {
            return OFFLINE_PROPERTIES;
        }

        @Override
        public boolean isBlockedPlayer(UUID playerID) {
            return false;
        }

        @Override
        public void refreshBlockList() {
        }

        @Override
        public TelemetrySession newTelemetrySession(Executor executor) {
            return TelemetrySession.DISABLED;
        }

        @Override
        @Nullable
        public KeyPairResponse getKeyPair() {
            return null;
        }

        @Override
        public void reportAbuse(AbuseReportRequest request) {
        }

        @Override
        public boolean canSendReports() {
            return false;
        }

        @Override
        public AbuseReportLimits getAbuseReportLimits() {
            return AbuseReportLimits.DEFAULTS;
        }
    };

    public UserProperties fetchProperties() throws AuthenticationException;

    public boolean isBlockedPlayer(UUID var1);

    public void refreshBlockList();

    public TelemetrySession newTelemetrySession(Executor var1);

    @Nullable
    public KeyPairResponse getKeyPair();

    public void reportAbuse(AbuseReportRequest var1);

    public boolean canSendReports();

    public AbuseReportLimits getAbuseReportLimits();

    public record UserProperties(Set<UserFlag> flags, Map<String, BanDetails> bannedScopes) {
        public boolean flag(UserFlag flag) {
            return this.flags.contains((Object)flag);
        }
    }

    public static enum UserFlag {
        SERVERS_ALLOWED,
        REALMS_ALLOWED,
        CHAT_ALLOWED,
        TELEMETRY_ENABLED,
        PROFANITY_FILTER_ENABLED,
        OPTIONAL_TELEMETRY_AVAILABLE;

    }
}

