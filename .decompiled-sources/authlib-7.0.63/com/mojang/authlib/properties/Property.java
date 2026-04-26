/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib.properties;

import com.google.gson.annotations.SerializedName;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import javax.annotation.Nullable;

public record Property(@SerializedName(value="name") String name, @SerializedName(value="value") String value, @SerializedName(value="signature") @Nullable String signature) {
    public Property(String name, String value) {
        this(name, value, null);
    }

    public boolean hasSignature() {
        return this.signature != null;
    }

    @Deprecated
    public boolean isSignatureValid(PublicKey publicKey) {
        try {
            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initVerify(publicKey);
            signature.update(this.value.getBytes(StandardCharsets.US_ASCII));
            return signature.verify(Base64.getDecoder().decode(this.signature));
        }
        catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            e.printStackTrace();
            return false;
        }
    }
}

