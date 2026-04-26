/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class EntityCustomNameToComponentFix
extends DataFix {
    public EntityCustomNameToComponentFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> entityType = this.getInputSchema().getType(References.ENTITY);
        Type<?> newEntityType = this.getOutputSchema().getType(References.ENTITY);
        OpticFinder<String> idF = DSL.fieldFinder("id", NamespacedSchema.namespacedString());
        OpticFinder<?> customNameF = entityType.findField("CustomName");
        Type<?> newCustomNameType = newEntityType.findFieldType("CustomName");
        return this.fixTypeEverywhereTyped("EntityCustomNameToComponentFix", entityType, newEntityType, (Typed<?> entity) -> EntityCustomNameToComponentFix.fixEntity(entity, newEntityType, idF, customNameF, newCustomNameType));
    }

    private static <T> Typed<?> fixEntity(Typed<?> entity, Type<?> newEntityType, OpticFinder<String> idF, OpticFinder<String> customNameF, Type<T> newCustomNameType) {
        Optional<String> customName = entity.getOptional(customNameF);
        if (customName.isEmpty()) {
            return ExtraDataFixUtils.cast(newEntityType, entity);
        }
        if (customName.get().isEmpty()) {
            return Util.writeAndReadTypedOrThrow(entity, newEntityType, dynamic -> dynamic.remove("CustomName"));
        }
        String id = entity.getOptional(idF).orElse("");
        Dynamic<?> component = EntityCustomNameToComponentFix.fixCustomName(entity.getOps(), customName.get(), id);
        return entity.set(customNameF, Util.readTypedOrThrow(newCustomNameType, component));
    }

    private static <T> Dynamic<T> fixCustomName(DynamicOps<T> ops, String customName, String id) {
        if ("minecraft:commandblock_minecart".equals(id)) {
            return new Dynamic<T>(ops, ops.createString(customName));
        }
        return LegacyComponentDataFixUtils.createPlainTextComponent(ops, customName);
    }
}

