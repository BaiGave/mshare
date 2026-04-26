/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.transfer.fluid;

import com.google.common.collect.MapMaker;
import com.google.common.primitives.Ints;
import java.util.Map;
import net.fabricmc.fabric.api.transfer.v1.fluid.CauldronFluidContent;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.fabricmc.fabric.impl.transfer.DebugMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class CauldronStorage
extends SnapshotParticipant<BlockState>
implements SingleSlotStorage<FluidVariant> {
    private static final Map<LevelLocation, CauldronStorage> CAULDRONS = new MapMaker().concurrencyLevel(1).weakValues().makeMap();
    private final LevelLocation location;
    private BlockState lastReleasedSnapshot;

    public static CauldronStorage get(Level level, BlockPos pos) {
        LevelLocation location = new LevelLocation(level, pos.immutable());
        return CAULDRONS.computeIfAbsent(location, CauldronStorage::new);
    }

    CauldronStorage(LevelLocation location) {
        this.location = location;
    }

    @Override
    protected void releaseSnapshot(BlockState snapshot) {
        this.lastReleasedSnapshot = snapshot;
    }

    private CauldronFluidContent getCurrentContent() {
        CauldronFluidContent content = CauldronFluidContent.getForBlock(this.createSnapshot().getBlock());
        if (content == null) {
            throw new IllegalStateException("Unexpected error: no cauldron at location " + String.valueOf(this.location));
        }
        return content;
    }

    private void updateLevel(CauldronFluidContent newContent, int level, TransactionContext transaction) {
        this.updateSnapshots(transaction);
        BlockState newState = newContent.block.defaultBlockState();
        if (newContent.levelProperty != null) {
            newState = (BlockState)newState.setValue(newContent.levelProperty, level);
        }
        this.location.level.setBlock(this.location.pos, newState, 0);
    }

    @Override
    public long insert(FluidVariant fluidVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(fluidVariant, maxAmount);
        CauldronFluidContent insertContent = CauldronFluidContent.getForFluid(fluidVariant.getFluid());
        if (insertContent != null) {
            int maxLevelsInserted = Ints.saturatedCast(maxAmount / insertContent.amountPerLevel);
            if (this.getAmount() == 0L) {
                int levelsInserted = Math.min(maxLevelsInserted, insertContent.maxLevel);
                if (levelsInserted > 0) {
                    this.updateLevel(insertContent, levelsInserted, transaction);
                }
                return (long)levelsInserted * insertContent.amountPerLevel;
            }
            CauldronFluidContent currentContent = this.getCurrentContent();
            if (fluidVariant.isOf(currentContent.fluid)) {
                int currentLevel = currentContent.currentLevel(this.createSnapshot());
                int levelsInserted = Math.min(maxLevelsInserted, currentContent.maxLevel - currentLevel);
                if (levelsInserted > 0) {
                    this.updateLevel(currentContent, currentLevel + levelsInserted, transaction);
                }
                return (long)levelsInserted * currentContent.amountPerLevel;
            }
        }
        return 0L;
    }

    @Override
    public long extract(FluidVariant fluidVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(fluidVariant, maxAmount);
        CauldronFluidContent currentContent = this.getCurrentContent();
        if (fluidVariant.isOf(currentContent.fluid)) {
            int currentLevel;
            int maxLevelsExtracted = Ints.saturatedCast(maxAmount / currentContent.amountPerLevel);
            int levelsExtracted = Math.min(maxLevelsExtracted, currentLevel = currentContent.currentLevel(this.createSnapshot()));
            if (levelsExtracted > 0) {
                if (levelsExtracted == currentLevel) {
                    this.updateSnapshots(transaction);
                    this.location.level.setBlock(this.location.pos, Blocks.CAULDRON.defaultBlockState(), 0);
                } else {
                    this.updateLevel(currentContent, currentLevel - levelsExtracted, transaction);
                }
            }
            return (long)levelsExtracted * currentContent.amountPerLevel;
        }
        return 0L;
    }

    @Override
    public boolean isResourceBlank() {
        return this.getResource().isBlank();
    }

    @Override
    public FluidVariant getResource() {
        return FluidVariant.of(this.getCurrentContent().fluid);
    }

    @Override
    public long getAmount() {
        CauldronFluidContent currentContent = this.getCurrentContent();
        return (long)currentContent.currentLevel(this.createSnapshot()) * currentContent.amountPerLevel;
    }

    @Override
    public long getCapacity() {
        CauldronFluidContent currentContent = this.getCurrentContent();
        return (long)currentContent.maxLevel * currentContent.amountPerLevel;
    }

    @Override
    public BlockState createSnapshot() {
        return this.location.level.getBlockState(this.location.pos);
    }

    @Override
    public void readSnapshot(BlockState savedState) {
        this.location.level.setBlock(this.location.pos, savedState, 0);
    }

    @Override
    public void onFinalCommit() {
        BlockState originalState = this.lastReleasedSnapshot;
        BlockState state = this.createSnapshot();
        if (originalState != state) {
            this.location.level.setBlock(this.location.pos, originalState, 0);
            this.location.level.setBlockAndUpdate(this.location.pos, state);
        }
    }

    public String toString() {
        return "CauldronStorage[" + String.valueOf(this.location) + "]";
    }

    private record LevelLocation(Level level, BlockPos pos) {
        @Override
        public String toString() {
            return DebugMessages.forGlobalPos(this.level, this.pos);
        }
    }
}

