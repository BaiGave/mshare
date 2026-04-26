/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.rendering.hud;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.impl.client.rendering.hud.HudLayer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.VisibleForTesting;

public class HudElementRegistryImpl {
    @VisibleForTesting
    static final List<Identifier> VANILLA_ELEMENT_IDS = List.of(VanillaHudElements.MISC_OVERLAYS, VanillaHudElements.CROSSHAIR, VanillaHudElements.SPECTATOR_MENU, VanillaHudElements.HOTBAR, VanillaHudElements.ARMOR_BAR, VanillaHudElements.HEALTH_BAR, VanillaHudElements.FOOD_BAR, VanillaHudElements.AIR_BAR, VanillaHudElements.MOUNT_HEALTH, VanillaHudElements.INFO_BAR, VanillaHudElements.EXPERIENCE_LEVEL, VanillaHudElements.HELD_ITEM_TOOLTIP, VanillaHudElements.SPECTATOR_TOOLTIP, VanillaHudElements.MOB_EFFECTS, VanillaHudElements.BOSS_BAR, VanillaHudElements.SLEEP, VanillaHudElements.DEMO_TIMER, VanillaHudElements.SCOREBOARD, VanillaHudElements.OVERLAY_MESSAGE, VanillaHudElements.TITLE_AND_SUBTITLE, VanillaHudElements.CHAT, VanillaHudElements.PLAYER_LIST, VanillaHudElements.SUBTITLES);
    @VisibleForTesting
    public static final Map<Identifier, RootLayer> ROOT_ELEMENTS = VANILLA_ELEMENT_IDS.stream().map(RootLayer::new).collect(Collectors.toMap(RootLayer::id, Function.identity(), (a, b) -> a, IdentityHashMap::new));
    private static final RootLayer FIRST = ROOT_ELEMENTS.get(VanillaHudElements.MISC_OVERLAYS);
    private static final RootLayer LAST = ROOT_ELEMENTS.get(VanillaHudElements.SUBTITLES);

    public static RootLayer getRoot(Identifier id) {
        return ROOT_ELEMENTS.get(id);
    }

    public static void addFirst(Identifier id, HudElement element) {
        HudElementRegistryImpl.validateUnique(id);
        FIRST.layers().addFirst(HudLayer.ofElement(id, element));
    }

    public static void addLast(Identifier id, HudElement element) {
        HudElementRegistryImpl.validateUnique(id);
        LAST.layers().addLast(HudLayer.ofElement(id, element));
    }

    public static void attachElementBefore(Identifier beforeThis, Identifier id, HudElement element) {
        HudElementRegistryImpl.validateUnique(id);
        boolean didChange = HudElementRegistryImpl.findLayer(beforeThis, (l, iterator) -> {
            iterator.previous();
            iterator.add(HudLayer.ofElement(id, element));
            iterator.next();
            return true;
        });
        if (!didChange) {
            throw new IllegalArgumentException("Layer with identifier " + String.valueOf(beforeThis) + " not found");
        }
    }

    public static void attachElementAfter(Identifier afterThis, Identifier id, HudElement element) {
        HudElementRegistryImpl.validateUnique(id);
        boolean didChange = HudElementRegistryImpl.findLayer(afterThis, (l, iterator) -> {
            iterator.add(HudLayer.ofElement(id, element));
            return true;
        });
        if (!didChange) {
            throw new IllegalArgumentException("Layer with identifier " + String.valueOf(afterThis) + " not found");
        }
    }

    public static void removeElement(Identifier identifier) {
        boolean didChange = HudElementRegistryImpl.findLayer(identifier, (l, iterator) -> {
            iterator.set(HudLayer.of(l.id(), l::element, true));
            return true;
        });
        if (!didChange) {
            throw new IllegalArgumentException("Layer with identifier " + String.valueOf(identifier) + " not found");
        }
    }

    public static void replaceElement(Identifier identifier, Function<HudElement, HudElement> replacer) {
        boolean didChange = HudElementRegistryImpl.findLayer(identifier, (l, iterator) -> {
            iterator.set(HudLayer.of(l.id(), replacer.compose(l::element), l.isRemoved()));
            return true;
        });
        if (!didChange) {
            throw new IllegalArgumentException("Layer with identifier " + String.valueOf(identifier) + " not found");
        }
    }

    @VisibleForTesting
    static void validateUnique(Identifier id) {
        HudElementRegistryImpl.visitLayers((l, iterator) -> {
            if (l.id().equals(id)) {
                throw new IllegalArgumentException("Layer with identifier " + String.valueOf(id) + " already exists");
            }
            return false;
        });
    }

    @VisibleForTesting
    static boolean findLayer(Identifier identifier, LayerVisitor visitor) {
        MutableBoolean found = new MutableBoolean(false);
        HudElementRegistryImpl.visitLayers((l, iterator) -> {
            if (l.id().equals(identifier)) {
                found.setTrue();
                return visitor.visit(l, iterator);
            }
            return false;
        });
        return found.booleanValue();
    }

    @VisibleForTesting
    static boolean visitLayers(LayerVisitor visitor) {
        boolean modified = false;
        for (Identifier id : VANILLA_ELEMENT_IDS) {
            RootLayer rootLayer = ROOT_ELEMENTS.get(id);
            modified |= HudElementRegistryImpl.visitLayers(rootLayer.layers(), visitor);
        }
        return modified;
    }

    private static boolean visitLayers(List<HudLayer> layers, LayerVisitor visitor) {
        MutableBoolean modified = new MutableBoolean(false);
        ListIterator<HudLayer> iterator = layers.listIterator();
        while (iterator.hasNext()) {
            HudLayer layer = iterator.next();
            if (!visitor.visit(layer, iterator)) continue;
            modified.setTrue();
        }
        return modified.booleanValue();
    }

    public record RootLayer(Identifier id, List<HudLayer> layers) {
        private RootLayer(Identifier id) {
            this(id, new ArrayList<HudLayer>());
            this.layers().add(HudLayer.ofVanilla(id));
        }

        public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, HudElement vanillaElement) {
            for (HudLayer layer : this.layers) {
                if (layer.isRemoved()) continue;
                layer.element(vanillaElement).extractRenderState(graphics, deltaTracker);
            }
        }
    }

    @VisibleForTesting
    static interface LayerVisitor {
        public boolean visit(HudLayer var1, ListIterator<HudLayer> var2);
    }
}

