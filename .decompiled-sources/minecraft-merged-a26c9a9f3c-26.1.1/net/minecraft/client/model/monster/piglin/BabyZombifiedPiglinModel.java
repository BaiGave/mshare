/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model.monster.piglin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.monster.piglin.BabyPiglinModel;
import net.minecraft.client.model.monster.piglin.ZombifiedPiglinModel;

@Environment(value=EnvType.CLIENT)
public class BabyZombifiedPiglinModel
extends ZombifiedPiglinModel {
    public BabyZombifiedPiglinModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        return BabyPiglinModel.createBodyLayer();
    }

    @Override
    float getDefaultEarAngleInDegrees() {
        return 5.0f;
    }
}

