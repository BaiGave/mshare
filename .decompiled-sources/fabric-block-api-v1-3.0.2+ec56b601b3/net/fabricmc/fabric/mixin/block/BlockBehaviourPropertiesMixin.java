/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.block;

import net.fabricmc.fabric.api.block.v1.FabricBlock;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={BlockBehaviour.Properties.class})
public class BlockBehaviourPropertiesMixin
implements FabricBlock.FabricProperties {
    @Shadow
    private @Nullable ResourceKey<Block> id;

    @Override
    public @Nullable ResourceKey<Block> blockId() {
        return this.id;
    }
}

