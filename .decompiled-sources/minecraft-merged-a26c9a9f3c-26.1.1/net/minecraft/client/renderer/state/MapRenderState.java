/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.state;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class MapRenderState
implements FabricRenderState {
    public @Nullable Identifier texture;
    public final List<MapDecorationRenderState> decorations = new ArrayList<MapDecorationRenderState>();

    @Environment(value=EnvType.CLIENT)
    public static class MapDecorationRenderState
    implements FabricRenderState {
        public @Nullable TextureAtlasSprite atlasSprite;
        public byte x;
        public byte y;
        public byte rot;
        public boolean renderOnFrame;
        public @Nullable Component name;
    }
}

