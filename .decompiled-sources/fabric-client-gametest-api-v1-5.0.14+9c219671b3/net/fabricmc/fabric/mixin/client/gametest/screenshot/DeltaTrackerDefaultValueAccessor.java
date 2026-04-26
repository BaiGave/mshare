/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.gametest.screenshot;

import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={DeltaTracker.DefaultValue.class})
public interface DeltaTrackerDefaultValueAccessor {
    @Invoker(value="<init>")
    public static DeltaTracker.DefaultValue create(float constant) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }
}

