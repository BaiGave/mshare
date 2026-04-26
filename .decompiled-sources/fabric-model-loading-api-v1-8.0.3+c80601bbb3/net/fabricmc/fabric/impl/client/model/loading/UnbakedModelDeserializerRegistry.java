/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.model.loading;

import com.google.gson.JsonParseException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.fabricmc.fabric.mixin.client.model.loading.CuboidModelAccessor;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

public class UnbakedModelDeserializerRegistry {
    private static final Map<Identifier, UnbakedModelDeserializer> DESERIALIZERS = new HashMap<Identifier, UnbakedModelDeserializer>();

    public static void register(Identifier id, UnbakedModelDeserializer deserializer) {
        Objects.requireNonNull(id, "id cannot be null");
        Objects.requireNonNull(id, "deserializer cannot be null");
        if (DESERIALIZERS.putIfAbsent(id, deserializer) != null) {
            throw new IllegalArgumentException("UnbakedModelDeserializer with identifier '" + String.valueOf(id) + "' already registered");
        }
    }

    public static UnbakedModelDeserializer get(Identifier id) {
        Objects.requireNonNull(id, "id cannot be null");
        return DESERIALIZERS.get(id);
    }

    public static UnbakedModel deserialize(Reader reader) throws JsonParseException {
        return GsonHelper.fromJson(CuboidModelAccessor.fabric_getGson(), reader, UnbakedModel.class);
    }
}

