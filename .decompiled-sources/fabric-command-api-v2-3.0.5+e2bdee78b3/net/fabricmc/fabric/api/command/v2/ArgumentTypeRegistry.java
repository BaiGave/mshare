/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.command.v2;

import com.mojang.brigadier.arguments.ArgumentType;
import net.fabricmc.fabric.mixin.command.ArgumentTypeInfosAccessor;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class ArgumentTypeRegistry {
    public static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void registerArgumentType(Identifier id, Class<? extends A> clazz, ArgumentTypeInfo<A, T> serializer) {
        ArgumentTypeInfosAccessor.fabric_getClassMap().put(clazz, serializer);
        Registry.register(BuiltInRegistries.COMMAND_ARGUMENT_TYPE, id, serializer);
    }

    private ArgumentTypeRegistry() {
    }
}

