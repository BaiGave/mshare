/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources.model.cuboid;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.reflect.Type;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Environment(value=EnvType.CLIENT)
public record ItemTransform(Vector3fc rotation, Vector3fc translation, Vector3fc scale) {
    public static final ItemTransform NO_TRANSFORM = new ItemTransform(new Vector3f(), new Vector3f(), new Vector3f(1.0f, 1.0f, 1.0f));

    public void apply(boolean applyLeftHandFix, PoseStack.Pose pose) {
        float rotZ;
        float rotY;
        float translationX;
        if (this == NO_TRANSFORM) {
            pose.translate(-0.5f, -0.5f, -0.5f);
            return;
        }
        if (applyLeftHandFix) {
            translationX = -this.translation.x();
            rotY = -this.rotation.y();
            rotZ = -this.rotation.z();
        } else {
            translationX = this.translation.x();
            rotY = this.rotation.y();
            rotZ = this.rotation.z();
        }
        pose.translate(translationX, this.translation.y(), this.translation.z());
        pose.rotate(new Quaternionf().rotationXYZ(this.rotation.x() * ((float)Math.PI / 180), rotY * ((float)Math.PI / 180), rotZ * ((float)Math.PI / 180)));
        pose.scale(this.scale.x(), this.scale.y(), this.scale.z());
        pose.translate(-0.5f, -0.5f, -0.5f);
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Deserializer
    implements JsonDeserializer<ItemTransform> {
        private static final Vector3f DEFAULT_ROTATION = new Vector3f(0.0f, 0.0f, 0.0f);
        private static final Vector3f DEFAULT_TRANSLATION = new Vector3f(0.0f, 0.0f, 0.0f);
        private static final Vector3f DEFAULT_SCALE = new Vector3f(1.0f, 1.0f, 1.0f);
        public static final float MAX_TRANSLATION = 5.0f;
        public static final float MAX_SCALE = 4.0f;

        protected Deserializer() {
        }

        @Override
        public ItemTransform deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            Vector3f rotation = this.getVector3f(object, "rotation", DEFAULT_ROTATION);
            Vector3f translation = this.getVector3f(object, "translation", DEFAULT_TRANSLATION);
            translation.mul(0.0625f);
            translation.set(Mth.clamp(translation.x, -5.0f, 5.0f), Mth.clamp(translation.y, -5.0f, 5.0f), Mth.clamp(translation.z, -5.0f, 5.0f));
            Vector3f scale = this.getVector3f(object, "scale", DEFAULT_SCALE);
            scale.set(Mth.clamp(scale.x, -4.0f, 4.0f), Mth.clamp(scale.y, -4.0f, 4.0f), Mth.clamp(scale.z, -4.0f, 4.0f));
            return new ItemTransform(rotation, translation, scale);
        }

        private Vector3f getVector3f(JsonObject object, String key, Vector3f def) {
            if (!object.has(key)) {
                return def;
            }
            JsonArray vecArray = GsonHelper.getAsJsonArray(object, key);
            if (vecArray.size() != 3) {
                throw new JsonParseException("Expected 3 " + key + " values, found: " + vecArray.size());
            }
            float[] elements = new float[3];
            for (int i = 0; i < elements.length; ++i) {
                elements[i] = GsonHelper.convertToFloat(vecArray.get(i), key + "[" + i + "]");
            }
            return new Vector3f(elements[0], elements[1], elements[2]);
        }
    }
}

