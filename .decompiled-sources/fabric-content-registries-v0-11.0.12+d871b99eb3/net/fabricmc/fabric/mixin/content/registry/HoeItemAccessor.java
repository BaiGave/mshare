/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.content.registry;

import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={HoeItem.class})
public interface HoeItemAccessor {
    @Accessor(value="TILLABLES")
    public static Map<Block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>> getTillables() {
        throw new AssertionError((Object)"Untransformed @Accessor");
    }
}

