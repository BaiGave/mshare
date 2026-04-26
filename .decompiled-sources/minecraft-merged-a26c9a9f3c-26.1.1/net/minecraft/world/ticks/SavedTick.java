/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.ticks;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.Hash;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;
import org.jspecify.annotations.Nullable;

public record SavedTick<T>(T type, BlockPos pos, int delay, TickPriority priority) {
    public static final Hash.Strategy<SavedTick<?>> UNIQUE_TICK_HASH = new Hash.Strategy<SavedTick<?>>(){

        @Override
        public int hashCode(SavedTick<?> o) {
            return 31 * o.pos().hashCode() + o.type().hashCode();
        }

        @Override
        public boolean equals(@Nullable SavedTick<?> a, @Nullable SavedTick<?> b) {
            if (a == b) {
                return true;
            }
            if (a == null || b == null) {
                return false;
            }
            return a.type() == b.type() && a.pos().equals(b.pos());
        }
    };

    public static <T> Codec<SavedTick<T>> codec(Codec<T> typeCodec) {
        MapCodec posCodec = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Codec.INT.fieldOf("x")).forGetter(Vec3i::getX), ((MapCodec)Codec.INT.fieldOf("y")).forGetter(Vec3i::getY), ((MapCodec)Codec.INT.fieldOf("z")).forGetter(Vec3i::getZ)).apply((Applicative<BlockPos, ?>)i, BlockPos::new));
        return RecordCodecBuilder.create(i -> i.group(((MapCodec)typeCodec.fieldOf("i")).forGetter(SavedTick::type), posCodec.forGetter(SavedTick::pos), ((MapCodec)Codec.INT.fieldOf("t")).forGetter(SavedTick::delay), ((MapCodec)TickPriority.CODEC.fieldOf("p")).forGetter(SavedTick::priority)).apply((Applicative<SavedTick, ?>)i, SavedTick::new));
    }

    public static <T> List<SavedTick<T>> filterTickListForChunk(List<SavedTick<T>> savedTicks, ChunkPos chunkPos) {
        long posKey = chunkPos.pack();
        return savedTicks.stream().filter(tick -> ChunkPos.pack(tick.pos()) == posKey).toList();
    }

    public ScheduledTick<T> unpack(long currentTick, long currentSubTick) {
        return new ScheduledTick<T>(this.type, this.pos, currentTick + (long)this.delay, this.priority, currentSubTick);
    }

    public static <T> SavedTick<T> probe(T type, BlockPos pos) {
        return new SavedTick<T>(type, pos, 0, TickPriority.NORMAL);
    }
}

