/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.object.builder;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BlockEntityType.class})
public class BlockEntityTypeMixin<T extends BlockEntity>
implements FabricBlockEntityType {
    @Mutable
    @Shadow
    @Final
    private Set<Block> validBlocks;

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void mutableBlocks(BlockEntityType.BlockEntitySupplier<? extends T> factory, Set<Block> blocks, CallbackInfo ci) {
        if (!(this.validBlocks instanceof HashSet)) {
            this.validBlocks = new HashSet<Block>(this.validBlocks);
        }
    }

    @Override
    public void addValidBlock(Block block) {
        Objects.requireNonNull(block, "block");
        this.validBlocks.add(block);
    }
}

