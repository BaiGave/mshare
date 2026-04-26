/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1.hud;

import java.util.Objects;
import java.util.function.Function;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.impl.client.rendering.hud.HudElementRegistryImpl;
import net.minecraft.resources.Identifier;

public interface HudElementRegistry {
    public static void addFirst(Identifier id, HudElement element) {
        Objects.requireNonNull(id, "identifier");
        Objects.requireNonNull(element, "hudElement");
        HudElementRegistryImpl.addFirst(id, element);
    }

    public static void addLast(Identifier id, HudElement element) {
        Objects.requireNonNull(id, "identifier");
        Objects.requireNonNull(element, "hudElement");
        HudElementRegistryImpl.addLast(id, element);
    }

    public static void attachElementBefore(Identifier beforeThis, Identifier identifier, HudElement element) {
        Objects.requireNonNull(beforeThis, "beforeThis");
        Objects.requireNonNull(identifier, "identifier");
        Objects.requireNonNull(element, "hudElement");
        HudElementRegistryImpl.attachElementBefore(beforeThis, identifier, element);
    }

    public static void attachElementAfter(Identifier afterThis, Identifier identifier, HudElement element) {
        Objects.requireNonNull(afterThis, "afterThis");
        Objects.requireNonNull(identifier, "identifier");
        Objects.requireNonNull(element, "hudElement");
        HudElementRegistryImpl.attachElementAfter(afterThis, identifier, element);
    }

    public static void removeElement(Identifier identifier) {
        Objects.requireNonNull(identifier, "identifier");
        HudElementRegistryImpl.removeElement(identifier);
    }

    public static void replaceElement(Identifier identifier, Function<HudElement, HudElement> replacer) {
        Objects.requireNonNull(identifier, "identifier");
        Objects.requireNonNull(replacer, "replacer");
        HudElementRegistryImpl.replaceElement(identifier, replacer);
    }
}

