/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.util;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum EventResult implements StringRepresentable
{
    ALLOW("allow"),
    PASS("pass"),
    DENY("deny");

    public static final Codec<EventResult> CODEC;
    private final String name;

    public boolean allowAction() {
        return this != DENY;
    }

    public boolean allowAction(boolean passResult) {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 1 -> passResult;
            case 0 -> true;
            case 2 -> false;
        };
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    private EventResult(String name) {
        this.name = name;
    }

    static {
        CODEC = StringRepresentable.fromEnum(EventResult::values);
    }
}

