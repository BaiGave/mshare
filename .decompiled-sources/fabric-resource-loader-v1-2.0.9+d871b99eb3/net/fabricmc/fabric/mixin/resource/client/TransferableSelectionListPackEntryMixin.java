/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.resource.client;

import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.impl.resource.client.PackTooltipComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={TransferableSelectionList.PackEntry.class})
class TransferableSelectionListPackEntryMixin {
    @Shadow
    @Final
    private static int MAX_DESCRIPTION_WIDTH_PIXELS;
    @Shadow
    @Final
    protected Minecraft minecraft;
    @Shadow
    @Final
    private StringWidget nameWidget;
    @Shadow
    @Final
    private MultiLineTextWidget descriptionWidget;
    @Shadow
    @Final
    TransferableSelectionList this$0;

    TransferableSelectionListPackEntryMixin() {
    }

    @Inject(method={"extractContent"}, at={@At(value="RETURN")})
    private void onExtractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        if (hovered) {
            List<FormattedCharSequence> splitDescription;
            Component name = null;
            boolean includeDescription = false;
            if (this.nameWidget.getWidth() < this.minecraft.font.width(this.nameWidget.getMessage().getVisualOrderText())) {
                name = this.nameWidget.getMessage();
            }
            if ((splitDescription = this.minecraft.font.split(this.descriptionWidget.getMessage(), MAX_DESCRIPTION_WIDTH_PIXELS - (this.this$0.maxScrollAmount() > 0 ? 6 : 0))).size() > 2) {
                includeDescription = true;
            }
            if (name != null || includeDescription) {
                graphics.setTooltipForNextFrame(this.minecraft.font, List.of(), Optional.of(new PackTooltipComponent(Optional.ofNullable(name), includeDescription ? Optional.of(splitDescription) : Optional.empty())), mouseX, mouseY);
            }
        }
    }
}

