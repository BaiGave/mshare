/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.datagen.v1;

import java.nio.file.Path;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.ApiStatus;

public final class FabricPackOutput
extends PackOutput {
    private final ModContainer modContainer;
    private final boolean strictValidation;

    @ApiStatus.Internal
    public FabricPackOutput(ModContainer modContainer, Path outputFolder, boolean strictValidation) {
        super(outputFolder);
        this.modContainer = modContainer;
        this.strictValidation = strictValidation;
    }

    public ModContainer getModContainer() {
        return this.modContainer;
    }

    public String getModId() {
        return this.getModContainer().getMetadata().getId();
    }

    public boolean isStrictValidationEnabled() {
        return this.strictValidation;
    }
}

