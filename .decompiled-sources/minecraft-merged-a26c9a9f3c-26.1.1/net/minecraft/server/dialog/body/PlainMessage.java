/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.dialog.body;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.body.DialogBody;

public record PlainMessage(Component contents, int width) implements DialogBody
{
    public static final int DEFAULT_WIDTH = 200;
    public static final MapCodec<PlainMessage> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)ComponentSerialization.CODEC.fieldOf("contents")).forGetter(PlainMessage::contents), Dialog.WIDTH_CODEC.optionalFieldOf("width", 200).forGetter(PlainMessage::width)).apply((Applicative<PlainMessage, ?>)i, PlainMessage::new));
    public static final Codec<PlainMessage> CODEC = Codec.withAlternative(MAP_CODEC.codec(), ComponentSerialization.CODEC, contents -> new PlainMessage((Component)contents, 200));

    public MapCodec<PlainMessage> mapCodec() {
        return MAP_CODEC;
    }
}

