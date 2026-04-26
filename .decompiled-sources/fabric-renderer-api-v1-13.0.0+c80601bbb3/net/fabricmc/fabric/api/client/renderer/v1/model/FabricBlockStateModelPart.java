/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.renderer.v1.model;

import java.util.function.Predicate;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.impl.client.renderer.VanillaBlockModelPartEncoder;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public interface FabricBlockStateModelPart {
    default public void emitQuads(QuadEmitter emitter, Predicate<@Nullable Direction> cullTest) {
        VanillaBlockModelPartEncoder.emitQuads((BlockStateModelPart)this, emitter, cullTest);
    }
}

