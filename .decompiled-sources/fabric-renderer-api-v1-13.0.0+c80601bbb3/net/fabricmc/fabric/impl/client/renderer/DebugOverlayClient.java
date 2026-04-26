/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.renderer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.minecraft.client.gui.components.debug.DebugEntryCategory;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

public class DebugOverlayClient
implements ClientModInitializer {
    public static Identifier ACTIVE_RENDERER = DebugScreenEntries.register(Identifier.fromNamespaceAndPath("fabric", "active_renderer"), (DebugScreenEntry)new ActiveRendererDebugOverlayEntry());

    @Override
    public void onInitializeClient() {
    }

    private static class ActiveRendererDebugOverlayEntry
    implements DebugScreenEntry {
        private ActiveRendererDebugOverlayEntry() {
        }

        @Override
        public void display(DebugScreenDisplayer lines, @Nullable Level level, @Nullable LevelChunk clientChunk, @Nullable LevelChunk chunk) {
            lines.addLine("[Fabric] Active renderer: " + Renderer.get().getClass().getSimpleName());
        }

        @Override
        public boolean isAllowed(boolean reducedDebugInfo) {
            return true;
        }

        @Override
        public DebugEntryCategory category() {
            return DebugEntryCategory.SCREEN_TEXT;
        }
    }
}

