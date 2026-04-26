/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib.yggdrasil;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.annotations.SerializedName;
import com.mojang.authlib.exceptions.MinecraftClientException;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.ServicesKeyInfo;
import com.mojang.authlib.yggdrasil.ServicesKeySet;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YggdrasilServicesKeyInfo
implements ServicesKeyInfo {
    private static final Logger LOGGER = LoggerFactory.getLogger(YggdrasilServicesKeyInfo.class);
    private static final ScheduledExecutorService FETCHER_EXECUTOR = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setNameFormat("Yggdrasil Key Fetcher").setDaemon(true).build());
    private static final int KEY_SIZE_BITS = 4096;
    private static final String KEY_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";
    private static final int REFRESH_INTERVAL_HOURS = 24;
    private static final int BASE_FAILURE_INTERVAL_MINUTES = 5;
    private static final int MAX_BACKOFF_EXPONENT = 6;
    private final PublicKey publicKey;

    private YggdrasilServicesKeyInfo(PublicKey publicKey) {
        this.publicKey = publicKey;
        String algorithm = publicKey.getAlgorithm();
        if (!algorithm.equals(KEY_ALGORITHM)) {
            throw new IllegalArgumentException("Expected RSA key, got " + algorithm);
        }
    }

    public static ServicesKeyInfo parse(byte[] keyBytes) {
        try {
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PublicKey publicKey = keyFactory.generatePublic(spec);
            return new YggdrasilServicesKeyInfo(publicKey);
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalArgumentException("Invalid yggdrasil public key!", e);
        }
    }

    private static List<ServicesKeyInfo> parseList(@Nullable List<KeyData> keys) {
        if (keys == null) {
            return List.of();
        }
        return keys.stream().map(data -> YggdrasilServicesKeyInfo.parse(data.publicKey.array())).toList();
    }

    public static ServicesKeySet get(final URL url, final MinecraftClient client) {
        final CompletableFuture ready = new CompletableFuture();
        final AtomicReference keySet = new AtomicReference();
        FETCHER_EXECUTOR.execute(new Runnable(){
            private final AtomicInteger failureCount = new AtomicInteger();

            @Override
            public void run() {
                YggdrasilServicesKeyInfo.fetch(url, client).ifPresent(keySet::set);
                ready.complete(null);
                this.reschedule();
            }

            private void reschedule() {
                if (keySet.get() == null) {
                    int backoffExponent = Math.min(this.failureCount.getAndIncrement(), 6);
                    int delayMinutes = 5 * (1 << backoffExponent);
                    FETCHER_EXECUTOR.schedule(this, (long)delayMinutes, TimeUnit.MINUTES);
                    return;
                }
                FETCHER_EXECUTOR.schedule(this, 24L, TimeUnit.HOURS);
            }
        });
        return ServicesKeySet.lazy(() -> {
            ready.join();
            return Objects.requireNonNullElse((ServicesKeySet)keySet.get(), ServicesKeySet.EMPTY);
        });
    }

    private static Optional<ServicesKeySet> fetch(URL url, MinecraftClient client) {
        KeySetResponse response;
        try {
            response = client.get(url, KeySetResponse.class);
        }
        catch (MinecraftClientException e) {
            LOGGER.error("Failed to request yggdrasil public key", e);
            return Optional.empty();
        }
        if (response == null) {
            return Optional.empty();
        }
        try {
            List<ServicesKeyInfo> profilePropertyKeys = YggdrasilServicesKeyInfo.parseList(response.profilePropertyKeys);
            List<ServicesKeyInfo> playerCertificateKeys = YggdrasilServicesKeyInfo.parseList(response.playerCertificateKeys);
            return Optional.of(type -> switch (type) {
                default -> throw new IncompatibleClassChangeError();
                case ServicesKeyType.PROFILE_PROPERTY -> profilePropertyKeys;
                case ServicesKeyType.PROFILE_KEY -> playerCertificateKeys;
            });
        }
        catch (Exception e) {
            LOGGER.error("Received malformed yggdrasil public key data", e);
            return Optional.empty();
        }
    }

    @Override
    public Signature signature() {
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(this.publicKey);
            return signature;
        }
        catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new AssertionError("Failed to create signature", e);
        }
    }

    @Override
    public int keyBitCount() {
        return 4096;
    }

    @Override
    public boolean validateProperty(Property property) {
        byte[] expected;
        Signature signature = this.signature();
        try {
            expected = Base64.getDecoder().decode(property.signature());
        }
        catch (IllegalArgumentException e) {
            LOGGER.error("Malformed signature encoding on property {}", (Object)property, (Object)e);
            return false;
        }
        try {
            signature.update(property.value().getBytes());
            return signature.verify(expected);
        }
        catch (SignatureException e) {
            LOGGER.error("Failed to verify signature on property {}", (Object)property, (Object)e);
            return false;
        }
    }

    private record KeySetResponse(@SerializedName(value="profilePropertyKeys") @Nullable List<KeyData> profilePropertyKeys, @SerializedName(value="playerCertificateKeys") @Nullable List<KeyData> playerCertificateKeys) {
    }

    private record KeyData(@SerializedName(value="publicKey") ByteBuffer publicKey) {
    }
}

