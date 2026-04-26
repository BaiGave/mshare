/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.dialog.body;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStackTemplate;

public record ItemBody(ItemStackTemplate item, Optional<PlainMessage> description, boolean showDecorations, boolean showTooltip, int width, int height) implements DialogBody
{
    public static final MapCodec<ItemBody> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)ItemStackTemplate.CODEC.fieldOf("item")).forGetter(ItemBody::item), PlainMessage.CODEC.optionalFieldOf("description").forGetter(ItemBody::description), Codec.BOOL.optionalFieldOf("show_decorations", true).forGetter(ItemBody::showDecorations), Codec.BOOL.optionalFieldOf("show_tooltip", true).forGetter(ItemBody::showTooltip), ExtraCodecs.intRange(1, 256).optionalFieldOf("width", 16).forGetter(ItemBody::width), ExtraCodecs.intRange(1, 256).optionalFieldOf("height", 16).forGetter(ItemBody::height)).apply((Applicative<ItemBody, ?>)i, ItemBody::new));

    public MapCodec<ItemBody> mapCodec() {
        return MAP_CODEC;
    }
}

