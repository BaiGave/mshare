/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.soft.annotation.injection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.fabricmc.loader.impl.lib.tinyremapper.api.TrClass;
import net.fabricmc.loader.impl.lib.tinyremapper.api.TrLocal;
import net.fabricmc.loader.impl.lib.tinyremapper.api.TrMethod;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.common.ResolveUtility;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.common.data.CommonData;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.soft.annotation.injection.CommonInjectionAnnotationVisitor;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.soft.data.MemberInfo;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.tree.AnnotationNode;

public class ModifyVariableAnnotationVisitor
extends AnnotationNode {
    private final CommonData data;
    private final AnnotationVisitor delegate;
    private final List<String> targets;
    private final List<MemberInfo> methods = new ArrayList<MemberInfo>();

    public ModifyVariableAnnotationVisitor(CommonData data, AnnotationVisitor delegate, List<String> targets) {
        super(589824, "Lorg/spongepowered/asm/mixin/injection/ModifyVariable;");
        this.data = Objects.requireNonNull(data);
        this.delegate = Objects.requireNonNull(delegate);
        this.targets = Objects.requireNonNull(targets);
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        AnnotationVisitor av = super.visitArray(name);
        if (name.equals("method")) {
            return new AnnotationVisitor(589824, av){

                @Override
                public void visit(String name, Object value) {
                    MemberInfo info = MemberInfo.parse(Objects.requireNonNull((String)value).replaceAll("\\s", ""));
                    if (info != null && (info.getOwner().isEmpty() || ModifyVariableAnnotationVisitor.this.targets.contains(info.getOwner()))) {
                        ModifyVariableAnnotationVisitor.this.methods.add(info);
                    }
                    super.visit(name, value);
                }
            };
        }
        return av;
    }

    @Override
    public void visitEnd() {
        this.accept(new ModifyVariableSecondPassAnnotationVisitor(this.data, this.delegate, this.targets, this.methods));
        super.visitEnd();
    }

    private static class ModifyVariableSecondPassAnnotationVisitor
    extends CommonInjectionAnnotationVisitor {
        private final List<MemberInfo> methods;
        private final List<TrClass> targets;

        ModifyVariableSecondPassAnnotationVisitor(CommonData data, AnnotationVisitor delegate, List<String> targets, List<MemberInfo> methods) {
            super(data, delegate, targets);
            this.methods = methods;
            this.targets = Objects.requireNonNull(targets).stream().map(data.resolver::resolveClass).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            AnnotationVisitor av = super.visitArray(name);
            if (name.equals("name")) {
                return new AnnotationVisitor(589824, av){

                    @Override
                    public void visit(String name, Object value) {
                        String localName = Objects.requireNonNull((String)value).replaceAll("\\s", "");
                        List collection = targets.stream().flatMap(target -> methods.stream().map(info -> this.resolvePartial(target, info.getName(), info.getDesc()))).filter(Optional::isPresent).map(Optional::get).map(m -> {
                            TrLocal[] localVariables = m.getLocals();
                            if (localVariables == null || localVariables.length == 0) {
                                return localName;
                            }
                            HashMap<String, Integer> lvtName2Index = new HashMap<String, Integer>();
                            for (TrLocal variable : localVariables) {
                                if (!lvtName2Index.containsKey(variable.getName())) {
                                    lvtName2Index.put(variable.getName(), variable.getIndex());
                                    continue;
                                }
                                lvtName2Index.put(variable.getName(), -1);
                            }
                            if (!lvtName2Index.containsKey(localName)) {
                                return localName;
                            }
                            int lvIndex = (Integer)lvtName2Index.get(localName);
                            if (lvIndex < 0) {
                                return localName;
                            }
                            return data.mapper.asTrRemapper().mapMethodArg(m.getOwner().getName(), m.getName(), m.getDesc(), lvIndex, localName);
                        }).distinct().collect(Collectors.toList());
                        if (collection.size() > 1) {
                            data.getLogger().error("Conflict mapping detected, %s -> %s.", localName, collection);
                        } else if (collection.isEmpty()) {
                            data.getLogger().warn("Cannot remap %s because it does not exist in any of the targets %s", localName, targets);
                        }
                        super.visit(name, collection.stream().findFirst().orElse(localName));
                    }
                };
            }
            return av;
        }

        private Optional<TrMethod> resolvePartial(TrClass owner, String name, String desc) {
            Objects.requireNonNull(owner);
            name = name.isEmpty() ? null : name;
            desc = desc.isEmpty() ? null : desc;
            return this.data.resolver.resolveMethod(owner, name, desc, ResolveUtility.FLAG_FIRST | ResolveUtility.FLAG_NON_SYN).map(m -> m);
        }
    }
}

