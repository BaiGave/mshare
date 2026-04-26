/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.content.registry;

import java.util.List;
import net.minecraft.world.entity.ai.behavior.WorkAtComposter;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={WorkAtComposter.class})
public interface WorkAtComposterAccessor {
    @Mutable
    @Accessor(value="COMPOSTABLE_ITEMS")
    public static void fabric_setCompostables(List<Item> items) {
        throw new AssertionError((Object)"Untransformed @Accessor");
    }

    @Accessor(value="COMPOSTABLE_ITEMS")
    public static List<Item> fabric_getCompostable() {
        throw new AssertionError((Object)"Untransformed @Accessor");
    }
}

