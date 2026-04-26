/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.attachment.v1;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

@ApiStatus.NonExtendable
public interface AttachmentTarget {
    public static final String NBT_ATTACHMENT_KEY = "fabric:attachments";

    default public <A> @Nullable A getAttached(AttachmentType<A> type) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default public <A> A getAttachedOrThrow(AttachmentType<A> type) {
        return Objects.requireNonNull(this.getAttached(type), "No value was attached");
    }

    default public <A> A getAttachedOrSet(AttachmentType<A> type, A defaultValue) {
        Objects.requireNonNull(defaultValue, "default value cannot be null");
        A attached = this.getAttached(type);
        if (attached != null) {
            return attached;
        }
        this.setAttached(type, defaultValue);
        return defaultValue;
    }

    default public <A> A getAttachedOrCreate(AttachmentType<A> type, Supplier<A> initializer) {
        A attached = this.getAttached(type);
        if (attached != null) {
            return attached;
        }
        A initialized = Objects.requireNonNull(initializer.get(), "initializer result cannot be null");
        this.setAttached(type, initialized);
        return initialized;
    }

    default public <A> A getAttachedOrCreate(AttachmentType<A> type) {
        Supplier<A> init = type.initializer();
        if (init == null) {
            throw new IllegalArgumentException("Single-argument getAttachedOrCreate is reserved for attachment types with default initializers");
        }
        return this.getAttachedOrCreate(type, init);
    }

    @Contract(value="_, !null -> !null")
    default public <A> A getAttachedOrElse(AttachmentType<A> type, @Nullable A defaultValue) {
        A attached = this.getAttached(type);
        return attached == null ? defaultValue : attached;
    }

    default public <A> A getAttachedOrGet(AttachmentType<A> type, Supplier<A> defaultValue) {
        Objects.requireNonNull(defaultValue, "default value supplier cannot be null");
        A attached = this.getAttached(type);
        return attached == null ? defaultValue.get() : attached;
    }

    default public <A> @Nullable A setAttached(AttachmentType<A> type, @Nullable A value) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default public boolean hasAttached(AttachmentType<?> type) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default public <A> @Nullable A removeAttached(AttachmentType<A> type) {
        return this.setAttached(type, null);
    }

    default public <A> Event<OnAttachedSet<A>> onAttachedSet(AttachmentType<A> type) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default public <A> @Nullable A modifyAttached(AttachmentType<A> type, UnaryOperator<A> modifier) {
        return this.setAttached(type, modifier.apply(this.getAttached(type)));
    }

    @FunctionalInterface
    public static interface OnAttachedSet<A> {
        public void onAttachedSet(@Nullable A var1, @Nullable A var2);
    }
}

