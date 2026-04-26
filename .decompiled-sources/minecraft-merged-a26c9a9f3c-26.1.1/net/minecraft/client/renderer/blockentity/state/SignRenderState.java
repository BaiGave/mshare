/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.blockentity.state;

import com.mojang.math.Transformation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SignRenderState
extends BlockEntityRenderState {
    public WoodType woodType = WoodType.OAK;
    public @Nullable SignText frontText;
    public @Nullable SignText backText;
    public int textLineHeight;
    public int maxTextLineWidth;
    public boolean isTextFilteringEnabled;
    public boolean drawOutline;
    public SignTransformations transformations = SignTransformations.IDENTITY;

    @Environment(value=EnvType.CLIENT)
    public record SignTransformations(Transformation body, Transformation frontText, Transformation backText) {
        public static final SignTransformations IDENTITY = new SignTransformations(Transformation.IDENTITY, Transformation.IDENTITY, Transformation.IDENTITY);
    }
}

