/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

@Environment(value=EnvType.CLIENT)
public class CommonButtons {
    public static SpriteIconButton language(int width, Button.OnPress onPress, boolean iconOnly) {
        return SpriteIconButton.builder(Component.translatable("options.language"), onPress, iconOnly).width(width).sprite(Identifier.withDefaultNamespace("icon/language"), 15, 15).build();
    }

    public static SpriteIconButton accessibility(int width, Button.OnPress onPress, boolean iconOnly) {
        MutableComponent text = iconOnly ? Component.translatable("options.accessibility") : Component.translatable("accessibility.onboarding.accessibility.button");
        return SpriteIconButton.builder(text, onPress, iconOnly).width(width).sprite(Identifier.withDefaultNamespace("icon/accessibility"), 15, 15).build();
    }
}

