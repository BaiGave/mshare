/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.InvalidLockComponentFix;
import net.minecraft.util.datafix.fixes.References;

public class InvalidBlockEntityLockFix
extends DataFix {
    public InvalidBlockEntityLockFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("BlockEntityLockToComponentFix", this.getInputSchema().getType(References.BLOCK_ENTITY), blockEntity -> blockEntity.update(DSL.remainderFinder(), remainder -> {
            Optional lock = remainder.get("lock").result();
            if (lock.isEmpty()) {
                return remainder;
            }
            Dynamic newLock = InvalidLockComponentFix.fixLock(lock.get());
            if (newLock != null) {
                return remainder.set("lock", newLock);
            }
            return remainder.remove("lock");
        }));
    }
}

