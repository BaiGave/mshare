/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.datagen.v1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

public final class FabricDataGenerator
extends DataGenerator.Uncached {
    private final ModContainer modContainer;
    private final boolean strictValidation;
    private final FabricPackOutput fabricOutput;
    private final CompletableFuture<HolderLookup.Provider> registriesFuture;

    @ApiStatus.Internal
    public FabricDataGenerator(Path output, ModContainer mod, boolean strictValidation, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output);
        this.modContainer = Objects.requireNonNull(mod);
        this.strictValidation = strictValidation;
        this.fabricOutput = new FabricPackOutput(mod, output, strictValidation);
        this.registriesFuture = registriesFuture;
    }

    public Pack createPack() {
        return new Pack(this, true, this.modContainer.getMetadata().getName(), this.fabricOutput);
    }

    public Pack createBuiltinResourcePack(Identifier id) {
        Path path = this.vanillaPackOutput.getOutputFolder().resolve("resourcepacks").resolve(id.getPath());
        return new Pack(this, true, id.toString(), new FabricPackOutput(this.modContainer, path, this.strictValidation));
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

    public CompletableFuture<HolderLookup.Provider> getRegistries() {
        return this.registriesFuture;
    }

    @Override
    @Deprecated
    public DataGenerator.PackGenerator getVanillaPack(boolean shouldRun) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public DataGenerator.PackGenerator getBuiltinDatapack(boolean shouldRun, String packName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void run() throws IOException {
        Path output = this.vanillaPackOutput.getOutputFolder();
        if (Files.exists(output, new LinkOption[0])) {
            FabricDataGenHelper.deleteDirectory(output);
        }
        super.run();
    }

    public final class Pack
    extends DataGenerator.PackGenerator {
        final /* synthetic */ FabricDataGenerator this$0;

        private Pack(FabricDataGenerator this$0, boolean shouldRun, String name, FabricPackOutput output) {
            FabricDataGenerator fabricDataGenerator = this$0;
            Objects.requireNonNull(fabricDataGenerator);
            this.this$0 = fabricDataGenerator;
            super(this$0, shouldRun, name, output);
        }

        public <T extends DataProvider> T addProvider(Factory<T> factory) {
            return (T)super.addProvider((PackOutput output) -> factory.create((FabricPackOutput)output));
        }

        public <T extends DataProvider> T addProvider(RegistryDependentFactory<T> factory) {
            return (T)super.addProvider((PackOutput output) -> factory.create((FabricPackOutput)output, this.this$0.registriesFuture));
        }

        @FunctionalInterface
        public static interface Factory<T extends DataProvider> {
            public T create(FabricPackOutput var1);
        }

        @FunctionalInterface
        public static interface RegistryDependentFactory<T extends DataProvider> {
            public T create(FabricPackOutput var1, CompletableFuture<HolderLookup.Provider> var2);
        }
    }
}

