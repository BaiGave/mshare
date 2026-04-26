/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.attachment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.util.filefix.fixes.DimensionStorageFileFix;
import net.minecraft.util.filefix.operations.FileFixOperation;
import net.minecraft.util.filefix.operations.FileFixOperations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value={DimensionStorageFileFix.class})
abstract class DimensionStorageFileFixMixin {
    DimensionStorageFileFixMixin() {
    }

    @ModifyArg(method={"makeFixer"}, at=@At(value="INVOKE", target="Lnet/minecraft/util/filefix/operations/FileFixOperations;applyInFolders(Lnet/minecraft/util/filefix/access/FileRelation;Ljava/util/List;)Lnet/minecraft/util/filefix/operations/ApplyInFolders;", ordinal=1), index=1)
    private List<FileFixOperation> addFabricAttachmentsMigration(List<FileFixOperation> original) {
        ArrayList<FileFixOperation> operations = new ArrayList<FileFixOperation>(original);
        operations.add(FileFixOperations.move("fabric_attachments.dat", "fabric/attachments.dat"));
        return Collections.unmodifiableList(operations);
    }
}

