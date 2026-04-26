/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.registry;

import net.fabricmc.fabric.impl.content.registry.StrippableBlockRegistryImpl;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.Nullable;

public final class StrippableBlockRegistry {
    private StrippableBlockRegistry() {
    }

    public static void register(Block input, Block stripped) {
        StrippingTransformer transformer = input.defaultBlockState().hasProperty(BlockStateProperties.AXIS) && stripped.defaultBlockState().hasProperty(BlockStateProperties.AXIS) ? StrippingTransformer.VANILLA : StrippingTransformer.DEFAULT_STATE;
        StrippableBlockRegistryImpl.register(input, stripped, transformer);
    }

    public static void registerCopyState(Block input, Block stripped) {
        StrippableBlockRegistryImpl.register(input, stripped, StrippingTransformer.COPY);
    }

    public static void register(Block input, Block stripped, StrippingTransformer transformer) {
        StrippableBlockRegistryImpl.register(input, stripped, transformer);
    }

    public static @Nullable BlockState getStrippedBlockState(BlockState blockState) {
        return StrippableBlockRegistryImpl.getStrippedBlockState(blockState);
    }

    public static interface StrippingTransformer {
        public static final StrippingTransformer DEFAULT_STATE = (strippedBlock, originalState) -> strippedBlock.defaultBlockState();
        public static final StrippingTransformer VANILLA = (strippedBlock, originalState) -> (BlockState)strippedBlock.defaultBlockState().trySetValue(BlockStateProperties.AXIS, originalState.getValueOrElse(BlockStateProperties.AXIS, Direction.Axis.Y));
        public static final StrippingTransformer COPY = Block::withPropertiesOf;

        public @Nullable BlockState getStrippedBlockState(Block var1, BlockState var2);

        public static StrippingTransformer copyOf(Property<?> ... properties) {
            if (properties.length == 0) {
                return DEFAULT_STATE;
            }
            if (properties.length == 1 && properties[0] == BlockStateProperties.AXIS) {
                return VANILLA;
            }
            return (strippedBlock, originalState) -> {
                BlockState state = strippedBlock.defaultBlockState();
                for (Property property : properties) {
                    if (!originalState.hasProperty(property)) continue;
                    state = (BlockState)state.trySetValue(property, originalState.getValue(property));
                }
                return state;
            };
        }
    }
}

