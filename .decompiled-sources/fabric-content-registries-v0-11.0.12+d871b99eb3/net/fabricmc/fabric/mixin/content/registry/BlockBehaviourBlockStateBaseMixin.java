/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.content.registry;

import net.fabricmc.fabric.impl.content.registry.OxidizableBlocksRegistryImpl;
import net.fabricmc.fabric.mixin.content.registry.BlockBehaviourAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={BlockBehaviour.BlockStateBase.class})
public abstract class BlockBehaviourBlockStateBaseMixin
extends StateHolder<Block, BlockState>
implements OxidizableBlocksRegistryImpl.RandomTickCacheRefresher {
    @Shadow
    private boolean isRandomlyTicking;

    @Shadow
    protected abstract BlockState asState();

    protected BlockBehaviourBlockStateBaseMixin(Block owner, Property<?>[] propertyKeys, Comparable<?>[] propertyValues) {
        super(owner, propertyKeys, propertyValues);
    }

    @Override
    public void fabric_api$refreshRandomTickCache() {
        this.isRandomlyTicking = ((BlockBehaviourAccessor)this.owner).callIsRandomlyTicking(this.asState());
    }
}

