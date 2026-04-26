/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking;

import java.util.Objects;
import java.util.Set;
import net.fabricmc.fabric.impl.networking.FabricRegistryFriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={RegistryFriendlyByteBuf.class})
public class RegistryFriendlyByteBufMixin
implements FabricRegistryFriendlyByteBuf {
    @Unique
    private Set<Identifier> sendableConfigurationChannels = null;

    @Override
    public void fabric_setSendableConfigurationChannels(Set<Identifier> globalChannels) {
        this.sendableConfigurationChannels = Objects.requireNonNull(globalChannels);
    }

    @Override
    public @Nullable Set<Identifier> fabric_getSendableConfigurationChannels() {
        return this.sendableConfigurationChannels;
    }
}

