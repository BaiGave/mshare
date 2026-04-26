/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.game.minecraft.launchwrapper;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.game.minecraft.launchwrapper.FabricTweaker;

public class FabricServerTweaker
extends FabricTweaker {
    @Override
    public EnvType getEnvironmentType() {
        return EnvType.SERVER;
    }

    public String getLaunchTarget() {
        return "net.minecraft.server.MinecraftServer";
    }
}

