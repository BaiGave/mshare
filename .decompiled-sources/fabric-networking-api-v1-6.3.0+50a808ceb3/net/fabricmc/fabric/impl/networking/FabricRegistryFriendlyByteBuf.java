/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking;

import java.util.Set;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public interface FabricRegistryFriendlyByteBuf {
    public void fabric_setSendableConfigurationChannels(Set<Identifier> var1);

    public @Nullable Set<Identifier> fabric_getSendableConfigurationChannels();
}

