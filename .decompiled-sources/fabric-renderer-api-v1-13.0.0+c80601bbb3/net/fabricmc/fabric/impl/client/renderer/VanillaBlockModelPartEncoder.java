/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.renderer;

import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.ShadeMode;
import net.fabricmc.fabric.api.client.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public class VanillaBlockModelPartEncoder {
    public static void emitQuads(BlockStateModelPart part, QuadEmitter emitter, Predicate<@Nullable Direction> cullTest) {
        TriState ao = part.useAmbientOcclusion() ? TriState.DEFAULT : TriState.FALSE;
        for (int i = 0; i <= 6; ++i) {
            Direction cullFace = ModelHelper.faceFromIndex(i);
            if (cullTest.test(cullFace)) continue;
            List<BakedQuad> quads = part.getQuads(cullFace);
            int quadCount = quads.size();
            for (int j = 0; j < quadCount; ++j) {
                BakedQuad q = quads.get(j);
                emitter.cullFace(cullFace);
                emitter.fromBakedQuad(q);
                emitter.ambientOcclusion(ao);
                emitter.shadeMode(ShadeMode.VANILLA);
                emitter.emit();
            }
        }
    }
}

