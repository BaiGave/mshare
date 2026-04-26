/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.security.SignatureException;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SignatureUpdater;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public record SignedMessageLink(int index, UUID sender, UUID sessionId) {
    public static final Codec<SignedMessageLink> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("index")).forGetter(SignedMessageLink::index), ((MapCodec)UUIDUtil.CODEC.fieldOf("sender")).forGetter(SignedMessageLink::sender), ((MapCodec)UUIDUtil.CODEC.fieldOf("session_id")).forGetter(SignedMessageLink::sessionId)).apply((Applicative<SignedMessageLink, ?>)i, SignedMessageLink::new));

    public static SignedMessageLink unsigned(UUID sender) {
        return SignedMessageLink.root(sender, Util.NIL_UUID);
    }

    public static SignedMessageLink root(UUID sender, UUID sessionId) {
        return new SignedMessageLink(0, sender, sessionId);
    }

    public void updateSignature(SignatureUpdater.Output output) throws SignatureException {
        output.update(UUIDUtil.uuidToByteArray(this.sender));
        output.update(UUIDUtil.uuidToByteArray(this.sessionId));
        output.update(Ints.toByteArray(this.index));
    }

    public boolean isDescendantOf(SignedMessageLink link) {
        return this.index > link.index() && this.sender.equals(link.sender()) && this.sessionId.equals(link.sessionId());
    }

    public @Nullable SignedMessageLink advance() {
        if (this.index == Integer.MAX_VALUE) {
            return null;
        }
        return new SignedMessageLink(this.index + 1, this.sender, this.sessionId);
    }
}

