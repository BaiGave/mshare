/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib.yggdrasil.response;

import com.google.gson.annotations.SerializedName;
import java.nio.ByteBuffer;

public record KeyPairResponse(@SerializedName(value="keyPair") KeyPair keyPair, @SerializedName(value="publicKeySignatureV2") ByteBuffer publicKeySignature, @SerializedName(value="expiresAt") String expiresAt, @SerializedName(value="refreshedAfter") String refreshedAfter) {

    public record KeyPair(@SerializedName(value="privateKey") String privateKey, @SerializedName(value="publicKey") String publicKey) {
    }
}

