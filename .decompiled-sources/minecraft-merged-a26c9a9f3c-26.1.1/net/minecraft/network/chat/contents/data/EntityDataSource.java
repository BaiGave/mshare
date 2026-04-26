/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.chat.contents.data;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.advancements.criterion.NbtPredicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.contents.data.DataSource;
import net.minecraft.util.CompilableString;
import net.minecraft.world.entity.Entity;

public record EntityDataSource(CompilableString<EntitySelector> selector) implements DataSource
{
    public static final MapCodec<EntityDataSource> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)EntitySelector.COMPILABLE_CODEC.fieldOf("entity")).forGetter(EntityDataSource::selector)).apply((Applicative<EntityDataSource, ?>)i, EntityDataSource::new));

    @Override
    public Stream<CompoundTag> getData(CommandSourceStack sender) throws CommandSyntaxException {
        List<? extends Entity> entities = this.selector.compiled().findEntities(sender);
        return entities.stream().map(NbtPredicate::getEntityTagToCompare);
    }

    public MapCodec<EntityDataSource> codec() {
        return MAP_CODEC;
    }
}

