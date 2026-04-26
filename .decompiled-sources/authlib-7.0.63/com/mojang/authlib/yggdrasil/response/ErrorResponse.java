/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib.yggdrasil.response;

import com.google.gson.annotations.SerializedName;
import java.util.Map;
import javax.annotation.Nullable;

public record ErrorResponse(@SerializedName(value="path") String path, @SerializedName(value="error") @Nullable String error, @SerializedName(value="errorMessage") @Nullable String errorMessage, @SerializedName(value="details") @Nullable Map<String, Object> details) {
}

