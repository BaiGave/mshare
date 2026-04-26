/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.object.builder.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.inventory.HangingSignEditScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={HangingSignEditScreen.class})
public abstract class HangingSignEditScreenMixin
extends AbstractSignEditScreen {
    private HangingSignEditScreenMixin(SignBlockEntity blockEntity, boolean filtered, boolean bl) {
        super(blockEntity, filtered, bl);
    }

    @WrapOperation(method={"<init>"}, at={@At(value="INVOKE", target="Lnet/minecraft/resources/Identifier;withDefaultNamespace(Ljava/lang/String;)Lnet/minecraft/resources/Identifier;")})
    private Identifier init(String id, Operation<Identifier> original) {
        if (this.woodType.name().indexOf(58) != -1) {
            Identifier identifier = Identifier.parse(this.woodType.name());
            return identifier.withPath(path -> "textures/gui/hanging_signs/" + path + ".png");
        }
        return original.call(id);
    }
}

