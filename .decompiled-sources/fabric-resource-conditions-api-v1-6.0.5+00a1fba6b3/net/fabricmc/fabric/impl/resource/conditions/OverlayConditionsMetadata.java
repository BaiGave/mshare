/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.resource.conditions;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.minecraft.server.packs.metadata.MetadataSectionType;

public record OverlayConditionsMetadata(List<Entry> overlays) {
    public static final Codec<OverlayConditionsMetadata> CODEC = ((MapCodec)Entry.CODEC.listOf().fieldOf("entries")).xmap(OverlayConditionsMetadata::new, OverlayConditionsMetadata::overlays).codec();
    public static final MetadataSectionType<OverlayConditionsMetadata> SERIALIZER = new MetadataSectionType<OverlayConditionsMetadata>("fabric:overlays", CODEC);

    public List<String> appliedOverlays() {
        ArrayList<String> appliedOverlays = new ArrayList<String>();
        for (Entry entry : this.overlays()) {
            if (!entry.condition().test(null)) continue;
            appliedOverlays.add(entry.directory());
        }
        return appliedOverlays;
    }

    public record Entry(String directory, ResourceCondition condition) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.STRING.validate(Entry::validateDirectory).fieldOf("directory")).forGetter(Entry::directory), ((MapCodec)ResourceCondition.CODEC.fieldOf("condition")).forGetter(Entry::condition)).apply((Applicative<Entry, ?>)instance, Entry::new));
        private static final Pattern DIRECTORY_NAME_PATTERN = Pattern.compile("[-_a-zA-Z0-9.]+");

        private static DataResult<String> validateDirectory(String directory) {
            boolean valid = DIRECTORY_NAME_PATTERN.matcher(directory).matches();
            return valid ? DataResult.success(directory) : DataResult.error(() -> "Directory name is invalid");
        }
    }
}

