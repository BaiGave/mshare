/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.network.chat.Component;

public class RotationArgument
implements ArgumentType<Coordinates> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0 0", "~ ~", "~-5 ~5");
    public static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(Component.translatable("argument.rotation.incomplete"));

    public static RotationArgument rotation() {
        return new RotationArgument();
    }

    public static Coordinates getRotation(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, Coordinates.class);
    }

    @Override
    public Coordinates parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        if (!reader.canRead()) {
            throw ERROR_NOT_COMPLETE.createWithContext(reader);
        }
        WorldCoordinate y = WorldCoordinate.parseDouble(reader, false);
        if (!reader.canRead() || reader.peek() != ' ') {
            reader.setCursor(start);
            throw ERROR_NOT_COMPLETE.createWithContext(reader);
        }
        reader.skip();
        WorldCoordinate x = WorldCoordinate.parseDouble(reader, false);
        return new WorldCoordinates(x, y, new WorldCoordinate(true, 0.0));
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}

