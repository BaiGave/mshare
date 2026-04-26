/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.command;

import java.util.Map;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ArgumentTypeInfos.class})
public interface ArgumentTypeInfosAccessor {
    @Accessor(value="BY_CLASS")
    public static Map<Class<?>, ArgumentTypeInfo<?, ?>> fabric_getClassMap() {
        throw new AssertionError((Object)"");
    }
}

