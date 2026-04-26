/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.command;

import java.util.function.Predicate;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={EntitySelectorOptions.class})
public interface EntitySelectorOptionsAccessor {
    @Invoker(value="register")
    public static void callPutOption(String id, EntitySelectorOptions.Modifier modifier, Predicate<EntitySelectorParser> condition, Component description) {
    }
}

