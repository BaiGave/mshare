/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.dialog;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.ButtonListDialog;
import net.minecraft.server.dialog.CommonDialogData;
import net.minecraft.util.ExtraCodecs;

public record ServerLinksDialog(CommonDialogData common, Optional<ActionButton> exitAction, int columns, int buttonWidth) implements ButtonListDialog
{
    public static final MapCodec<ServerLinksDialog> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(CommonDialogData.MAP_CODEC.forGetter(ServerLinksDialog::common), ActionButton.CODEC.optionalFieldOf("exit_action").forGetter(ServerLinksDialog::exitAction), ExtraCodecs.POSITIVE_INT.optionalFieldOf("columns", 2).forGetter(ServerLinksDialog::columns), WIDTH_CODEC.optionalFieldOf("button_width", 150).forGetter(ServerLinksDialog::buttonWidth)).apply((Applicative<ServerLinksDialog, ?>)i, ServerLinksDialog::new));

    public MapCodec<ServerLinksDialog> codec() {
        return MAP_CODEC;
    }
}

