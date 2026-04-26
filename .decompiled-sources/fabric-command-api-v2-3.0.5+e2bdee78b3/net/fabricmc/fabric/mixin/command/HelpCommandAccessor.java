/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.command;

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.commands.HelpCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={HelpCommand.class})
public interface HelpCommandAccessor {
    @Accessor(value="ERROR_FAILED")
    public static SimpleCommandExceptionType getFailedException() {
        throw new AssertionError((Object)"mixin");
    }
}

