/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.indigo.renderer;

import net.minecraft.client.renderer.block.BlockModelLighter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={BlockModelLighter.class})
public interface BlockModelLighterAccessor {
    @Accessor(value="CACHE")
    public static ThreadLocal<BlockModelLighter.Cache> fabric_getCACHE() {
        throw new AssertionError();
    }
}

