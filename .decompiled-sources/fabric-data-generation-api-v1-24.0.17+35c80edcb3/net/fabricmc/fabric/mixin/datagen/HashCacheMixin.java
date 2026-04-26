/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.datagen;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import net.minecraft.data.HashCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={HashCache.class})
public abstract class HashCacheMixin {
    @Redirect(method={"lambda$purgeStaleAndWrite$0"}, at=@At(value="INVOKE", target="Ljava/time/ZonedDateTime;now()Ljava/time/ZonedDateTime;"))
    private ZonedDateTime constantTime() {
        return ZonedDateTime.of(LocalDateTime.MIN, ZoneOffset.UTC);
    }
}

