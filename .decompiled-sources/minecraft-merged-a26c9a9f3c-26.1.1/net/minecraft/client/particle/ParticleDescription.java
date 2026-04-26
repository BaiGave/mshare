/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.particle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

@Environment(value=EnvType.CLIENT)
public class ParticleDescription {
    private final List<Identifier> textures;

    private ParticleDescription(List<Identifier> textures) {
        this.textures = textures;
    }

    public List<Identifier> getTextures() {
        return this.textures;
    }

    public static ParticleDescription fromJson(JsonObject data) {
        JsonArray texturesData = GsonHelper.getAsJsonArray(data, "textures", null);
        if (texturesData == null) {
            return new ParticleDescription(List.of());
        }
        List textures = Streams.stream(texturesData).map(element -> GsonHelper.convertToString(element, "texture")).map(Identifier::parse).collect(ImmutableList.toImmutableList());
        return new ParticleDescription(textures);
    }
}

