/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.resource;

import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.minecraft.resources.Identifier;

@Deprecated
public final class ResourceReloadListenerKeys {
    public static final Identifier SOUNDS = ResourceReloaderKeys.Client.SOUNDS;
    public static final Identifier FONTS = ResourceReloaderKeys.Client.FONTS;
    public static final Identifier MODELS = ResourceReloaderKeys.Client.MODELS;
    public static final Identifier LANGUAGES = ResourceReloaderKeys.Client.LANGUAGES;
    public static final Identifier TEXTURES = ResourceReloaderKeys.Client.TEXTURES;
    public static final Identifier RECIPES = ResourceReloaderKeys.Server.RECIPES;
    public static final Identifier ADVANCEMENTS = ResourceReloaderKeys.Server.ADVANCEMENTS;
    public static final Identifier FUNCTIONS = ResourceReloaderKeys.Server.FUNCTIONS;

    private ResourceReloadListenerKeys() {
    }
}

