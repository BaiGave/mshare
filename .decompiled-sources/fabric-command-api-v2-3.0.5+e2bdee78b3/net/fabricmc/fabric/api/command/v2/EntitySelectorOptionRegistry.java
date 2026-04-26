/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.command.v2;

import java.util.function.Predicate;
import net.fabricmc.fabric.mixin.command.EntitySelectorOptionsAccessor;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class EntitySelectorOptionRegistry {
    private EntitySelectorOptionRegistry() {
    }

    public static void register(Identifier id, Component description, EntitySelectorOptions.Modifier modifier, Predicate<EntitySelectorParser> canUse) {
        EntitySelectorOptionsAccessor.callPutOption(id.toDebugFileName(), modifier, canUse, description);
    }

    public static void registerNonRepeatable(Identifier id, Component description, EntitySelectorOptions.Modifier modifier) {
        EntitySelectorOptionRegistry.register(id, description, parser -> {
            modifier.handle(parser);
            parser.setCustomFlag(id, true);
        }, parser -> !parser.getCustomFlag(id));
    }
}

