/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.model.loading;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

public class UnbakedModelJsonDeserializer
implements JsonDeserializer<UnbakedModel> {
    private static final String TYPE_KEY = "fabric:type";
    private static final String TYPE_ID_KEY = "id";
    private static final String TYPE_OPTIONAL_KEY = "optional";

    @Override
    public UnbakedModel deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        if (jsonObject.has(TYPE_KEY)) {
            boolean optional;
            String idStr;
            JsonElement typeElement = jsonObject.get(TYPE_KEY);
            if (typeElement.isJsonPrimitive()) {
                idStr = typeElement.getAsString();
                optional = false;
            } else if (typeElement.isJsonObject()) {
                JsonObject typeObject = typeElement.getAsJsonObject();
                idStr = GsonHelper.getAsString(typeObject, TYPE_ID_KEY);
                optional = GsonHelper.getAsBoolean(typeObject, TYPE_OPTIONAL_KEY, false);
            } else {
                throw new JsonSyntaxException("Expected fabric:type to be a string or object, was " + GsonHelper.getType(typeElement));
            }
            Identifier id = Identifier.parse(idStr);
            UnbakedModelDeserializer deserializer = UnbakedModelDeserializer.get(id);
            if (deserializer != null) {
                return deserializer.deserialize(jsonObject, context);
            }
            if (!optional) {
                throw new JsonParseException("Cannot deserialize custom unbaked model of unknown type '" + String.valueOf(id) + "'");
            }
        }
        return (UnbakedModel)context.deserialize(jsonElement, (Type)((Object)CuboidModel.class));
    }
}

