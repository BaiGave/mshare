/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.particle;

import it.unimi.dsi.fastutil.objects.Reference2IntLinkedOpenHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import net.fabricmc.fabric.api.client.particle.v1.ParticleGroupRegistry;
import net.fabricmc.fabric.impl.base.toposort.NodeSorting;
import net.fabricmc.fabric.impl.base.toposort.SortableNode;
import net.fabricmc.fabric.mixin.client.particle.ParticleEngineAccessor;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.VisibleForTesting;
import org.jspecify.annotations.Nullable;

public final class ParticleGroupRegistryImpl {
    public static final ParticleGroupRegistryImpl INSTANCE = new ParticleGroupRegistryImpl(ParticleEngineAccessor.getParticleRenderTypes());
    private final List<ParticleRenderType> renderTypes;
    private final Map<Identifier, ParticleTextureNode> nodes = new HashMap<Identifier, ParticleTextureNode>();
    private final IdentityHashMap<ParticleRenderType, Function<ParticleEngine, ParticleGroup<?>>> factories = new IdentityHashMap();

    @VisibleForTesting
    public ParticleGroupRegistryImpl(List<ParticleRenderType> renderTypes) {
        ArrayList<ParticleRenderType> copyOfRenderTypes = new ArrayList<ParticleRenderType>(renderTypes);
        this.renderTypes = renderTypes;
        Identifier last = null;
        for (ParticleRenderType renderType : this.renderTypes) {
            Identifier id = ParticleGroupRegistry.getId(renderType);
            this.nodes.put(id, new ParticleTextureNode(renderType));
            if (last != null) {
                ParticleTextureNode.link(this.nodes.get(last), this.nodes.get(id));
            }
            last = id;
        }
        this.sort();
        ParticleGroupRegistryImpl.assertIdentical(renderTypes, copyOfRenderTypes);
    }

    public void register(ParticleRenderType renderType, Function<ParticleEngine, ParticleGroup<?>> function) {
        Identifier id = ParticleGroupRegistry.getId(renderType);
        if (this.nodes.containsKey(id)) {
            throw new IllegalArgumentException("A ParticleRenderType with the id " + String.valueOf(id) + " has already been registered.");
        }
        if (this.factories.containsKey(renderType)) {
            throw new IllegalArgumentException("The specified ParticleRenderType instance has already been registered.");
        }
        ParticleTextureNode node = new ParticleTextureNode(id, renderType);
        this.nodes.put(id, node);
        this.renderTypes.add(renderType);
        this.factories.put(renderType, function);
        this.sort();
    }

    public void registerOrdering(Identifier first, Identifier second) {
        Objects.requireNonNull(first);
        Objects.requireNonNull(second);
        ParticleTextureNode firstEntry = this.nodes.get(first);
        ParticleTextureNode secondEntry = this.nodes.get(second);
        if (firstEntry == null) {
            throw new IllegalArgumentException("The specified first id " + String.valueOf(first) + " does not correspond to a registered ParticleRenderType.");
        }
        if (secondEntry == null) {
            throw new IllegalArgumentException("The specified second id " + String.valueOf(second) + " does not correspond to a registered ParticleRenderType.");
        }
        ParticleTextureNode.link(firstEntry, secondEntry);
        this.sort();
    }

    public @Nullable ParticleRenderType getParticleRenderType(Identifier id) {
        Objects.requireNonNull(id);
        ParticleTextureNode entry = this.nodes.get(id);
        return entry != null ? entry.renderType : null;
    }

    public @Nullable Function<ParticleEngine, ParticleGroup<?>> getFactory(ParticleRenderType renderType) {
        return this.factories.get(renderType);
    }

    private void sort() {
        ArrayList<ParticleTextureNode> entries = new ArrayList<ParticleTextureNode>(this.nodes.values());
        NodeSorting.sort(entries, "particle texture sheets", Comparator.comparing(a -> a.id));
        Reference2IntLinkedOpenHashMap<ParticleRenderType> sheets = new Reference2IntLinkedOpenHashMap<ParticleRenderType>();
        for (int i = 0; i < entries.size(); ++i) {
            sheets.put(((ParticleTextureNode)entries.get((int)i)).renderType, i);
        }
        this.renderTypes.sort(Comparator.comparingInt(sheets::getInt));
    }

    private static void assertIdentical(List<?> a, List<?> b) {
        if (a.size() != b.size()) {
            throw new AssertionError((Object)("Lists differ in size: " + a.size() + " != " + b.size()));
        }
        for (int i = 0; i < a.size(); ++i) {
            if (a.get(i) != b.get(i)) {
                throw new AssertionError((Object)("Lists differ at index " + i + ": " + String.valueOf(a.get(i)) + " != " + String.valueOf(b.get(i))));
            }
        }
    }

    private static class ParticleTextureNode
    extends SortableNode<ParticleTextureNode> {
        final Identifier id;
        final ParticleRenderType renderType;

        private ParticleTextureNode(Identifier id, ParticleRenderType renderType) {
            this.id = id;
            this.renderType = renderType;
        }

        private ParticleTextureNode(ParticleRenderType renderType) {
            this.id = ParticleGroupRegistry.getId(renderType);
            this.renderType = renderType;
        }

        @Override
        protected String getDescription() {
            return this.id.toString();
        }
    }
}

