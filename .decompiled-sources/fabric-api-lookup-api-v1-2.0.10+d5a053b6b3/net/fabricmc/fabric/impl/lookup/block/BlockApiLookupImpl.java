/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.lookup.block;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.custom.ApiLookupMap;
import net.fabricmc.fabric.api.lookup.v1.custom.ApiProviderMap;
import net.fabricmc.fabric.mixin.lookup.BlockEntityTypeAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BlockApiLookupImpl<A, C>
implements BlockApiLookup<A, C> {
    private static final Logger LOGGER = LoggerFactory.getLogger("fabric-api-lookup-api-v1/block");
    private static final ApiLookupMap<BlockApiLookup<?, ?>> LOOKUPS = ApiLookupMap.create(BlockApiLookupImpl::new);
    private final Identifier identifier;
    private final Class<A> apiClass;
    private final Class<C> contextClass;
    private final ApiProviderMap<Block, BlockApiLookup.BlockApiProvider<A, C>> providerMap = ApiProviderMap.create();
    private final List<BlockApiLookup.BlockApiProvider<A, C>> fallbackProviders = new CopyOnWriteArrayList<BlockApiLookup.BlockApiProvider<A, C>>();

    public static <A, C> BlockApiLookup<A, C> get(Identifier lookupId, Class<A> apiClass, Class<C> contextClass) {
        return LOOKUPS.getLookup(lookupId, apiClass, contextClass);
    }

    private BlockApiLookupImpl(Identifier identifier, Class<?> apiClass, Class<?> contextClass) {
        this.identifier = identifier;
        this.apiClass = apiClass;
        this.contextClass = contextClass;
    }

    @Override
    public @Nullable A find(Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity, C context) {
        Objects.requireNonNull(level, "Level may not be null.");
        Objects.requireNonNull(pos, "BlockPos may not be null.");
        if (blockEntity == null) {
            if (state == null) {
                state = level.getBlockState(pos);
            }
            if (state.hasBlockEntity()) {
                blockEntity = level.getBlockEntity(pos);
            }
        } else if (state == null) {
            state = blockEntity.getBlockState();
        }
        @Nullable BlockApiLookup.BlockApiProvider<A, C> provider = this.getProvider(state.getBlock());
        A instance = null;
        if (provider != null) {
            instance = provider.find(level, pos, state, blockEntity, context);
        }
        if (instance != null) {
            return instance;
        }
        for (BlockApiLookup.BlockApiProvider<A, C> fallbackProvider : this.fallbackProviders) {
            instance = fallbackProvider.find(level, pos, state, blockEntity, context);
            if (instance == null) continue;
            return instance;
        }
        return null;
    }

    @Override
    public void registerSelf(BlockEntityType<?> ... blockEntityTypes) {
        for (BlockEntityType<?> blockEntityType : blockEntityTypes) {
            Block supportBlock = ((BlockEntityTypeAccessor)((Object)blockEntityType)).getBlocks().iterator().next();
            Objects.requireNonNull(supportBlock, "Could not get a support block for block entity type.");
            Object blockEntity2 = blockEntityType.create(BlockPos.ZERO, supportBlock.defaultBlockState());
            Objects.requireNonNull(blockEntity2, "Instantiated block entity may not be null.");
            if (this.apiClass.isAssignableFrom(blockEntity2.getClass())) continue;
            String errorMessage = String.format("Failed to register self-implementing block entities. API class %s is not assignable from block entity class %s.", this.apiClass.getCanonicalName(), blockEntity2.getClass().getCanonicalName());
            throw new IllegalArgumentException(errorMessage);
        }
        this.registerForBlockEntities((blockEntity, context) -> blockEntity, blockEntityTypes);
    }

    @Override
    public void registerForBlocks(BlockApiLookup.BlockApiProvider<A, C> provider, Block ... blocks) {
        Objects.requireNonNull(provider, "BlockApiProvider may not be null.");
        if (blocks.length == 0) {
            throw new IllegalArgumentException("Must register at least one Block instance with a BlockApiProvider.");
        }
        for (Block block : blocks) {
            Objects.requireNonNull(block, "Encountered null block while registering a block API provider mapping.");
            if (this.providerMap.putIfAbsent(block, provider) == null) continue;
            LOGGER.warn("Encountered duplicate API provider registration for block: " + String.valueOf(BuiltInRegistries.BLOCK.getKey(block)));
        }
    }

    @Override
    public void registerForBlockEntities(BlockApiLookup.BlockEntityApiProvider<A, C> provider, BlockEntityType<?> ... blockEntityTypes) {
        Objects.requireNonNull(provider, "BlockEntityApiProvider may not be null.");
        if (blockEntityTypes.length == 0) {
            throw new IllegalArgumentException("Must register at least one BlockEntityType instance with a BlockEntityApiProvider.");
        }
        for (BlockEntityType<?> blockEntityType : blockEntityTypes) {
            Objects.requireNonNull(blockEntityType, "Encountered null block entity type while registering a block entity API provider mapping.");
            BlockApiLookup.BlockApiProvider<Object, Object> nullCheckedProvider = (level, pos, state, blockEntity, context) -> {
                if (blockEntity == null || blockEntity.getType() != blockEntityType) {
                    return null;
                }
                return provider.find(blockEntity, context);
            };
            Block[] blocks = ((BlockEntityTypeAccessor)((Object)blockEntityType)).getBlocks().toArray(new Block[0]);
            this.registerForBlocks(nullCheckedProvider, blocks);
        }
    }

    @Override
    public void registerFallback(BlockApiLookup.BlockApiProvider<A, C> fallbackProvider) {
        Objects.requireNonNull(fallbackProvider, "BlockApiProvider may not be null.");
        this.fallbackProviders.add(fallbackProvider);
    }

    @Override
    public Identifier getId() {
        return this.identifier;
    }

    @Override
    public Class<A> apiClass() {
        return this.apiClass;
    }

    @Override
    public Class<C> contextClass() {
        return this.contextClass;
    }

    @Override
    public @Nullable BlockApiLookup.BlockApiProvider<A, C> getProvider(Block block) {
        return this.providerMap.get(block);
    }

    public List<BlockApiLookup.BlockApiProvider<A, C>> getFallbackProviders() {
        return this.fallbackProviders;
    }
}

