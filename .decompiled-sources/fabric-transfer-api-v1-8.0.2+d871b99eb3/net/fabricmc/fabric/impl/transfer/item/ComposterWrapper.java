/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.transfer.item;

import com.google.common.collect.MapMaker;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.fabricmc.fabric.impl.transfer.DebugMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class ComposterWrapper
extends SnapshotParticipant<Float> {
    private static final Map<LevelLocation, ComposterWrapper> COMPOSTERS = new MapMaker().concurrencyLevel(1).weakValues().makeMap();
    private static final float DO_NOTHING = 0.0f;
    private static final float EXTRACT_BONEMEAL = -1.0f;
    private final LevelLocation location;
    private Float increaseProbability = Float.valueOf(0.0f);
    private final TopStorage upStorage = new TopStorage(this);
    private final BottomStorage downStorage = new BottomStorage(this);

    public static @Nullable Storage<ItemVariant> get(Level level, BlockPos pos, @Nullable Direction direction) {
        if (direction != null && direction.getAxis().isVertical()) {
            LevelLocation location = new LevelLocation(level, pos.immutable());
            ComposterWrapper composterWrapper = COMPOSTERS.computeIfAbsent(location, ComposterWrapper::new);
            return direction == Direction.UP ? composterWrapper.upStorage : composterWrapper.downStorage;
        }
        return null;
    }

    private ComposterWrapper(LevelLocation location) {
        this.location = location;
    }

    @Override
    protected Float createSnapshot() {
        return this.increaseProbability;
    }

    @Override
    protected void readSnapshot(Float snapshot) {
        this.increaseProbability = snapshot;
    }

    @Override
    protected void onFinalCommit() {
        if (this.increaseProbability.floatValue() == -1.0f) {
            this.location.setBlockState((BlockState)this.location.getBlockState().setValue(ComposterBlock.LEVEL, 0));
            this.location.level.playSound(null, this.location.pos, SoundEvents.COMPOSTER_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
        } else if (this.increaseProbability.floatValue() > 0.0f) {
            boolean increaseSuccessful;
            BlockState state = this.location.getBlockState();
            boolean bl = increaseSuccessful = state.getValue(ComposterBlock.LEVEL) == 0 || this.location.level.getRandom().nextDouble() < (double)this.increaseProbability.floatValue();
            if (increaseSuccessful) {
                int newLevel = state.getValue(ComposterBlock.LEVEL) + 1;
                BlockState newState = (BlockState)state.setValue(ComposterBlock.LEVEL, newLevel);
                this.location.setBlockState(newState);
                if (newLevel == 7) {
                    this.location.level.scheduleTick(this.location.pos, state.getBlock(), 20);
                }
            }
            this.location.level.levelEvent(1500, this.location.pos, increaseSuccessful ? 1 : 0);
        }
        this.increaseProbability = Float.valueOf(0.0f);
    }

    private record LevelLocation(Level level, BlockPos pos) {
        private BlockState getBlockState() {
            return this.level.getBlockState(this.pos);
        }

        private void setBlockState(BlockState state) {
            this.level.setBlockAndUpdate(this.pos, state);
        }

        @Override
        public String toString() {
            return DebugMessages.forGlobalPos(this.level, this.pos);
        }
    }

    private class TopStorage
    implements InsertionOnlyStorage<ItemVariant> {
        final /* synthetic */ ComposterWrapper this$0;

        private TopStorage(ComposterWrapper composterWrapper) {
            ComposterWrapper composterWrapper2 = composterWrapper;
            Objects.requireNonNull(composterWrapper2);
            this.this$0 = composterWrapper2;
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            if (maxAmount < 1L) {
                return 0L;
            }
            if (this.this$0.increaseProbability.floatValue() != 0.0f) {
                return 0L;
            }
            if (this.this$0.location.getBlockState().getValue(ComposterBlock.LEVEL) >= 7) {
                return 0L;
            }
            float insertedIncreaseProbability = ComposterBlock.COMPOSTABLES.getFloat(resource.getItem());
            if (insertedIncreaseProbability <= 0.0f) {
                return 0L;
            }
            this.this$0.updateSnapshots(transaction);
            this.this$0.increaseProbability = Float.valueOf(insertedIncreaseProbability);
            return 1L;
        }

        public String toString() {
            return "ComposterWrapper[" + String.valueOf(this.this$0.location) + "/top]";
        }
    }

    private class BottomStorage
    implements ExtractionOnlyStorage<ItemVariant>,
    SingleSlotStorage<ItemVariant> {
        private static final ItemVariant BONE_MEAL = ItemVariant.of(Items.BONE_MEAL);
        final /* synthetic */ ComposterWrapper this$0;

        private BottomStorage(ComposterWrapper composterWrapper) {
            ComposterWrapper composterWrapper2 = composterWrapper;
            Objects.requireNonNull(composterWrapper2);
            this.this$0 = composterWrapper2;
        }

        private boolean hasBoneMeal() {
            return this.this$0.increaseProbability.floatValue() == 0.0f && this.this$0.location.getBlockState().getValue(ComposterBlock.LEVEL) == 8;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            if (maxAmount < 1L) {
                return 0L;
            }
            if (!BONE_MEAL.equals(resource)) {
                return 0L;
            }
            if (!this.hasBoneMeal()) {
                return 0L;
            }
            this.this$0.updateSnapshots(transaction);
            this.this$0.increaseProbability = Float.valueOf(-1.0f);
            return 1L;
        }

        @Override
        public boolean isResourceBlank() {
            return this.getResource().isBlank();
        }

        @Override
        public ItemVariant getResource() {
            return BONE_MEAL;
        }

        @Override
        public long getAmount() {
            return this.hasBoneMeal() ? 1L : 0L;
        }

        @Override
        public long getCapacity() {
            return 1L;
        }

        public String toString() {
            return "ComposterWrapper[" + String.valueOf(this.this$0.location) + "/bottom]";
        }
    }
}

