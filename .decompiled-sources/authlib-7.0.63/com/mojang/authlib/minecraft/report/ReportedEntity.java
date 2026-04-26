/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib.minecraft.report;

import com.google.gson.annotations.SerializedName;
import java.util.UUID;

public record ReportedEntity(@SerializedName(value="profileId") UUID profileId) {
}

