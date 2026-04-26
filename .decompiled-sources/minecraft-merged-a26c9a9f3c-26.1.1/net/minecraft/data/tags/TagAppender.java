/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.tags;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;

public interface TagAppender<E, T>
extends FabricTagAppender<E, T> {
    public TagAppender<E, T> add(E var1);

    default public TagAppender<E, T> add(E ... elements) {
        return this.addAll(Arrays.stream(elements));
    }

    default public TagAppender<E, T> addAll(Collection<E> elements) {
        elements.forEach(this::add);
        return this;
    }

    default public TagAppender<E, T> addAll(Stream<E> elements) {
        elements.forEach(this::add);
        return this;
    }

    public TagAppender<E, T> addOptional(E var1);

    public TagAppender<E, T> addTag(TagKey<T> var1);

    public TagAppender<E, T> addOptionalTag(TagKey<T> var1);

    public static <T> TagAppender<ResourceKey<T>, T> forBuilder(final TagBuilder builder) {
        return new TagAppender<ResourceKey<T>, T>(){

            @Override
            public TagAppender<ResourceKey<T>, T> add(ResourceKey<T> element) {
                builder.addElement(element.identifier());
                return this;
            }

            @Override
            public TagAppender<ResourceKey<T>, T> addOptional(ResourceKey<T> element) {
                builder.addOptionalElement(element.identifier());
                return this;
            }

            @Override
            public TagAppender<ResourceKey<T>, T> addTag(TagKey<T> tag) {
                builder.addTag(tag.location());
                return this;
            }

            @Override
            public TagAppender<ResourceKey<T>, T> addOptionalTag(TagKey<T> tag) {
                builder.addOptionalTag(tag.location());
                return this;
            }
        };
    }

    default public <U> TagAppender<U, T> map(final Function<U, E> converter) {
        final TagAppender original = this;
        return new TagAppender<U, T>(this){
            {
                Objects.requireNonNull(this$0);
            }

            @Override
            public TagAppender<U, T> add(U element) {
                original.add(converter.apply(element));
                return this;
            }

            @Override
            public TagAppender<U, T> addOptional(U element) {
                original.add(converter.apply(element));
                return this;
            }

            @Override
            public TagAppender<U, T> addTag(TagKey<T> tag) {
                original.addTag(tag);
                return this;
            }

            @Override
            public TagAppender<U, T> addOptionalTag(TagKey<T> tag) {
                original.addOptionalTag(tag);
                return this;
            }
        };
    }
}

