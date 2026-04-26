/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1;

import java.util.Set;
import net.fabricmc.fabric.impl.client.rendering.ColorResolverRegistryImpl;
import net.minecraft.world.level.ColorResolver;
import org.jetbrains.annotations.UnmodifiableView;

public final class ColorResolverRegistry {
    private ColorResolverRegistry() {
    }

    public static void register(ColorResolver resolver) {
        ColorResolverRegistryImpl.register(resolver);
    }

    public static @UnmodifiableView Set<ColorResolver> getAllResolvers() {
        return ColorResolverRegistryImpl.getAllResolvers();
    }

    public static @UnmodifiableView Set<ColorResolver> getCustomResolvers() {
        return ColorResolverRegistryImpl.getCustomResolvers();
    }

    public static boolean isRegistered(ColorResolver resolver) {
        return ColorResolverRegistry.getAllResolvers().contains(resolver);
    }
}

