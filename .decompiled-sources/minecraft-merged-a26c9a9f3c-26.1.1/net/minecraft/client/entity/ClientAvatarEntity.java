/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.entity.ClientAvatarState;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface ClientAvatarEntity {
    public ClientAvatarState avatarState();

    public PlayerSkin getSkin();

    public @Nullable Parrot.Variant getParrotVariantOnShoulder(boolean var1);

    public boolean showExtraEars();
}

