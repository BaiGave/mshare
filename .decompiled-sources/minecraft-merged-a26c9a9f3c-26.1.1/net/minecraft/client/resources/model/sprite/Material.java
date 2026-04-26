/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources.model.sprite;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

@Environment(value=EnvType.CLIENT)
public record Material(Identifier sprite, boolean forceTranslucent) {
    private static final Codec<Material> SIMPLE_CODEC = Identifier.CODEC.xmap(Material::new, Material::sprite);
    private static final Codec<Material> FULL_CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)Identifier.CODEC.fieldOf("sprite")).forGetter(Material::sprite), Codec.BOOL.optionalFieldOf("force_translucent", false).forGetter(Material::forceTranslucent)).apply((Applicative<Material, ?>)i, Material::new));
    public static final Codec<Material> CODEC = Codec.either(SIMPLE_CODEC, FULL_CODEC).xmap(Either::unwrap, material -> material.forceTranslucent ? Either.right(material) : Either.left(material));

    public Material(Identifier sprite) {
        this(sprite, false);
    }

    public Material withForceTranslucent(boolean forceTranslucent) {
        return new Material(this.sprite, forceTranslucent);
    }

    @Environment(value=EnvType.CLIENT)
    public record Baked(TextureAtlasSprite sprite, boolean forceTranslucent) {
    }
}

