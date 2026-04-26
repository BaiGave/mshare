/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.worldselection;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldGenSettings;

@Environment(value=EnvType.CLIENT)
public record DataPackReloadCookie(WorldGenSettings worldGenSettings, WorldDataConfiguration dataConfiguration) {
}

