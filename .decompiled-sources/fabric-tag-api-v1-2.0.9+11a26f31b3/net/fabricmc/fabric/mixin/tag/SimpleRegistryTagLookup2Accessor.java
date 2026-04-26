/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.tag;

import java.util.Map;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets={"net.minecraft.core.MappedRegistry$TagSet$2"})
public interface SimpleRegistryTagLookup2Accessor<T> {
    @Accessor(value="val$tags")
    public Map<TagKey<T>, HolderSet.Named<T>> fabric_getTagMap();

    @Accessor(value="val$tags")
    @Mutable
    public void fabric_setTagMap(Map<TagKey<T>, HolderSet.Named<T>> var1);
}

