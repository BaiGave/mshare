/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking;

import java.util.Collection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.resources.Identifier;

public interface ChannelInfoHolder {
    public Collection<Identifier> fabric_getPendingChannelsNames(ConnectionProtocol var1);
}

