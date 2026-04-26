/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.attachment.v1;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.NonExtendable
public interface AttachmentType<A> {
    public Identifier identifier();

    public @Nullable Codec<A> persistenceCodec();

    default public boolean isPersistent() {
        return this.persistenceCodec() != null;
    }

    public @Nullable Supplier<A> initializer();

    public boolean isSynced();

    public boolean copyOnDeath();
}

