/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.google.gson.JsonElement;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import java.lang.invoke.CallSite;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.fixes.References;

public class LegacyHoverEventFix
extends DataFix {
    public LegacyHoverEventFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> hoverEventType = this.getInputSchema().getType(References.TEXT_COMPONENT).findFieldType("hoverEvent");
        return this.createFixer(this.getInputSchema().getTypeRaw(References.TEXT_COMPONENT), hoverEventType);
    }

    private <C, H extends Pair<String, ?>> TypeRewriteRule createFixer(Type<C> rawTextComponentType, Type<H> hoverEventType) {
        Type<Pair<String, Either<Either<String, C>, Pair<Either<C, Unit>, Pair<Either<C, Unit>, Pair<Either<H, Unit>, Dynamic<?>>>>>>> textComponentType = DSL.named(References.TEXT_COMPONENT.typeName(), DSL.or(DSL.or(DSL.string(), DSL.list(rawTextComponentType)), DSL.and(DSL.optional(DSL.field("extra", DSL.list(rawTextComponentType))), DSL.optional(DSL.field("separator", rawTextComponentType)), DSL.optional(DSL.field("hoverEvent", hoverEventType)), DSL.remainderType())));
        if (!textComponentType.equals(this.getInputSchema().getType(References.TEXT_COMPONENT))) {
            throw new IllegalStateException("Text component type did not match, expected " + String.valueOf(textComponentType) + " but got " + String.valueOf(this.getInputSchema().getType(References.TEXT_COMPONENT)));
        }
        return this.fixTypeEverywhere("LegacyHoverEventFix", textComponentType, ops -> named -> named.mapSecond(simpleOrFull -> simpleOrFull.mapRight(full -> full.mapSecond(separatorHoverRemainder -> separatorHoverRemainder.mapSecond(hoverAndRemainder -> {
            Dynamic remainder = (Dynamic)hoverAndRemainder.getSecond();
            Optional hoverEvent = remainder.get("hoverEvent").result();
            if (hoverEvent.isEmpty()) {
                return hoverAndRemainder;
            }
            Optional legacyHoverValue = hoverEvent.get().get("value").result();
            if (legacyHoverValue.isEmpty()) {
                return hoverAndRemainder;
            }
            String hoverAction = ((Either)hoverAndRemainder.getFirst()).left().map(Pair::getFirst).orElse("");
            Pair newHoverEvent = (Pair)this.fixHoverEvent(hoverEventType, hoverAction, hoverEvent.get());
            return hoverAndRemainder.mapFirst(ignored -> Either.left(newHoverEvent));
        })))));
    }

    private <H> H fixHoverEvent(Type<H> hoverEventType, String action, Dynamic<?> oldHoverEvent) {
        if ("show_text".equals(action)) {
            return LegacyHoverEventFix.fixShowTextHover(hoverEventType, oldHoverEvent);
        }
        return LegacyHoverEventFix.createPlaceholderHover(hoverEventType, oldHoverEvent);
    }

    private static <H> H fixShowTextHover(Type<H> hoverEventType, Dynamic<?> oldHoverEvent) {
        Dynamic<?> newHoverEvent = oldHoverEvent.renameField("value", "contents");
        return Util.readTypedOrThrow(hoverEventType, newHoverEvent).getValue();
    }

    private static <H> H createPlaceholderHover(Type<H> hoverEventType, Dynamic<?> oldHoverEvent) {
        JsonElement oldJson = oldHoverEvent.convert(JsonOps.INSTANCE).getValue();
        Dynamic<Map<String, Map<String, CallSite>>> placeholderHoverEvent = new Dynamic<Map<String, Map<String, CallSite>>>(JavaOps.INSTANCE, Map.of("action", "show_text", "contents", Map.of("text", "Legacy hoverEvent: " + GsonHelper.toStableString(oldJson))));
        return Util.readTypedOrThrow(hoverEventType, placeholderHoverEvent).getValue();
    }
}

