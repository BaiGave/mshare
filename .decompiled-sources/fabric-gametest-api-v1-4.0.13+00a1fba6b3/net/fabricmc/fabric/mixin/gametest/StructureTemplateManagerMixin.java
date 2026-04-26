/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.gametest;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixer;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.fabricmc.fabric.impl.gametest.FabricGameTestRunner;
import net.minecraft.core.HolderGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.loader.TemplateSource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={StructureTemplateManager.class})
public abstract class StructureTemplateManagerMixin {
    @Inject(method={"<init>"}, at={@At(value="INVOKE", target="Lcom/google/common/collect/ImmutableList$Builder;add(Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList$Builder;", ordinal=2, shift=At.Shift.AFTER)})
    private void addFabricTemplateProvider(final ResourceManager resourceManager, LevelStorageSource.LevelStorageAccess storageAccess, final DataFixer dataFixer, final HolderGetter<Block> blockLookup, CallbackInfo ci, @Local(name={"sources"}) ImmutableList.Builder<TemplateSource> builder) {
        builder.add((Object)new TemplateSource(this, dataFixer, blockLookup){
            {
                Objects.requireNonNull(this$0);
                super(arg0, arg1);
            }

            @Override
            public Optional<StructureTemplate> load(Identifier id) {
                Identifier path = FabricGameTestRunner.GAMETEST_STRUCTURE_FINDER.idToFile(id);
                Optional<Resource> resource = resourceManager.getResource(path);
                if (resource.isPresent()) {
                    try {
                        String snbt = IOUtils.toString(resource.get().openAsReader());
                        CompoundTag tag = NbtUtils.snbtToStructure(snbt);
                        StructureTemplate structureTemplate = new StructureTemplate();
                        int version = NbtUtils.getDataVersion(tag, 500);
                        structureTemplate.load(blockLookup, DataFixTypes.STRUCTURE.updateToCurrentVersion(dataFixer, tag, version));
                        return Optional.of(structureTemplate);
                    }
                    catch (CommandSyntaxException | IOException e) {
                        throw new RuntimeException("Failed to load GameTest structure " + String.valueOf(id), e);
                    }
                }
                return Optional.empty();
            }

            @Override
            public Stream<Identifier> list() {
                FileToIdConverter finder = FabricGameTestRunner.GAMETEST_STRUCTURE_FINDER;
                return finder.listMatchingResources(resourceManager).keySet().stream().map(finder::fileToId);
            }
        });
    }
}

