/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.transfer;

import com.mojang.serialization.DataResult;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={ItemStack.class})
public interface ItemStackAccessor {
    @Invoker(value="validateComponents")
    public static DataResult<?> validateComponents(DataComponentMap components) {
        throw new UnsupportedOperationException();
    }
}

