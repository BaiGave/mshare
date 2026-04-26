/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.gamerule.rpc;

import net.minecraft.util.StringRepresentable;

public enum FabricGameRuleType implements StringRepresentable
{
    DOUBLE("fabric:double"),
    ENUM("fabric:enum");

    private final String name;

    private FabricGameRuleType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}

