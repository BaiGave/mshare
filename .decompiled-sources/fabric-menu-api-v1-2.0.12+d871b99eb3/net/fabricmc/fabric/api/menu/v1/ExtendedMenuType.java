/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.menu.v1;

import java.util.Objects;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class ExtendedMenuType<T extends AbstractContainerMenu, D>
extends MenuType<T> {
    private final ExtendedFactory<T, D> factory;
    private final StreamCodec<? super RegistryFriendlyByteBuf, D> streamCodec;

    public ExtendedMenuType(ExtendedFactory<T, D> factory, StreamCodec<? super RegistryFriendlyByteBuf, D> streamCodec) {
        super(null, FeatureFlags.VANILLA_SET);
        this.factory = Objects.requireNonNull(factory, "menu factory cannot be null");
        this.streamCodec = Objects.requireNonNull(streamCodec, "stream codec cannot be null");
    }

    @Override
    @Deprecated
    public final T create(int containerId, Inventory inventory) {
        throw new UnsupportedOperationException("Use ExtendedMenuType.create(int, Inventory, FriendlyByteBuf)!");
    }

    public T create(int containerId, Inventory inventory, D data) {
        return this.factory.create(containerId, inventory, data);
    }

    public StreamCodec<? super RegistryFriendlyByteBuf, D> getStreamCodec() {
        return this.streamCodec;
    }

    @FunctionalInterface
    public static interface ExtendedFactory<T extends AbstractContainerMenu, D> {
        public T create(int var1, Inventory var2, D var3);
    }
}

