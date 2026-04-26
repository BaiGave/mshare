/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.transfer;

import com.mojang.serialization.DataResult;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.component.BundleContents;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={BundleContents.class})
public interface BundleContentsAccessor {
    @Invoker(value="getWeight")
    public static DataResult<Fraction> getWeight(ItemInstance itemInstance) {
        throw new AssertionError((Object)"This shouldn't happen!");
    }
}

