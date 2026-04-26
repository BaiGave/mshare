/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.rendering.hud;

import java.util.function.Function;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.resources.Identifier;

public interface HudLayer {
    public Identifier id();

    public HudElement element(HudElement var1);

    public boolean isRemoved();

    public static HudLayer ofVanilla(Identifier id) {
        return HudLayer.of(id, Function.identity(), false);
    }

    public static HudLayer ofElement(Identifier id, HudElement element) {
        return HudLayer.of(id, $ -> element, false);
    }

    public static HudLayer of(final Identifier id, final Function<HudElement, HudElement> operator, final boolean isRemoved) {
        return new HudLayer(){

            @Override
            public Identifier id() {
                return id;
            }

            @Override
            public HudElement element(HudElement vanillaElement) {
                return (HudElement)operator.apply(vanillaElement);
            }

            @Override
            public boolean isRemoved() {
                return isRemoved;
            }
        };
    }
}

