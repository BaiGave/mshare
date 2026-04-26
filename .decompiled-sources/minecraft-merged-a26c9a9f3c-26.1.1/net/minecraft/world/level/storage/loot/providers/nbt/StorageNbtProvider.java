/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.storage.loot.providers.nbt;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;

public record StorageNbtProvider(Identifier id) implements NbtProvider
{
    public static final MapCodec<StorageNbtProvider> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Identifier.CODEC.fieldOf("source")).forGetter(StorageNbtProvider::id)).apply((Applicative<StorageNbtProvider, ?>)i, StorageNbtProvider::new));

    public MapCodec<StorageNbtProvider> codec() {
        return MAP_CODEC;
    }

    @Override
    public Tag get(LootContext context) {
        return context.getLevel().getServer().getCommandStorage().get(this.id);
    }
}

