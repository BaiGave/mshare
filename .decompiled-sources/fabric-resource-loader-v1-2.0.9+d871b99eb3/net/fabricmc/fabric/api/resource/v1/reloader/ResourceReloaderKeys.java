/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.resource.v1.reloader;

import net.minecraft.resources.Identifier;

public final class ResourceReloaderKeys {
    public static final Identifier BEFORE_VANILLA = Identifier.fromNamespaceAndPath("fabric", "before_vanilla");
    public static final Identifier AFTER_VANILLA = Identifier.fromNamespaceAndPath("fabric", "after_vanilla");

    private ResourceReloaderKeys() {
    }

    public static final class Server {
        public static final Identifier ADVANCEMENTS = Identifier.withDefaultNamespace("advancements");
        public static final Identifier FUNCTIONS = Identifier.withDefaultNamespace("functions");
        public static final Identifier RECIPES = Identifier.withDefaultNamespace("recipes");

        private Server() {
        }
    }

    public static final class Client {
        public static final Identifier BLOCK_ENTITY_RENDER_DISPATCHER = Identifier.withDefaultNamespace("block_entity_render_dispatcher");
        public static final Identifier CLOUD_RENDERER = Identifier.withDefaultNamespace("cloud_renderer");
        public static final Identifier EQUIPMENT_ASSETS = Identifier.withDefaultNamespace("equipment_assets");
        public static final Identifier ENTITY_RENDER_DISPATCHER = Identifier.withDefaultNamespace("entity_render_dispatcher");
        public static final Identifier DRY_FOLIAGE_COLOR = Identifier.withDefaultNamespace("dry_foliage_color");
        public static final Identifier FOLIAGE_COLOR = Identifier.withDefaultNamespace("foliage_color");
        public static final Identifier FONTS = Identifier.withDefaultNamespace("fonts");
        public static final Identifier GRASS_COLOR = Identifier.withDefaultNamespace("grass_color");
        public static final Identifier ATLAS = Identifier.withDefaultNamespace("atlas");
        public static final Identifier LANGUAGES = Identifier.withDefaultNamespace("languages");
        public static final Identifier MODELS = Identifier.withDefaultNamespace("models");
        public static final Identifier PARTICLES = Identifier.withDefaultNamespace("particles");
        public static final Identifier SHADERS = Identifier.withDefaultNamespace("shaders");
        public static final Identifier SOUNDS = Identifier.withDefaultNamespace("sounds");
        public static final Identifier SPLASH_TEXTS = Identifier.withDefaultNamespace("splash_texts");
        public static final Identifier TEXTURES = Identifier.withDefaultNamespace("textures");
        public static final Identifier WAYPOINT_STYLE = Identifier.withDefaultNamespace("waypoint_style");

        private Client() {
        }
    }
}

