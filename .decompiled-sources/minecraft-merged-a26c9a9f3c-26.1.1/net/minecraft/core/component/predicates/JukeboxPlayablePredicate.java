/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.core.component.predicates;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.criterion.SingleComponentItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.JukeboxSong;

public record JukeboxPlayablePredicate(Optional<HolderSet<JukeboxSong>> song) implements SingleComponentItemPredicate<JukeboxPlayable>
{
    public static final Codec<JukeboxPlayablePredicate> CODEC = RecordCodecBuilder.create(i -> i.group(RegistryCodecs.homogeneousList(Registries.JUKEBOX_SONG).optionalFieldOf("song").forGetter(JukeboxPlayablePredicate::song)).apply((Applicative<JukeboxPlayablePredicate, ?>)i, JukeboxPlayablePredicate::new));

    @Override
    public DataComponentType<JukeboxPlayable> componentType() {
        return DataComponents.JUKEBOX_PLAYABLE;
    }

    @Override
    public boolean matches(JukeboxPlayable value) {
        if (this.song.isPresent()) {
            boolean songIsPresent = false;
            for (Holder holder : this.song.get()) {
                Optional songId = holder.unwrapKey();
                if (songId.isEmpty() || !songId.equals(value.song().unwrapKey())) continue;
                songIsPresent = true;
                break;
            }
            return songIsPresent;
        }
        return true;
    }

    public static JukeboxPlayablePredicate any() {
        return new JukeboxPlayablePredicate(Optional.empty());
    }
}

