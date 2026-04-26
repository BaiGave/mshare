/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.dialog.action;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.server.dialog.action.Action;
import net.minecraft.server.dialog.action.ParsedTemplate;

public record CommandTemplate(ParsedTemplate template) implements Action
{
    public static final MapCodec<CommandTemplate> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)ParsedTemplate.CODEC.fieldOf("template")).forGetter(CommandTemplate::template)).apply((Applicative<CommandTemplate, ?>)i, CommandTemplate::new));

    public MapCodec<CommandTemplate> codec() {
        return MAP_CODEC;
    }

    @Override
    public Optional<ClickEvent> createAction(Map<String, Action.ValueGetter> parameters) {
        String command = this.template.instantiate(Action.ValueGetter.getAsTemplateSubstitutions(parameters));
        return Optional.of(new ClickEvent.RunCommand(command));
    }
}

