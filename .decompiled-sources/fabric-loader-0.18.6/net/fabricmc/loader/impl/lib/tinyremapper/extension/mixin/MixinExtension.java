/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.fabricmc.loader.impl.lib.tinyremapper.InputTag;
import net.fabricmc.loader.impl.lib.tinyremapper.TinyRemapper;
import net.fabricmc.loader.impl.lib.tinyremapper.api.TrClass;
import net.fabricmc.loader.impl.lib.tinyremapper.api.TrEnvironment;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.common.data.CommonData;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.hard.HardTargetMixinClassVisitor;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.soft.SoftTargetMixinClassVisitor;
import org.objectweb.asm.ClassVisitor;

public class MixinExtension
implements TinyRemapper.Extension {
    private final Map<Integer, Collection<Consumer<CommonData>>> tasks = new ConcurrentHashMap<Integer, Collection<Consumer<CommonData>>>();
    private final Set<AnnotationTarget> targets;
    private final Predicate<InputTag> inputTagFilter;

    public MixinExtension() {
        this(EnumSet.allOf(AnnotationTarget.class));
    }

    public MixinExtension(Predicate<InputTag> inputTagFilter) {
        this(EnumSet.allOf(AnnotationTarget.class), inputTagFilter);
    }

    public MixinExtension(Set<AnnotationTarget> targets) {
        this(targets, null);
    }

    public MixinExtension(Set<AnnotationTarget> targets, Predicate<InputTag> inputTagFilter) {
        this.targets = targets;
        this.inputTagFilter = inputTagFilter;
    }

    @Override
    public void attach(TinyRemapper.Builder builder) {
        if (this.targets.contains((Object)AnnotationTarget.HARD)) {
            builder.extraAnalyzeVisitor(new AnalyzeVisitorProvider()).extraStateProcessor(this::stateProcessor);
        }
        if (this.targets.contains((Object)AnnotationTarget.SOFT)) {
            builder.extraPreApplyVisitor(new PreApplyVisitorProvider());
        }
    }

    private void stateProcessor(TrEnvironment environment) {
        CommonData data = new CommonData(environment);
        for (Consumer task : (Collection)this.tasks.getOrDefault(environment.getMrjVersion(), Collections.emptyList())) {
            try {
                task.accept(data);
            }
            catch (RuntimeException e) {
                environment.getLogger().error(e.getMessage());
            }
        }
    }

    public static enum AnnotationTarget {
        SOFT,
        HARD;

    }

    private final class AnalyzeVisitorProvider
    implements TinyRemapper.AnalyzeVisitorProvider {
        private AnalyzeVisitorProvider() {
        }

        @Override
        public ClassVisitor insertAnalyzeVisitor(int mrjVersion, String className, ClassVisitor next) {
            return new HardTargetMixinClassVisitor(MixinExtension.this.tasks.computeIfAbsent(mrjVersion, k -> new ConcurrentLinkedQueue()), next);
        }

        @Override
        public ClassVisitor insertAnalyzeVisitor(int mrjVersion, String className, ClassVisitor next, InputTag[] inputTags) {
            if (MixinExtension.this.inputTagFilter == null || inputTags == null) {
                return this.insertAnalyzeVisitor(mrjVersion, className, next);
            }
            for (InputTag tag : inputTags) {
                if (!MixinExtension.this.inputTagFilter.test(tag)) continue;
                return this.insertAnalyzeVisitor(mrjVersion, className, next);
            }
            return next;
        }

        @Override
        public ClassVisitor insertAnalyzeVisitor(boolean isInput, int mrjVersion, String className, ClassVisitor next, InputTag[] inputTags) {
            if (!isInput) {
                return next;
            }
            return this.insertAnalyzeVisitor(mrjVersion, className, next, inputTags);
        }
    }

    private final class PreApplyVisitorProvider
    implements TinyRemapper.ApplyVisitorProvider {
        private PreApplyVisitorProvider() {
        }

        @Override
        public ClassVisitor insertApplyVisitor(TrClass cls, ClassVisitor next) {
            return new SoftTargetMixinClassVisitor(new CommonData(cls.getEnvironment()), next);
        }

        @Override
        public ClassVisitor insertApplyVisitor(TrClass cls, ClassVisitor next, InputTag[] inputTags) {
            if (!cls.isInput()) {
                return next;
            }
            if (MixinExtension.this.inputTagFilter == null || inputTags == null) {
                return this.insertApplyVisitor(cls, next);
            }
            for (InputTag tag : inputTags) {
                if (!MixinExtension.this.inputTagFilter.test(tag)) continue;
                return this.insertApplyVisitor(cls, next);
            }
            return next;
        }
    }
}

