/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.google.gson.JsonElement;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.LenientJsonParser;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.fixes.References;
import org.slf4j.Logger;

public class UnflattenTextComponentFix
extends DataFix {
    private static final Logger LOGGER = LogUtils.getLogger();

    public UnflattenTextComponentFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<Pair<String, String>> textComponentType = this.getInputSchema().getType(References.TEXT_COMPONENT);
        Type<?> newTextComponentType = this.getOutputSchema().getType(References.TEXT_COMPONENT);
        return this.createFixer(textComponentType, newTextComponentType);
    }

    private <T> TypeRewriteRule createFixer(Type<Pair<String, String>> textComponentType, Type<T> newTextComponentType) {
        return this.fixTypeEverywhere("UnflattenTextComponentFix", textComponentType, newTextComponentType, ops -> input -> Util.readTypedOrThrow(newTextComponentType, UnflattenTextComponentFix.unflattenJson(ops, (String)input.getSecond()), true).getValue());
    }

    private static <T> Dynamic<T> unflattenJson(DynamicOps<T> ops, String jsonString) {
        try {
            JsonElement json = LenientJsonParser.parse(jsonString);
            if (!json.isJsonNull()) {
                return new Dynamic<T>(ops, JsonOps.INSTANCE.convertTo(ops, json));
            }
        }
        catch (Exception e) {
            LOGGER.error("Failed to unflatten text component json: {}", (Object)jsonString, (Object)e);
        }
        return new Dynamic<T>(ops, ops.createString(jsonString));
    }
}

