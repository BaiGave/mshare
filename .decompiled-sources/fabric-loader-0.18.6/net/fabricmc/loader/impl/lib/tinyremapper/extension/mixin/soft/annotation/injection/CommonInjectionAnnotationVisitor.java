/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.soft.annotation.injection;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.fabricmc.loader.impl.lib.tinyremapper.api.TrClass;
import net.fabricmc.loader.impl.lib.tinyremapper.api.TrMember;
import net.fabricmc.loader.impl.lib.tinyremapper.api.TrMethod;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.common.ResolveUtility;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.common.data.CommonData;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.common.data.Pair;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.soft.annotation.injection.AtAnnotationVisitor;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.soft.annotation.injection.DescAnnotationVisitor;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.soft.annotation.injection.SliceAnnotationVisitor;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.soft.data.MemberInfo;
import org.objectweb.asm.AnnotationVisitor;

class CommonInjectionAnnotationVisitor
extends AnnotationVisitor {
    protected final CommonData data;
    protected final List<String> targets;

    CommonInjectionAnnotationVisitor(CommonData data, AnnotationVisitor delegate, List<String> targets) {
        super(589824, Objects.requireNonNull(delegate));
        this.data = Objects.requireNonNull(data);
        this.targets = Objects.requireNonNull(targets);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        AnnotationVisitor av = super.visitAnnotation(name, descriptor);
        if (name.equals("at")) {
            if (!descriptor.equals("Lorg/spongepowered/asm/mixin/injection/At;")) {
                throw new RuntimeException("Unexpected annotation " + descriptor);
            }
            av = new AtAnnotationVisitor(this.data, av, this.targets);
        } else if (name.equals("slice")) {
            if (!descriptor.equals("Lorg/spongepowered/asm/mixin/injection/Slice;")) {
                throw new RuntimeException("Unexpected annotation " + descriptor);
            }
            av = new SliceAnnotationVisitor(this.data, av, this.targets);
        }
        return av;
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        AnnotationVisitor av = super.visitArray(name);
        if (name.equals("method")) {
            return new AnnotationVisitor(589824, av){

                @Override
                public void visit(String name, Object value) {
                    Optional<MemberInfo> info = Optional.ofNullable(MemberInfo.parse(Objects.requireNonNull((String)value).replaceAll("\\s", "")));
                    value = info.map(i -> new InjectMethodMappable(CommonInjectionAnnotationVisitor.this.data, (MemberInfo)i, CommonInjectionAnnotationVisitor.this.targets).result().toString()).orElse((String)value);
                    super.visit(name, value);
                }
            };
        }
        if (name.equals("target")) {
            return new AnnotationVisitor(589824, av){

                @Override
                public AnnotationVisitor visitAnnotation(String name, String descriptor) {
                    if (!descriptor.equals("Lorg/spongepowered/asm/mixin/injection/Desc;")) {
                        throw new RuntimeException("Unexpected annotation " + descriptor);
                    }
                    AnnotationVisitor av1 = super.visitAnnotation(name, descriptor);
                    return new DescAnnotationVisitor(CommonInjectionAnnotationVisitor.this.targets, CommonInjectionAnnotationVisitor.this.data, av1, TrMember.MemberType.METHOD);
                }
            };
        }
        if (name.equals("at")) {
            return new AnnotationVisitor(589824, av){

                @Override
                public AnnotationVisitor visitAnnotation(String name, String descriptor) {
                    if (!descriptor.equals("Lorg/spongepowered/asm/mixin/injection/At;")) {
                        throw new RuntimeException("Unexpected annotation " + descriptor);
                    }
                    AnnotationVisitor av1 = super.visitAnnotation(name, descriptor);
                    return new AtAnnotationVisitor(CommonInjectionAnnotationVisitor.this.data, av1, CommonInjectionAnnotationVisitor.this.targets);
                }
            };
        }
        if (name.equals("slice")) {
            return new AnnotationVisitor(589824, av){

                @Override
                public AnnotationVisitor visitAnnotation(String name, String descriptor) {
                    if (!descriptor.equals("Lorg/spongepowered/asm/mixin/injection/Slice;")) {
                        throw new RuntimeException("Unexpected annotation " + descriptor);
                    }
                    AnnotationVisitor av1 = super.visitAnnotation(name, descriptor);
                    return new SliceAnnotationVisitor(CommonInjectionAnnotationVisitor.this.data, av1, CommonInjectionAnnotationVisitor.this.targets);
                }
            };
        }
        return av;
    }

    private static class InjectMethodMappable {
        private final CommonData data;
        private final MemberInfo info;
        private final List<TrClass> targets;

        InjectMethodMappable(CommonData data, MemberInfo info, List<String> targets) {
            this.data = Objects.requireNonNull(data);
            this.info = Objects.requireNonNull(info);
            this.targets = info.getOwner().isEmpty() ? Objects.requireNonNull(targets).stream().map(data.resolver::resolveClass).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList()) : data.resolver.resolveClass(info.getOwner()).map(Collections::singletonList).orElse(Collections.emptyList());
        }

        private Optional<TrMember> resolvePartial(TrClass owner, String name, String desc) {
            Objects.requireNonNull(owner);
            name = name.isEmpty() ? null : name;
            desc = desc.isEmpty() ? null : desc;
            return this.data.resolver.resolveMethod(owner, name, desc, ResolveUtility.FLAG_FIRST | ResolveUtility.FLAG_NON_SYN).map(m -> m);
        }

        public MemberInfo result() {
            if (this.info.getOwner().isEmpty() && this.info.getName().isEmpty() && this.info.getQuantifier().equals("*") && !this.info.getDesc().isEmpty()) {
                return new MemberInfo(this.info.getOwner(), this.info.getName(), this.info.getQuantifier(), this.data.mapper.asTrRemapper().mapDesc(this.info.getDesc()));
            }
            if (this.targets.isEmpty() || this.info.getName().isEmpty()) {
                return this.info;
            }
            List collection = this.targets.stream().map(target -> this.resolvePartial((TrClass)target, this.info.getName(), this.info.getDesc())).filter(Optional::isPresent).map(Optional::get).map(m -> {
                String mappedName = this.data.mapper.mapName((TrMember)m);
                boolean shouldPassDesc = false;
                for (TrMethod trMethod : m.getOwner().getMethods()) {
                    if (trMethod == m || !this.data.mapper.mapName(trMethod).equals(mappedName)) continue;
                    shouldPassDesc = true;
                }
                return Pair.of(mappedName, shouldPassDesc ? this.data.mapper.mapDesc((TrMember)m) : "");
            }).distinct().collect(Collectors.toList());
            if (collection.size() > 1) {
                this.data.getLogger().error("Conflict mapping detected, %s -> %s.", this.info.getName(), collection);
            } else if (collection.isEmpty()) {
                this.data.getLogger().warn("Cannot remap %s because it does not exist in any of the targets %s", this.info.getName(), this.targets);
            }
            return collection.stream().findFirst().map(pair -> new MemberInfo(this.data.mapper.asTrRemapper().map(this.info.getOwner()), (String)pair.first(), this.info.getQuantifier(), this.info.getQuantifier().equals("*") ? "" : (String)pair.second())).orElse(this.info);
        }
    }
}

