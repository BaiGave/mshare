/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.datagen;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagAppender;
import net.fabricmc.fabric.impl.datagen.FabricTagBuilder;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={TagAppender.class})
interface TagAppenderMixin<E, T>
extends FabricTagAppender<E, T> {

    @Mixin(targets={"net.minecraft.data.tags.TagAppender$2"})
    public static abstract class TagAppender2Mixin<E, T>
    implements TagAppenderMixin<E, T> {
        @Shadow
        @Final
        TagAppender val$original;

        @Override
        public TagAppender<E, T> setReplace(boolean replace) {
            this.val$original.setReplace(replace);
            return (TagAppender)((Object)this);
        }

        @Override
        public TagAppender<E, T> forceAddTag(TagKey<T> tag) {
            this.val$original.forceAddTag(tag);
            return (TagAppender)((Object)this);
        }

        @WrapOperation(method={"addOptional"}, at={@At(value="INVOKE", target="Lnet/minecraft/data/tags/TagAppender;add(Ljava/lang/Object;)Lnet/minecraft/data/tags/TagAppender;")})
        private TagAppender<E, T> fixAddOptional(TagAppender instance, E e, Operation<TagAppender<E, T>> original) {
            return instance.addOptional(e);
        }
    }

    @Mixin(targets={"net.minecraft.data.tags.TagAppender$1"})
    public static abstract class TagAppender1Mixin<E, T>
    implements TagAppenderMixin<E, T> {
        @Shadow
        @Final
        TagBuilder val$builder;

        @Override
        public TagAppender<E, T> setReplace(boolean replace) {
            ((FabricTagBuilder)((Object)this.val$builder)).fabric_setReplace(replace);
            return (TagAppender)((Object)this);
        }

        @Override
        public TagAppender<E, T> forceAddTag(TagKey<T> tag) {
            ((FabricTagBuilder)((Object)this.val$builder)).fabric_forceAddTag(tag.location());
            return (TagAppender)((Object)this);
        }
    }
}

