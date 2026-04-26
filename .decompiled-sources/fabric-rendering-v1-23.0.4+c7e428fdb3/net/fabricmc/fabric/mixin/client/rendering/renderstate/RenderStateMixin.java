/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering.renderstate;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.state.OptionsRenderState;
import net.minecraft.client.renderer.state.WindowRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.PanoramaRenderState;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.CameraEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.ParticlesRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import net.minecraft.client.renderer.state.level.WorldBorderRenderState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={BlockModelRenderState.class, MovingBlockRenderState.class, BlockEntityRenderState.class, EntityRenderState.class, EntityRenderState.LeashState.class, FogData.class, ItemStackRenderState.class, ItemStackRenderState.LayerRenderState.class, GameRenderState.class, LightmapRenderState.class, MapRenderState.class, MapRenderState.MapDecorationRenderState.class, OptionsRenderState.class, WindowRenderState.class, GuiRenderState.class, PanoramaRenderState.class, BlockBreakingRenderState.class, BlockOutlineRenderState.class, CameraEntityRenderState.class, CameraRenderState.class, LevelRenderState.class, ParticlesRenderState.class, SkyRenderState.class, WeatherRenderState.class, WorldBorderRenderState.class})
abstract class RenderStateMixin
implements FabricRenderState {
    @Unique
    private @Nullable Map<RenderStateDataKey<?>, Object> renderStateData;

    RenderStateMixin() {
    }

    @Override
    public <T> @Nullable T getData(RenderStateDataKey<T> key) {
        return (T)(this.renderStateData == null ? null : this.renderStateData.get(key));
    }

    @Override
    public <T> T getDataOrDefault(RenderStateDataKey<T> key, T defaultValue) {
        return (T)(this.renderStateData == null ? defaultValue : this.renderStateData.getOrDefault(key, defaultValue));
    }

    @Override
    public <T> void setData(RenderStateDataKey<T> key, T value) {
        if (this.renderStateData == null) {
            this.renderStateData = new Reference2ObjectOpenHashMap();
        }
        this.renderStateData.put(key, value);
    }

    @Override
    public void clearExtraData() {
        if (this.renderStateData != null) {
            this.renderStateData.clear();
        }
    }
}

