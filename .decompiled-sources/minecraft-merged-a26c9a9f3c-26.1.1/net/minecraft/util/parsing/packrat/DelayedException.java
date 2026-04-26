/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.parsing.packrat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.util.parsing.packrat.commands.StringReaderTerms;

public interface DelayedException<T extends Exception> {
    public T create(String var1, int var2);

    public static DelayedException<CommandSyntaxException> create(SimpleCommandExceptionType type) {
        return (contents, position) -> type.createWithContext(StringReaderTerms.createReader(contents, position));
    }

    public static DelayedException<CommandSyntaxException> create(DynamicCommandExceptionType type, String argument) {
        return (contents, position) -> type.createWithContext(StringReaderTerms.createReader(contents, position), argument);
    }
}

