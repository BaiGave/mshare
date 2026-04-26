/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.menu.v1;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;

public interface ExtendedMenuProvider<D>
extends MenuProvider {
    public D getScreenOpeningData(ServerPlayer var1);
}

