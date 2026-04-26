/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.gamerule.client;

import net.minecraft.client.gui.screens.worldselection.AbstractGameRulesScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={AbstractGameRulesScreen.RuleList.class})
public interface AbstractGameRulesScreenRuleListAccessor {
    @Accessor(value="this$0")
    public AbstractGameRulesScreen getThis();
}

