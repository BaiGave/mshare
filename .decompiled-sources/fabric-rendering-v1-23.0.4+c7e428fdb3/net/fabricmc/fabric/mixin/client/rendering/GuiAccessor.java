/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering;

import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={Gui.class})
public interface GuiAccessor {
    @Accessor(value="displayHealth")
    public int fabric$getRenderHealthValue();

    @Invoker(value="getPlayerVehicleWithHealth")
    public LivingEntity fabric$callGetRiddenEntity();

    @Invoker(value="getVehicleMaxHearts")
    public int fabric$callGetHeartCount(LivingEntity var1);

    @Invoker(value="getVisibleVehicleHeartRows")
    public int fabric$callGetHeartRows(int var1);

    @Invoker(value="getCameraPlayer")
    public Player fabric$callGetCameraPlayer();
}

