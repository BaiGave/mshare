/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib.yggdrasil.response;

import com.google.gson.annotations.SerializedName;
import java.util.Set;
import java.util.UUID;

public record BlockListResponse(@SerializedName(value="blockedProfiles") Set<UUID> blockedProfiles) {
}

