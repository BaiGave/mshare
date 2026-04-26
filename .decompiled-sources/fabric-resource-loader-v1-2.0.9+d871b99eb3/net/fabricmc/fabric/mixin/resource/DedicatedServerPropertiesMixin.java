/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.resource;

import net.fabricmc.fabric.impl.resource.pack.ModPackResourcesUtil;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.world.level.WorldDataConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={DedicatedServerProperties.class})
public class DedicatedServerPropertiesMixin {
    @Redirect(method={"<init>"}, at=@At(value="FIELD", target="Lnet/minecraft/world/level/WorldDataConfiguration;DEFAULT:Lnet/minecraft/world/level/WorldDataConfiguration;", opcode=178))
    private WorldDataConfiguration replaceDefaultDataConfiguration() {
        return ModPackResourcesUtil.createDefaultDataConfiguration();
    }
}

