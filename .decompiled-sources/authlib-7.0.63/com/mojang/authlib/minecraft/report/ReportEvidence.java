/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib.minecraft.report;

import com.google.gson.annotations.SerializedName;
import com.mojang.authlib.minecraft.report.ReportChatMessage;
import java.util.List;

public record ReportEvidence(@SerializedName(value="messages") List<ReportChatMessage> messages) {
}

