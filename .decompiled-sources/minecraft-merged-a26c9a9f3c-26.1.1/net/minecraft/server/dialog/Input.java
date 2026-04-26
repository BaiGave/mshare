/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.dialog;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.dialog.action.ParsedTemplate;
import net.minecraft.server.dialog.input.InputControl;

public record Input(String key, InputControl control) {
    public static final Codec<Input> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)ParsedTemplate.VARIABLE_CODEC.fieldOf("key")).forGetter(Input::key), InputControl.MAP_CODEC.forGetter(Input::control)).apply((Applicative<Input, ?>)i, Input::new));
}

