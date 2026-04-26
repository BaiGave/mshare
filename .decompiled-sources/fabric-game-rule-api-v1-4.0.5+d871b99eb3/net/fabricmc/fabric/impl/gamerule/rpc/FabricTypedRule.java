/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.gamerule.rpc;

import net.fabricmc.fabric.impl.gamerule.rpc.FabricGameRuleType;
import org.jspecify.annotations.Nullable;

public interface FabricTypedRule {
    public @Nullable FabricGameRuleType getFabricType();

    public void setFabricType(FabricGameRuleType var1);
}

