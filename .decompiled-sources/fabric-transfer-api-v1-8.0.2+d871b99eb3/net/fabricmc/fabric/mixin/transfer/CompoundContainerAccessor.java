/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.transfer;

import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={CompoundContainer.class})
public interface CompoundContainerAccessor {
    @Accessor(value="container1")
    public Container fabric_getContainer1();

    @Accessor(value="container2")
    public Container fabric_getContainer2();
}

