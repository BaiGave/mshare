/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.resource.pack;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackSource;

public record BuiltinModPackSource(String modId) implements PackSource
{
    @Override
    public boolean shouldAddAutomatically() {
        return true;
    }

    @Override
    public Component decorate(Component packName) {
        return Component.translatable("pack.nameAndSource", packName, Component.translatable("pack.source.builtinMod", this.modId)).withStyle(ChatFormatting.GRAY);
    }
}

