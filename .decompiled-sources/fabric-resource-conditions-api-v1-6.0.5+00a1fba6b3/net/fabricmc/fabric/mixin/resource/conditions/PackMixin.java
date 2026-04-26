/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.resource.conditions;

import com.llamalad7.mixinextras.sugar.Local;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.impl.resource.conditions.OverlayConditionsMetadata;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value={Pack.class})
public class PackMixin {
    @ModifyVariable(method={"readPackMetadata"}, at=@At(value="STORE"), name={"overlaySet"})
    private static List<String> applyOverlayConditions(List<String> overlays, @Local(name={"pack"}) PackResources pack) throws IOException {
        ArrayList<String> appliedOverlays = new ArrayList<String>(overlays);
        OverlayConditionsMetadata overlayMetadata = pack.getMetadataSection(OverlayConditionsMetadata.SERIALIZER);
        if (overlayMetadata != null) {
            appliedOverlays.addAll(overlayMetadata.appliedOverlays());
        }
        return List.copyOf(appliedOverlays);
    }
}

