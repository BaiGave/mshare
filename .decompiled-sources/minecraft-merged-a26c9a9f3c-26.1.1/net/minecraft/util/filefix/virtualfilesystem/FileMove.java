/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.filefix.virtualfilesystem;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.file.Path;
import net.minecraft.util.ExtraCodecs;

public record FileMove(Path from, Path to) {
    public static Codec<FileMove> moveCodec(Path fromDirectory, Path toDirectory) {
        return RecordCodecBuilder.create(i -> i.group(((MapCodec)ExtraCodecs.guardedPathCodec(fromDirectory).fieldOf("from")).forGetter(r -> r.from), ((MapCodec)ExtraCodecs.guardedPathCodec(toDirectory).fieldOf("to")).forGetter(r -> r.to)).apply((Applicative<FileMove, ?>)i, FileMove::new));
    }
}

