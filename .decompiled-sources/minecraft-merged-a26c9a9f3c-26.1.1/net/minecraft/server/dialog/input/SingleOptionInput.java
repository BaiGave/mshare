/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.dialog.input;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.input.InputControl;
import net.minecraft.util.ExtraCodecs;

public record SingleOptionInput(int width, List<Entry> entries, Component label, boolean labelVisible) implements InputControl
{
    public static final MapCodec<SingleOptionInput> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(Dialog.WIDTH_CODEC.optionalFieldOf("width", 200).forGetter(SingleOptionInput::width), ((MapCodec)ExtraCodecs.nonEmptyList(Entry.CODEC.listOf()).fieldOf("options")).forGetter(SingleOptionInput::entries), ((MapCodec)ComponentSerialization.CODEC.fieldOf("label")).forGetter(SingleOptionInput::label), Codec.BOOL.optionalFieldOf("label_visible", true).forGetter(SingleOptionInput::labelVisible)).apply((Applicative<SingleOptionInput, ?>)i, SingleOptionInput::new)).validate(o -> {
        long initialCount = o.entries.stream().filter(Entry::initial).count();
        if (initialCount > 1L) {
            return DataResult.error(() -> "Multiple initial values");
        }
        return DataResult.success(o);
    });

    public MapCodec<SingleOptionInput> mapCodec() {
        return MAP_CODEC;
    }

    public Optional<Entry> initial() {
        return this.entries.stream().filter(Entry::initial).findFirst();
    }

    public record Entry(String id, Optional<Component> display, boolean initial) {
        public static final Codec<Entry> FULL_CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)Codec.STRING.fieldOf("id")).forGetter(Entry::id), ComponentSerialization.CODEC.optionalFieldOf("display").forGetter(Entry::display), Codec.BOOL.optionalFieldOf("initial", false).forGetter(Entry::initial)).apply((Applicative<Entry, ?>)i, Entry::new));
        public static final Codec<Entry> CODEC = Codec.withAlternative(FULL_CODEC, Codec.STRING, id -> new Entry((String)id, Optional.empty(), false));

        public Component displayOrDefault() {
            return this.display.orElseGet(() -> Component.literal(this.id));
        }
    }
}

