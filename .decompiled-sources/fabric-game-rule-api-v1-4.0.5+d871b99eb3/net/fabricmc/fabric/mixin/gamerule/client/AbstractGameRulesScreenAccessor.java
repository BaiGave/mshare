/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.gamerule.client;

import net.minecraft.client.gui.screens.worldselection.AbstractGameRulesScreen;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={AbstractGameRulesScreen.class})
public interface AbstractGameRulesScreenAccessor {
    @Invoker(value="clearInvalid")
    public void callClearInvalid(AbstractGameRulesScreen.RuleEntry var1);

    @Invoker(value="markInvalid")
    public void callMarkInvalid(AbstractGameRulesScreen.RuleEntry var1);

    @Accessor(value="gameRules")
    public GameRules getGameRules();
}

