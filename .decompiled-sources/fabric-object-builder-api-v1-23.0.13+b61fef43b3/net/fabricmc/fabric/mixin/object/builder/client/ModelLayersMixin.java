/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.object.builder.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.HangingSignBlock;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ModelLayers.class})
public class ModelLayersMixin {
    @Inject(method={"createStandingSignModelName"}, at={@At(value="HEAD")}, cancellable=true)
    private static void createStandingSign(WoodType type, CallbackInfoReturnable<ModelLayerLocation> cir) {
        if (type.name().indexOf(58) != -1) {
            Identifier identifier = Identifier.parse(type.name());
            cir.setReturnValue(new ModelLayerLocation(identifier.withPrefix("sign/standing/"), "main"));
        }
    }

    @Inject(method={"createWallSignModelName"}, at={@At(value="HEAD")}, cancellable=true)
    private static void createWallSign(WoodType type, CallbackInfoReturnable<ModelLayerLocation> cir) {
        if (type.name().indexOf(58) != -1) {
            Identifier identifier = Identifier.parse(type.name());
            cir.setReturnValue(new ModelLayerLocation(identifier.withPrefix("sign/wall/"), "main"));
        }
    }

    @Inject(method={"createHangingSignModelName"}, at={@At(value="HEAD")}, cancellable=true)
    private static void createHangingSign(WoodType type, HangingSignBlock.Attachment attachmentType, CallbackInfoReturnable<ModelLayerLocation> cir) {
        if (type.name().indexOf(58) != -1) {
            Identifier identifier = Identifier.parse(type.name());
            cir.setReturnValue(new ModelLayerLocation(identifier.withPath(path -> "hanging_sign/" + path + "/" + attachmentType.getSerializedName()), "main"));
        }
    }
}

