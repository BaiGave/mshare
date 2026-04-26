/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.attachment;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public record AttachmentTypeImpl<A>(Identifier identifier, @Nullable Supplier<A> initializer, @Nullable Codec<A> persistenceCodec, @Nullable StreamCodec<? super RegistryFriendlyByteBuf, A> streamCodec, @Nullable AttachmentSyncPredicate syncPredicate, boolean copyOnDeath, int maxSyncSize) implements AttachmentType<A>
{
    @Override
    public boolean isSynced() {
        return this.syncPredicate != null;
    }
}

