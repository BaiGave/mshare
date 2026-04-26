/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components.debug;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class DebugEntryFps
implements DebugScreenEntry {
    @Override
    public void display(DebugScreenDisplayer displayer, @Nullable Level serverOrClientLevel, @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {
        Minecraft minecraft = Minecraft.getInstance();
        int framerateLimit = minecraft.getFramerateLimitTracker().getFramerateLimit();
        Options options = minecraft.options;
        displayer.addPriorityLine(String.format(Locale.ROOT, "%d fps T: %s%s", minecraft.getFps(), framerateLimit == 260 ? "inf" : Integer.valueOf(framerateLimit), options.enableVsync().get() != false ? " vsync" : ""));
    }

    @Override
    public boolean isAllowed(boolean reducedDebugInfo) {
        return true;
    }
}

