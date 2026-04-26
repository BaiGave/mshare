/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.gamerule.entry;

import com.mojang.serialization.DataResult;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.mixin.gamerule.client.AbstractGameRulesScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.worldselection.AbstractGameRulesScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.gamerules.GameRule;

public final class DoubleRuleEntry
extends AbstractGameRulesScreen.GameRuleEntry {
    private final EditBox input;

    public DoubleRuleEntry(AbstractGameRulesScreen gameRuleScreen, Component name, List<FormattedCharSequence> description, String ruleName, GameRule<Double> doubleRule) {
        AbstractGameRulesScreen abstractGameRulesScreen = gameRuleScreen;
        Objects.requireNonNull(abstractGameRulesScreen);
        super(abstractGameRulesScreen, description, name);
        AbstractGameRulesScreenAccessor accessor = (AbstractGameRulesScreenAccessor)((Object)gameRuleScreen);
        this.input = new EditBox(Minecraft.getInstance().font, 10, 5, 42, 20, name.copy().append(CommonComponents.NEW_LINE).append(ruleName).append(CommonComponents.NEW_LINE));
        this.input.setValue(accessor.getGameRules().getAsString(doubleRule));
        this.input.setResponder(value -> {
            DataResult dataResult = doubleRule.deserialize((String)value);
            if (dataResult.isSuccess()) {
                this.input.setTextColor(-2039584);
                accessor.callClearInvalid(this);
                accessor.getGameRules().set(doubleRule, (Double)dataResult.getOrThrow(), null);
            } else {
                this.input.setTextColor(-65536);
                accessor.callMarkInvalid(this);
            }
        });
        this.children.add(this.input);
    }

    @Override
    public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        this.extractLabel(graphics, this.getContentY(), this.getContentX());
        this.input.setX(this.getContentRight() - 44);
        this.input.setY(this.getContentY());
        this.input.extractRenderState(graphics, mouseX, mouseY, tickDelta);
    }
}

