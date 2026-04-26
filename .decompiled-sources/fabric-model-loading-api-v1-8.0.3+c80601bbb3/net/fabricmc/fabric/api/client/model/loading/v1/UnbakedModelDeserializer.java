/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.model.loading.v1;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.Reader;
import net.fabricmc.fabric.impl.client.model.loading.UnbakedModelDeserializerRegistry;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public interface UnbakedModelDeserializer {
    public static void register(Identifier id, UnbakedModelDeserializer deserializer) {
        UnbakedModelDeserializerRegistry.register(id, deserializer);
    }

    public static @Nullable UnbakedModelDeserializer get(Identifier id) {
        return UnbakedModelDeserializerRegistry.get(id);
    }

    public static UnbakedModel deserialize(Reader reader) throws JsonParseException {
        return UnbakedModelDeserializerRegistry.deserialize(reader);
    }

    public UnbakedModel deserialize(JsonObject var1, JsonDeserializationContext var2);
}

