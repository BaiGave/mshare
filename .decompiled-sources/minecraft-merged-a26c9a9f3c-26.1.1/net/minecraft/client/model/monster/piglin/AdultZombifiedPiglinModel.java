/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model.monster.piglin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.monster.piglin.AdultPiglinModel;
import net.minecraft.client.model.monster.piglin.ZombifiedPiglinModel;

@Environment(value=EnvType.CLIENT)
public class AdultZombifiedPiglinModel
extends ZombifiedPiglinModel {
    public AdultZombifiedPiglinModel(ModelPart root) {
        super(root);
    }

    @Override
    float getDefaultEarAngleInDegrees() {
        return 30.0f;
    }

    public static LayerDefinition createBodyLayer() {
        return AdultPiglinModel.createBodyLayer();
    }
}

