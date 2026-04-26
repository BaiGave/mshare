/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.lookup.v1.entity;

import java.util.function.BiFunction;
import net.fabricmc.fabric.impl.lookup.entity.EntityApiLookupImpl;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.NonExtendable
public interface EntityApiLookup<A, C> {
    public static <A, C> EntityApiLookup<A, C> get(Identifier lookupId, Class<A> apiClass, Class<C> contextClass) {
        return EntityApiLookupImpl.get(lookupId, apiClass, contextClass);
    }

    public @Nullable A find(Entity var1, C var2);

    public void registerSelf(EntityType<?> ... var1);

    default public <T extends Entity> void registerForType(BiFunction<T, C, @Nullable A> provider, EntityType<T> entityType) {
        this.registerForTypes((entity, context) -> provider.apply(entity, context), entityType);
    }

    public void registerForTypes(EntityApiProvider<A, C> var1, EntityType<?> ... var2);

    public void registerFallback(EntityApiProvider<A, C> var1);

    public Identifier getId();

    public Class<A> apiClass();

    public Class<C> contextClass();

    public @Nullable EntityApiProvider<A, C> getProvider(EntityType<?> var1);

    public static interface EntityApiProvider<A, C> {
        public @Nullable A find(Entity var1, C var2);
    }
}

