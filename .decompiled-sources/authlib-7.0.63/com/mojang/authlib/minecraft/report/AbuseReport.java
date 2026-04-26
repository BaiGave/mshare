/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib.minecraft.report;

import com.google.gson.annotations.SerializedName;
import com.mojang.authlib.minecraft.report.ReportEvidence;
import com.mojang.authlib.minecraft.report.ReportedEntity;
import java.time.Instant;
import javax.annotation.Nullable;

public record AbuseReport(@SerializedName(value="opinionComments") String opinionComments, @SerializedName(value="reason") @Nullable String reason, @SerializedName(value="evidence") @Nullable ReportEvidence evidence, @SerializedName(value="skinUrl") @Nullable String skinUrl, @SerializedName(value="reportedEntity") ReportedEntity reportedEntity, @SerializedName(value="createdTime") Instant createdTime) {
    public static AbuseReport name(String opinionComments, ReportedEntity reportedEntity, Instant createdTime) {
        return new AbuseReport(opinionComments, null, null, null, reportedEntity, createdTime);
    }

    public static AbuseReport skin(String opinionComments, String reason, @Nullable String skinUrl, ReportedEntity reportedEntity, Instant createdTime) {
        return new AbuseReport(opinionComments, reason, null, skinUrl, reportedEntity, createdTime);
    }

    public static AbuseReport chat(String opinionComments, String reason, ReportEvidence evidence, ReportedEntity reportedEntity, Instant createdTime) {
        return new AbuseReport(opinionComments, reason, evidence, null, reportedEntity, createdTime);
    }
}

