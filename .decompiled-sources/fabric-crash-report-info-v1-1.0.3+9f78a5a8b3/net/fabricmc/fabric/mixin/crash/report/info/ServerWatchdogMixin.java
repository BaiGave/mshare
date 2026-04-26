/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.crash.report.info;

import java.lang.management.ThreadInfo;
import net.fabricmc.fabric.impl.crash.report.info.ThreadPrinting;
import net.minecraft.server.dedicated.ServerWatchdog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value={ServerWatchdog.class})
public class ServerWatchdogMixin {
    @ModifyArg(method={"createWatchdogCrashReport(Ljava/lang/String;J)Lnet/minecraft/CrashReport;"}, at=@At(value="INVOKE", target="Ljava/lang/StringBuilder;append(Ljava/lang/Object;)Ljava/lang/StringBuilder;", ordinal=0))
    private static Object printEntireThreadDump(Object object) {
        if (object instanceof ThreadInfo) {
            ThreadInfo threadInfo = (ThreadInfo)object;
            return ThreadPrinting.fullThreadInfoToString(threadInfo);
        }
        return object;
    }
}

