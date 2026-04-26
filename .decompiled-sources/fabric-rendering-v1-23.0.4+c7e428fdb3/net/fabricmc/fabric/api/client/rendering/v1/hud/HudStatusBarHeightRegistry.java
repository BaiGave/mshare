/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1.hud;

import java.util.Objects;
import net.fabricmc.fabric.api.client.rendering.v1.hud.StatusBarHeightProvider;
import net.fabricmc.fabric.impl.client.rendering.hud.HudStatusBarHeightRegistryImpl;
import net.minecraft.resources.Identifier;

public final class HudStatusBarHeightRegistry {
    public static void addLeft(Identifier id, StatusBarHeightProvider heightProvider) {
        Objects.requireNonNull(id, "id is null");
        Objects.requireNonNull(heightProvider, "height provider is null");
        HudStatusBarHeightRegistryImpl.addLeft(id, heightProvider);
    }

    public static void addRight(Identifier id, StatusBarHeightProvider heightProvider) {
        Objects.requireNonNull(id, "id is null");
        Objects.requireNonNull(heightProvider, "height provider is null");
        HudStatusBarHeightRegistryImpl.addRight(id, heightProvider);
    }

    public static int getHeight(Identifier id) {
        Objects.requireNonNull(id, "id is null");
        return HudStatusBarHeightRegistryImpl.getHeight(id);
    }

    private HudStatusBarHeightRegistry() {
    }
}

