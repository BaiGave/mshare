/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.LootContextUser;

public class ValidationContext {
    private final ProblemReporter reporter;
    private final ContextKeySet contextKeySet;
    private final Optional<HolderGetter.Provider> resolver;
    private final Set<ResourceKey<?>> visitedElements;

    public ValidationContext(ProblemReporter reporter, ContextKeySet contextKeySet, HolderGetter.Provider resolver) {
        this(reporter, contextKeySet, Optional.of(resolver), Set.of());
    }

    public ValidationContext(ProblemReporter reporter, ContextKeySet contextKeySet) {
        this(reporter, contextKeySet, Optional.empty(), Set.of());
    }

    private ValidationContext(ProblemReporter reporter, ContextKeySet contextKeySet, Optional<HolderGetter.Provider> resolver, Set<ResourceKey<?>> visitedElements) {
        this.reporter = reporter;
        this.contextKeySet = contextKeySet;
        this.resolver = resolver;
        this.visitedElements = visitedElements;
    }

    public ValidationContext forChild(ProblemReporter.PathElement subContext) {
        return new ValidationContext(this.reporter.forChild(subContext), this.contextKeySet, this.resolver, this.visitedElements);
    }

    public ValidationContext forField(String name) {
        return this.forChild(new ProblemReporter.FieldPathElement(name));
    }

    public ValidationContext forIndexedField(String name, int index) {
        return this.forChild(new ProblemReporter.IndexedFieldPathElement(name, index));
    }

    public ValidationContext forMapField(String name, String key) {
        return this.forChild(new ProblemReporter.MapEntryPathElement(name, key));
    }

    public ValidationContext enterElement(ProblemReporter.PathElement subContext, ResourceKey<?> element) {
        ImmutableCollection newVisitedElements = ((ImmutableSet.Builder)((ImmutableSet.Builder)ImmutableSet.builder().addAll(this.visitedElements)).add(element)).build();
        return new ValidationContext(this.reporter.forChild(subContext), this.contextKeySet, this.resolver, (Set<ResourceKey<?>>)((Object)newVisitedElements));
    }

    public boolean hasVisitedElement(ResourceKey<?> element) {
        return this.visitedElements.contains(element);
    }

    public void reportProblem(ProblemReporter.Problem description) {
        this.reporter.report(description);
    }

    public void validateContextUsage(LootContextUser lootContextUser) {
        Set<ContextKey<?>> allReferenced = lootContextUser.getReferencedContextParams();
        Sets.SetView<ContextKey<?>> notProvided = Sets.difference(allReferenced, this.contextKeySet.allowed());
        if (!notProvided.isEmpty()) {
            this.reporter.report(new ParametersNotProvidedProblem(notProvided));
        }
    }

    public HolderGetter.Provider resolver() {
        return this.resolver.orElseThrow(() -> new UnsupportedOperationException("References not allowed"));
    }

    public boolean allowsReferences() {
        return this.resolver.isPresent();
    }

    public ProblemReporter reporter() {
        return this.reporter;
    }

    public record ParametersNotProvidedProblem(Set<ContextKey<?>> notProvided) implements ProblemReporter.Problem
    {
        @Override
        public String description() {
            return "Parameters " + String.valueOf(this.notProvided) + " are not provided in this context";
        }
    }

    public record MissingReferenceProblem(ResourceKey<?> referenced) implements ProblemReporter.Problem
    {
        @Override
        public String description() {
            return "Missing element " + String.valueOf(this.referenced.identifier()) + " of type " + String.valueOf(this.referenced.registry());
        }
    }

    public record RecursiveReferenceProblem(ResourceKey<?> referenced) implements ProblemReporter.Problem
    {
        @Override
        public String description() {
            return String.valueOf(this.referenced.identifier()) + " of type " + String.valueOf(this.referenced.registry()) + " is recursively called";
        }
    }

    public record ReferenceNotAllowedProblem(ResourceKey<?> referenced) implements ProblemReporter.Problem
    {
        @Override
        public String description() {
            return "Reference to " + String.valueOf(this.referenced.identifier()) + " of type " + String.valueOf(this.referenced.registry()) + " was used, but references are not allowed";
        }
    }
}

