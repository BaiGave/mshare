/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components.debug;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
class DebugEntryVersion
implements DebugScreenEntry {
    DebugEntryVersion() {
    }

    @Override
    public void display(DebugScreenDisplayer displayer, @Nullable Level level, @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {
        displayer.addPriorityLine("Minecraft " + SharedConstants.getCurrentVersion().name() + " (" + Minecraft.getInstance().getLaunchedVersion() + "/" + ClientBrandRetriever.getClientModName() + ")");
    }

    @Override
    public boolean isAllowed(boolean reducedDebugInfo) {
        return true;
    }
}

