/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.object.builder.v1.world.poi;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class PoiHelper {
    private PoiHelper() {
    }

    public static PoiType register(Identifier id, int ticketCount, int searchDistance, Block ... blocks) {
        ImmutableSet.Builder builder = ImmutableSet.builder();
        for (Block block : blocks) {
            builder.addAll(block.getStateDefinition().getPossibleStates());
        }
        return PoiHelper.register(id, ticketCount, searchDistance, (Set<BlockState>)((Object)builder.build()));
    }

    public static PoiType register(Identifier id, int ticketCount, int searchDistance, Iterable<BlockState> blocks) {
        ImmutableSet.Builder builder = ImmutableSet.builder();
        return PoiHelper.register(id, ticketCount, searchDistance, (Set<BlockState>)((Object)((ImmutableSet.Builder)builder.addAll(blocks)).build()));
    }

    private static PoiType register(Identifier id, int ticketCount, int searchDistance, Set<BlockState> states) {
        return PoiTypes.register(BuiltInRegistries.POINT_OF_INTEREST_TYPE, ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, id), states, ticketCount, searchDistance);
    }
}

