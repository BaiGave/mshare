/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.gamerule.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.Locale;
import net.fabricmc.fabric.api.gamerule.v1.FabricGameRuleTypeVisitor;
import net.fabricmc.fabric.impl.gamerule.RuleTypeExtensions;
import net.fabricmc.fabric.impl.gamerule.entry.DoubleRuleEntry;
import net.fabricmc.fabric.impl.gamerule.entry.EnumRuleEntry;
import net.fabricmc.fabric.impl.gamerule.rpc.FabricGameRuleType;
import net.fabricmc.fabric.mixin.gamerule.client.AbstractGameRulesScreenRuleListAccessor;
import net.minecraft.client.gui.screens.worldselection.AbstractGameRulesScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets={"net.minecraft.client.gui.screens.worldselection.AbstractGameRulesScreen$RuleList$1"})
public abstract class RuleListEntryTypeVisitorMixin
implements GameRuleTypeVisitor,
FabricGameRuleTypeVisitor {
    @Final
    @Shadow
    private AbstractGameRulesScreen.RuleList this$1;

    @Shadow
    protected abstract <T> void addEntry(GameRule<T> var1, AbstractGameRulesScreen.EntryFactory<T> var2);

    @Override
    public void visitDouble(GameRule<Double> doubleRule) {
        this.addEntry(doubleRule, (name, description, ruleName, rule) -> new DoubleRuleEntry(this.getThis(), name, description, ruleName, rule));
    }

    @Override
    public <E extends Enum<E>> void visitEnum(GameRule<E> enumRule) {
        this.addEntry(enumRule, (name, description, ruleName, rule) -> new EnumRuleEntry(this.getThis(), name, description, ruleName, rule, enumRule.getDescriptionId()));
    }

    @Unique
    AbstractGameRulesScreen getThis() {
        return ((AbstractGameRulesScreenRuleListAccessor)((Object)this.this$1)).getThis();
    }

    @WrapOperation(method={"Lnet/minecraft/client/gui/screens/worldselection/AbstractGameRulesScreen$RuleList$1;addEntry(Lnet/minecraft/world/level/gamerules/GameRule;Lnet/minecraft/client/gui/screens/worldselection/AbstractGameRulesScreen$EntryFactory;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/level/gamerules/GameRule;serialize(Ljava/lang/Object;)Ljava/lang/String;")})
    private <T> String displayProperEnumName(GameRule<T> instance, T value, Operation<String> original) {
        String valueName = original.call(instance, value);
        if (((RuleTypeExtensions)((Object)instance)).fabric_getType() != FabricGameRuleType.ENUM) {
            return valueName;
        }
        String translationKey = instance.getDescriptionId() + "." + valueName.toLowerCase(Locale.ROOT);
        if (I18n.exists(translationKey)) {
            return I18n.get(translationKey, new Object[0]);
        }
        return valueName;
    }
}

