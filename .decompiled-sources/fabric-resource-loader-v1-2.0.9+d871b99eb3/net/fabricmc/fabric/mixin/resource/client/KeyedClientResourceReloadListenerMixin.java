/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.resource.client;

import java.util.Locale;
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.fabricmc.fabric.impl.resource.FabricResourceReloader;
import net.minecraft.client.PeriodicNotificationManager;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DryFoliageColorReloadListener;
import net.minecraft.client.resources.FoliageColorReloadListener;
import net.minecraft.client.resources.GrassColorReloadListener;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.client.resources.WaypointStyleManager;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={AtlasManager.class, ModelManager.class, BlockEntityRenderDispatcher.class, CloudRenderer.class, EquipmentAssetManager.class, EntityRenderDispatcher.class, DryFoliageColorReloadListener.class, FoliageColorReloadListener.class, FontManager.class, GrassColorReloadListener.class, LanguageManager.class, ParticleResources.class, ShaderManager.class, SplashManager.class, SoundManager.class, TextureManager.class, WaypointStyleManager.class, LevelRenderer.class, GpuWarnlistManager.class, PeriodicNotificationManager.class})
public abstract class KeyedClientResourceReloadListenerMixin
implements FabricResourceReloader {
    @Unique
    private Identifier fabric$id;

    @Override
    public Identifier fabric$getId() {
        if (this.fabric$id == null) {
            KeyedClientResourceReloadListenerMixin self = this;
            this.fabric$id = self instanceof AtlasManager ? ResourceReloaderKeys.Client.ATLAS : (self instanceof ModelManager ? ResourceReloaderKeys.Client.MODELS : (self instanceof BlockEntityRenderDispatcher ? ResourceReloaderKeys.Client.BLOCK_ENTITY_RENDER_DISPATCHER : (self instanceof CloudRenderer ? ResourceReloaderKeys.Client.CLOUD_RENDERER : (self instanceof DryFoliageColorReloadListener ? ResourceReloaderKeys.Client.DRY_FOLIAGE_COLOR : (self instanceof EquipmentAssetManager ? ResourceReloaderKeys.Client.EQUIPMENT_ASSETS : (self instanceof EntityRenderDispatcher ? ResourceReloaderKeys.Client.ENTITY_RENDER_DISPATCHER : (self instanceof FontManager ? ResourceReloaderKeys.Client.FONTS : (self instanceof FoliageColorReloadListener ? ResourceReloaderKeys.Client.FOLIAGE_COLOR : (self instanceof GrassColorReloadListener ? ResourceReloaderKeys.Client.GRASS_COLOR : (self instanceof LanguageManager ? ResourceReloaderKeys.Client.LANGUAGES : (self instanceof ParticleResources ? ResourceReloaderKeys.Client.PARTICLES : (self instanceof ShaderManager ? ResourceReloaderKeys.Client.SHADERS : (self instanceof SplashManager ? ResourceReloaderKeys.Client.SPLASH_TEXTS : (self instanceof SoundManager ? ResourceReloaderKeys.Client.SOUNDS : (self instanceof TextureManager ? ResourceReloaderKeys.Client.TEXTURES : (self instanceof WaypointStyleManager ? ResourceReloaderKeys.Client.WAYPOINT_STYLE : Identifier.withDefaultNamespace("private/" + self.getClass().getSimpleName().toLowerCase(Locale.ROOT))))))))))))))))));
        }
        return this.fabric$id;
    }
}

