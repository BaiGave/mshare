/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.lookup.v1.block;

import java.util.function.BiFunction;
import net.fabricmc.fabric.impl.lookup.block.BlockApiLookupImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.NonExtendable
public interface BlockApiLookup<A, C> {
    public static <A, C> BlockApiLookup<A, C> get(Identifier lookupId, Class<A> apiClass, Class<C> contextClass) {
        return BlockApiLookupImpl.get(lookupId, apiClass, contextClass);
    }

    default public @Nullable A find(Level level, BlockPos pos, C context) {
        return this.find(level, pos, null, null, context);
    }

    public @Nullable A find(Level var1, BlockPos var2, @Nullable BlockState var3, @Nullable BlockEntity var4, C var5);

    public void registerSelf(BlockEntityType<?> ... var1);

    public void registerForBlocks(BlockApiProvider<A, C> var1, Block ... var2);

    default public <T extends BlockEntity> void registerForBlockEntity(BiFunction<? super T, C, @Nullable A> provider, BlockEntityType<T> blockEntityType) {
        this.registerForBlockEntities((blockEntity, context) -> provider.apply(blockEntity, context), blockEntityType);
    }

    public void registerForBlockEntities(BlockEntityApiProvider<A, C> var1, BlockEntityType<?> ... var2);

    public void registerFallback(BlockApiProvider<A, C> var1);

    public Identifier getId();

    public Class<A> apiClass();

    public Class<C> contextClass();

    public @Nullable BlockApiProvider<A, C> getProvider(Block var1);

    @FunctionalInterface
    public static interface BlockEntityApiProvider<A, C> {
        public @Nullable A find(BlockEntity var1, C var2);
    }

    @FunctionalInterface
    public static interface BlockApiProvider<A, C> {
        public @Nullable A find(Level var1, BlockPos var2, BlockState var3, @Nullable BlockEntity var4, C var5);
    }
}

