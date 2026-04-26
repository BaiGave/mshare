/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.dialog;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.dialog.CommonButtonData;
import net.minecraft.server.dialog.action.Action;

public record ActionButton(CommonButtonData button, Optional<Action> action) {
    public static final Codec<ActionButton> CODEC = RecordCodecBuilder.create(i -> i.group(CommonButtonData.MAP_CODEC.forGetter(ActionButton::button), Action.CODEC.optionalFieldOf("action").forGetter(ActionButton::action)).apply((Applicative<ActionButton, ?>)i, ActionButton::new));
}

