/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib.yggdrasil.request;

import com.google.gson.annotations.SerializedName;
import com.mojang.authlib.minecraft.report.AbuseReport;
import java.util.UUID;
import javax.annotation.Nullable;

public record AbuseReportRequest(@SerializedName(value="version") int version, @SerializedName(value="id") UUID id, @SerializedName(value="report") AbuseReport report, @SerializedName(value="clientInfo") ClientInfo clientInfo, @SerializedName(value="thirdPartyServerInfo") @Nullable ThirdPartyServerInfo thirdPartyServerInfo, @SerializedName(value="realmInfo") @Nullable RealmInfo realmInfo, @SerializedName(value="reportType") String reportType) {

    public record ClientInfo(@SerializedName(value="clientVersion") String clientVersion, @SerializedName(value="locale") String locale) {
    }

    public record ThirdPartyServerInfo(@SerializedName(value="address") String address) {
    }

    public record RealmInfo(@SerializedName(value="realmId") String realmId, @SerializedName(value="slotId") int slotId) {
    }
}

