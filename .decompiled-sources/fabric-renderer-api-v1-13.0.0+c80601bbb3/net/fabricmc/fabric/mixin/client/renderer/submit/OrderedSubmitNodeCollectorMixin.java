/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.renderer.submit;

import net.fabricmc.fabric.api.client.renderer.v1.render.FabricOrderedSubmitNodeCollector;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={OrderedSubmitNodeCollector.class})
interface OrderedSubmitNodeCollectorMixin
extends FabricOrderedSubmitNodeCollector {
}

