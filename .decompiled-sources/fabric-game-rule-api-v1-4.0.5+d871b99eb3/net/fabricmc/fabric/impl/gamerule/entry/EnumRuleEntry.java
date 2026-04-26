/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.gamerule.entry;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.fabricmc.fabric.impl.gamerule.RuleTypeExtensions;
import net.fabricmc.fabric.mixin.gamerule.client.AbstractGameRulesScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.AbstractGameRulesScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.gamerules.GameRule;

public final class EnumRuleEntry<E extends Enum<E>>
extends AbstractGameRulesScreen.GameRuleEntry {
    private final Button button;
    private final String rootTranslationKey;

    public EnumRuleEntry(AbstractGameRulesScreen gameRuleScreen, Component name, List<FormattedCharSequence> description, String ruleName, GameRule<E> enumRule, String translationKey) {
        AbstractGameRulesScreen abstractGameRulesScreen = gameRuleScreen;
        Objects.requireNonNull(abstractGameRulesScreen);
        super(abstractGameRulesScreen, description, name);
        AbstractGameRulesScreenAccessor accessor = (AbstractGameRulesScreenAccessor)((Object)gameRuleScreen);
        this.label = Minecraft.getInstance().font.split(name, 131);
        this.rootTranslationKey = translationKey;
        this.button = Button.builder(this.getValueComponent((Enum)accessor.getGameRules().get(enumRule)), button -> {
            accessor.getGameRules().set(enumRule, ((RuleTypeExtensions)((Object)enumRule)).fabric_enumCycle((Enum)accessor.getGameRules().get(enumRule)), null);
            button.setMessage(this.getValueComponent((Enum)accessor.getGameRules().get(enumRule)));
        }).bounds(10, 5, 42, 20).build();
        this.children.add(this.button);
    }

    public Component getValueComponent(E value) {
        String key = this.rootTranslationKey + "." + ((Enum)value).name().toLowerCase(Locale.ROOT);
        return Component.translatableWithFallback(key, ((Enum)value).toString());
    }

    @Override
    public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
        this.extractLabel(graphics, this.getContentY(), this.getContentX());
        this.button.setX(this.getContentRight() - 44);
        this.button.setY(this.getContentY());
        this.button.extractRenderState(graphics, mouseX, mouseY, a);
    }
}

