/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.rendering;

import java.util.HashMap;
import java.util.Objects;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

public class ArmorRendererRegistryImpl {
    private static final HashMap<Item, ArmorRenderer.Factory> FACTORIES = new HashMap();
    private static final HashMap<Item, ArmorRenderer> RENDERERS = new HashMap();

    public static void register(ArmorRenderer.Factory factory, ItemLike ... items) {
        Objects.requireNonNull(factory, "renderer factory is null");
        if (items.length == 0) {
            throw new IllegalArgumentException("Armor renderer registered for no item");
        }
        for (ItemLike item : items) {
            Objects.requireNonNull(item.asItem(), "armor item is null");
            if (FACTORIES.putIfAbsent(item.asItem(), factory) == null) continue;
            throw new IllegalArgumentException("Custom armor renderer already exists for " + String.valueOf(BuiltInRegistries.ITEM.getKey(item.asItem())));
        }
    }

    public static void register(ArmorRenderer renderer, ItemLike ... items) {
        Objects.requireNonNull(renderer, "renderer is null");
        ArmorRendererRegistryImpl.register((EntityRendererProvider.Context context) -> renderer, items);
    }

    public static @Nullable ArmorRenderer get(Item item) {
        return RENDERERS.get(item);
    }

    public static void createArmorRenderers(EntityRendererProvider.Context context) {
        RENDERERS.clear();
        FACTORIES.forEach((item, factory) -> RENDERERS.put((Item)item, factory.createArmorRenderer(context)));
    }
}

