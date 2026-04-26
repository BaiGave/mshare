/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources.metadata.gui;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.server.packs.metadata.MetadataSectionType;

@Environment(value=EnvType.CLIENT)
public record GuiMetadataSection(GuiSpriteScaling scaling) {
    public static final GuiMetadataSection DEFAULT = new GuiMetadataSection(GuiSpriteScaling.DEFAULT);
    public static final Codec<GuiMetadataSection> CODEC = RecordCodecBuilder.create(i -> i.group(GuiSpriteScaling.CODEC.optionalFieldOf("scaling", GuiSpriteScaling.DEFAULT).forGetter(GuiMetadataSection::scaling)).apply((Applicative<GuiMetadataSection, ?>)i, GuiMetadataSection::new));
    public static final MetadataSectionType<GuiMetadataSection> TYPE = new MetadataSectionType<GuiMetadataSection>("gui", CODEC);
}

