/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.attachment.v1;

import com.mojang.serialization.Codec;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.impl.attachment.AttachmentRegistryImpl;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

public final class AttachmentRegistry {
    private AttachmentRegistry() {
    }

    public static <A> AttachmentType<A> create(Identifier id, Consumer<Builder<A>> consumer) {
        Builder builder = AttachmentRegistryImpl.builder();
        consumer.accept(builder);
        return builder.buildAndRegister(id);
    }

    public static <A> AttachmentType<A> create(Identifier id) {
        return AttachmentRegistry.create(id, builder -> {});
    }

    public static <A> AttachmentType<A> createDefaulted(Identifier id, Supplier<A> initializer) {
        return AttachmentRegistry.create(id, builder -> builder.initializer(initializer));
    }

    public static <A> AttachmentType<A> createPersistent(Identifier id, Codec<A> codec) {
        return AttachmentRegistry.create(id, builder -> builder.persistent(codec));
    }

    @Deprecated
    public static <A> Builder<A> builder() {
        return AttachmentRegistryImpl.builder();
    }

    @ApiStatus.NonExtendable
    public static interface Builder<A> {
        public Builder<A> persistent(Codec<A> var1);

        public Builder<A> copyOnDeath();

        public Builder<A> initializer(Supplier<A> var1);

        public Builder<A> syncWith(StreamCodec<? super RegistryFriendlyByteBuf, A> var1, AttachmentSyncPredicate var2);

        public Builder<A> syncWith(StreamCodec<? super RegistryFriendlyByteBuf, A> var1, AttachmentSyncPredicate var2, int var3);

        public AttachmentType<A> buildAndRegister(Identifier var1);
    }
}

