/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.tinyremapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.lang.model.SourceVersion;
import net.fabricmc.loader.impl.lib.tinyremapper.AsmRemapper;
import net.fabricmc.loader.impl.lib.tinyremapper.PackageAccessChecker;
import net.fabricmc.loader.impl.lib.tinyremapper.TinyRemapper;
import net.fabricmc.loader.impl.lib.tinyremapper.VisitTrackingClassRemapper;
import net.fabricmc.loader.impl.lib.tinyremapper.api.TrMember;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.RecordComponentVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.FieldRemapper;
import org.objectweb.asm.commons.MethodRemapper;
import org.objectweb.asm.commons.RecordComponentRemapper;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;

final class AsmClassRemapper
extends VisitTrackingClassRemapper {
    private final boolean rebuildSourceFilenames;
    private final boolean checkPackageAccess;
    private final boolean skipLocalMapping;
    private final boolean renameInvalidLocals;
    private final Pattern invalidLvNamePattern;
    private final boolean inferNameFromSameLvIndex;
    private boolean sourceNameVisited;
    private MethodNode methodNode;

    AsmClassRemapper(ClassVisitor cv, AsmRemapper remapper, boolean rebuildSourceFilenames, boolean checkPackageAccess, boolean skipLocalMapping, boolean renameInvalidLocals, Pattern invalidLvNamePattern, boolean inferNameFromSameLvIndex) {
        super(cv, remapper);
        this.rebuildSourceFilenames = rebuildSourceFilenames;
        this.checkPackageAccess = checkPackageAccess;
        this.skipLocalMapping = skipLocalMapping;
        this.renameInvalidLocals = renameInvalidLocals;
        this.invalidLvNamePattern = invalidLvNamePattern;
        this.inferNameFromSameLvIndex = inferNameFromSameLvIndex;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if (this.checkPackageAccess) {
            AsmRemapper remapper = (AsmRemapper)this.remapper;
            if (superName != null) {
                PackageAccessChecker.checkClass(name, superName, "super class", remapper);
            }
            if (interfaces != null) {
                for (String iface : interfaces) {
                    PackageAccessChecker.checkClass(name, iface, "super interface", remapper);
                }
            }
        }
        this.sourceNameVisited = false;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitSource(String source, String debug) {
        int start;
        this.sourceNameVisited = true;
        if (!this.rebuildSourceFilenames) {
            super.visitSource(source, debug);
            return;
        }
        String mappedClsName = this.remapper.map(this.className);
        int end = mappedClsName.indexOf(36);
        if (end <= 0) {
            end = mappedClsName.length();
        }
        if (end <= (start = mappedClsName.lastIndexOf(47, end - 1) + 1)) {
            end = mappedClsName.length();
        }
        super.visitSource(mappedClsName.substring(start, end).concat(".java"), debug);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        if (this.checkPackageAccess) {
            PackageAccessChecker.checkDesc(this.className, descriptor, "field descriptor", (AsmRemapper)this.remapper);
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    protected FieldVisitor createFieldRemapper(FieldVisitor fieldVisitor) {
        return new AsmFieldRemapper(fieldVisitor, (AsmRemapper)this.remapper);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (this.checkPackageAccess) {
            PackageAccessChecker.checkDesc(this.className, descriptor, "method descriptor", (AsmRemapper)this.remapper);
        }
        if (!this.skipLocalMapping || this.renameInvalidLocals) {
            this.methodNode = new MethodNode(this.api, access, name, descriptor, signature, exceptions);
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    protected MethodVisitor createMethodRemapper(MethodVisitor methodVisitor) {
        return new AsmMethodRemapper(methodVisitor, (AsmRemapper)this.remapper, this.className, this.methodNode, this.checkPackageAccess, this.skipLocalMapping, this.renameInvalidLocals, this.invalidLvNamePattern, this.inferNameFromSameLvIndex);
    }

    @Override
    protected RecordComponentVisitor createRecordComponentRemapper(RecordComponentVisitor recordComponentVisitor) {
        return new AsmRecordComponentRemapper(recordComponentVisitor, (AsmRemapper)this.remapper);
    }

    @Override
    public AnnotationVisitor createAnnotationRemapper(String descriptor, AnnotationVisitor annotationVisitor) {
        return new AsmAnnotationRemapper(descriptor, annotationVisitor, (AsmRemapper)this.remapper);
    }

    @Override
    public void visitEnd() {
        ((AsmRemapper)this.remapper).finish(this.className, this.cv);
        super.visitEnd();
    }

    @Override
    protected void onVisit(VisitTrackingClassRemapper.VisitKind kind) {
        if (this.rebuildSourceFilenames && !this.sourceNameVisited && kind.ordinal() > VisitTrackingClassRemapper.VisitKind.SOURCE.ordinal()) {
            this.visitSource(null, null);
        }
    }

    static class AsmFieldRemapper
    extends FieldRemapper {
        AsmFieldRemapper(FieldVisitor fieldVisitor, AsmRemapper remapper) {
            super(fieldVisitor, remapper);
        }

        @Override
        public AnnotationVisitor createAnnotationRemapper(String descriptor, AnnotationVisitor annotationVisitor) {
            return new AsmAnnotationRemapper(descriptor, annotationVisitor, (AsmRemapper)this.remapper);
        }
    }

    static class AsmMethodRemapper
    extends MethodRemapper {
        private final TinyRemapper tr;
        private static final String[] singleCharStrings = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
        private final String owner;
        private final MethodNode methodNode;
        private final MethodVisitor output;
        private final Map<String, Integer> nameCounts = new HashMap<String, Integer>();
        private final boolean checkPackageAccess;
        private final boolean skipLocalMapping;
        private final boolean renameInvalidLocals;
        private final Pattern invalidLvNamePattern;
        private final boolean inferNameFromSameLvIndex;

        AsmMethodRemapper(MethodVisitor methodVisitor, AsmRemapper remapper, String owner, MethodNode methodNode, boolean checkPackageAccess, boolean skipLocalMapping, boolean renameInvalidLocals, Pattern invalidLvNamePattern, boolean inferNameFromSameLvIndex) {
            super(methodNode != null ? methodNode : methodVisitor, remapper);
            this.owner = owner;
            this.methodNode = methodNode;
            this.output = methodVisitor;
            this.checkPackageAccess = checkPackageAccess;
            this.skipLocalMapping = skipLocalMapping;
            this.renameInvalidLocals = renameInvalidLocals;
            this.invalidLvNamePattern = invalidLvNamePattern;
            this.inferNameFromSameLvIndex = inferNameFromSameLvIndex;
            this.tr = remapper.tr;
        }

        @Override
        public AnnotationVisitor createAnnotationRemapper(String descriptor, AnnotationVisitor annotationVisitor) {
            return new AsmAnnotationRemapper(descriptor, annotationVisitor, (AsmRemapper)this.remapper);
        }

        @Override
        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
            if (this.checkPackageAccess) {
                PackageAccessChecker.checkClass(this.owner, type, "try-catch", (AsmRemapper)this.remapper);
            }
            super.visitTryCatchBlock(start, end, handler, type);
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            if (this.checkPackageAccess) {
                PackageAccessChecker.checkClass(this.owner, type, "type instruction", (AsmRemapper)this.remapper);
            }
            super.visitTypeInsn(opcode, type);
        }

        @Override
        public void visitLdcInsn(Object value) {
            if (this.checkPackageAccess) {
                PackageAccessChecker.checkValue(this.owner, value, "ldc instruction", (AsmRemapper)this.remapper);
            }
            super.visitLdcInsn(value);
        }

        @Override
        public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
            if (this.checkPackageAccess) {
                PackageAccessChecker.checkDesc(this.owner, descriptor, "multianewarray instruction", (AsmRemapper)this.remapper);
            }
            super.visitMultiANewArrayInsn(descriptor, numDimensions);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            if (this.checkPackageAccess) {
                PackageAccessChecker.checkMember(this.owner, owner, name, descriptor, TrMember.MemberType.FIELD, "field instruction", (AsmRemapper)this.remapper);
            }
            super.visitFieldInsn(opcode, owner, name, descriptor);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            if (this.checkPackageAccess) {
                PackageAccessChecker.checkMember(this.owner, owner, name, descriptor, TrMember.MemberType.METHOD, "method instruction", (AsmRemapper)this.remapper);
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }

        @Override
        public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object ... bootstrapMethodArguments) {
            Handle implemented = this.getLambdaImplementedMethod(name, descriptor, bootstrapMethodHandle, this.tr.knownIndyBsm, bootstrapMethodArguments);
            name = implemented != null ? this.remapper.mapMethodName(implemented.getOwner(), implemented.getName(), implemented.getDesc()) : this.remapper.mapInvokeDynamicMethodName(name, descriptor);
            for (int i = 0; i < bootstrapMethodArguments.length; ++i) {
                bootstrapMethodArguments[i] = this.remapper.mapValue(bootstrapMethodArguments[i]);
            }
            this.mv.visitInvokeDynamicInsn(name, this.remapper.mapMethodDesc(descriptor), (Handle)this.remapper.mapValue(bootstrapMethodHandle), bootstrapMethodArguments);
        }

        private Handle getLambdaImplementedMethod(String name, String desc, Handle bsm, Set<String> knownIndyBsm, Object ... bsmArgs) {
            if (AsmMethodRemapper.isJavaLambdaMetafactory(bsm)) {
                assert (desc.endsWith(";"));
                return new Handle(9, desc.substring(desc.lastIndexOf(41) + 2, desc.length() - 1), name, ((Type)bsmArgs[0]).getDescriptor(), true);
            }
            if (knownIndyBsm.contains(bsm.getOwner())) {
                return null;
            }
            this.tr.getLogger().warn("unknown invokedynamic bsm: %s/%s%s (tag=%d iif=%b)", bsm.getOwner(), bsm.getName(), bsm.getDesc(), bsm.getTag(), bsm.isInterface());
            return null;
        }

        private static boolean isJavaLambdaMetafactory(Handle bsm) {
            return bsm.getTag() == 6 && bsm.getOwner().equals("java/lang/invoke/LambdaMetafactory") && (bsm.getName().equals("metafactory") && bsm.getDesc().equals("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;") || bsm.getName().equals("altMetafactory") && bsm.getDesc().equals("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;")) && !bsm.isInterface();
        }

        @Override
        public void visitEnd() {
            if (this.methodNode != null) {
                if (!this.skipLocalMapping || this.renameInvalidLocals && (this.methodNode.localVariables != null && !this.methodNode.localVariables.isEmpty() || this.methodNode.parameters != null && !this.methodNode.parameters.isEmpty())) {
                    this.processLocals();
                }
                this.methodNode.visitEnd();
                this.methodNode.accept(this.output);
            } else {
                super.visitEnd();
            }
        }

        private void processLocals() {
            int i;
            boolean isStatic = (this.methodNode.access & 8) != 0;
            Type[] argTypes = Type.getArgumentTypes(this.methodNode.desc);
            int argLvSize = AsmMethodRemapper.getLvIndex(argTypes.length, isStatic, argTypes);
            String[] args = new String[argTypes.length];
            if (this.methodNode.parameters != null && this.methodNode.parameters.size() == args.length) {
                for (i = 0; i < args.length; ++i) {
                    args[i] = this.methodNode.parameters.get((int)i).name;
                }
            } else assert (this.methodNode.parameters == null);
            if (this.methodNode.localVariables != null) {
                for (i = 0; i < this.methodNode.localVariables.size(); ++i) {
                    LocalVariableNode lv = this.methodNode.localVariables.get(i);
                    if (!isStatic && lv.index == 0) {
                        lv.name = "this";
                        continue;
                    }
                    if (lv.index < argLvSize) {
                        int asmIndex = AsmMethodRemapper.getAsmIndex(lv.index, isStatic, argTypes);
                        String existingName = args[asmIndex];
                        if (existingName != null && (AsmMethodRemapper.isValidJavaIdentifier(existingName) || !AsmMethodRemapper.isValidJavaIdentifier(lv.name))) continue;
                        args[asmIndex] = lv.name;
                        continue;
                    }
                    if (this.skipLocalMapping) continue;
                    int startOpIdx = 0;
                    AbstractInsnNode start = lv.start;
                    while ((start = start.getPrevious()) != null) {
                        if (start.getOpcode() < 0) continue;
                        ++startOpIdx;
                    }
                    lv.name = ((AsmRemapper)this.remapper).mapMethodVar(this.owner, this.methodNode.name, this.methodNode.desc, lv.index, startOpIdx, i, lv.name);
                    if (!this.renameInvalidLocals || !this.isValidLvName(lv.name)) continue;
                    this.nameCounts.putIfAbsent(lv.name, 1);
                }
            }
            if (!this.skipLocalMapping) {
                for (i = 0; i < args.length; ++i) {
                    args[i] = ((AsmRemapper)this.remapper).mapMethodArg(this.owner, this.methodNode.name, this.methodNode.desc, AsmMethodRemapper.getLvIndex(i, isStatic, argTypes), args[i]);
                    if (!this.renameInvalidLocals || !this.isValidLvName(args[i])) continue;
                    this.nameCounts.putIfAbsent(args[i], 1);
                }
            }
            if (this.renameInvalidLocals) {
                for (i = 0; i < args.length; ++i) {
                    if (this.isValidLvName(args[i])) continue;
                    args[i] = this.getNameFromType(this.remapper.mapDesc(argTypes[i].getDescriptor()), true);
                }
            }
            boolean hasAnyArgs = false;
            boolean hasAllArgs = true;
            for (String arg : args) {
                if (arg != null) {
                    hasAnyArgs = true;
                    continue;
                }
                hasAllArgs = false;
            }
            if (this.methodNode.localVariables != null || hasAnyArgs && (this.methodNode.access & 0x400) == 0) {
                if (this.methodNode.localVariables == null) {
                    this.methodNode.localVariables = new ArrayList<LocalVariableNode>();
                }
                boolean[] argsWritten = new boolean[args.length];
                block6: for (int i2 = 0; i2 < this.methodNode.localVariables.size(); ++i2) {
                    LocalVariableNode lv = this.methodNode.localVariables.get(i2);
                    if (!isStatic && lv.index == 0) continue;
                    if (lv.index < argLvSize) {
                        int asmIndex = AsmMethodRemapper.getAsmIndex(lv.index, isStatic, argTypes);
                        lv.name = args[asmIndex];
                        argsWritten[asmIndex] = true;
                        continue;
                    }
                    if (!this.renameInvalidLocals || this.isValidLvName(lv.name)) continue;
                    if (this.inferNameFromSameLvIndex) {
                        for (int j = 0; j < this.methodNode.localVariables.size(); ++j) {
                            if (j == i2) continue;
                            LocalVariableNode otherLv = this.methodNode.localVariables.get(j);
                            if (otherLv.index != lv.index || otherLv.name == null || !otherLv.desc.equals(lv.desc) || j >= i2 && !this.isValidLvName(otherLv.name)) continue;
                            lv.name = otherLv.name;
                            continue block6;
                        }
                    }
                    lv.name = this.getNameFromType(lv.desc, false);
                }
                LabelNode start = null;
                LabelNode end = null;
                for (int i3 = 0; i3 < args.length; ++i3) {
                    if (argsWritten[i3] || args[i3] == null) continue;
                    if (start == null) {
                        boolean pastStart = false;
                        for (AbstractInsnNode ain : this.methodNode.instructions) {
                            if (ain.getType() == 8) {
                                LabelNode label = (LabelNode)ain;
                                if (start == null && !pastStart) {
                                    start = label;
                                }
                                end = label;
                                continue;
                            }
                            if (ain.getOpcode() < 0) continue;
                            pastStart = true;
                            end = null;
                        }
                        if (start == null) {
                            start = new LabelNode();
                            this.methodNode.instructions.insert(start);
                        }
                        if (end == null) {
                            if (!pastStart) {
                                end = start;
                            } else {
                                end = new LabelNode();
                                this.methodNode.instructions.add(end);
                            }
                        }
                    }
                    this.methodNode.localVariables.add(new LocalVariableNode(args[i3], this.remapper.mapDesc(argTypes[i3].getDescriptor()), null, start, end, AsmMethodRemapper.getLvIndex(i3, isStatic, argTypes)));
                }
            }
            if (this.methodNode.parameters != null || hasAllArgs && args.length > 0 || hasAnyArgs && (this.methodNode.access & 0x400) != 0) {
                if (this.methodNode.parameters == null) {
                    this.methodNode.parameters = new ArrayList<ParameterNode>(args.length);
                }
                while (this.methodNode.parameters.size() < args.length) {
                    this.methodNode.parameters.add(new ParameterNode(null, 0));
                }
                for (int i4 = 0; i4 < args.length; ++i4) {
                    this.methodNode.parameters.get((int)i4).name = args[i4];
                }
            }
        }

        private static int getLvIndex(int asmIndex, boolean isStatic, Type[] argTypes) {
            int ret = 0;
            if (!isStatic) {
                ++ret;
            }
            for (int i = 0; i < asmIndex; ++i) {
                ret += argTypes[i].getSize();
            }
            return ret;
        }

        private static int getAsmIndex(int lvIndex, boolean isStatic, Type[] argTypes) {
            if (!isStatic) {
                --lvIndex;
            }
            for (int i = 0; i < argTypes.length; ++i) {
                if (lvIndex == 0) {
                    return i;
                }
                lvIndex -= argTypes[i].getSize();
            }
            return -1;
        }

        /*
         * WARNING - void declaration
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        private String getNameFromType(String type, boolean isArg) {
            void var5_25;
            void var5_20;
            void var5_18;
            String pluralVarName;
            boolean plural = false;
            if (type.charAt(0) == '[') {
                plural = true;
                type = type.substring(type.lastIndexOf(91) + 1);
            }
            boolean incrementLetter = true;
            switch (type.charAt(0)) {
                case 'B': {
                    String string = "b";
                    break;
                }
                case 'C': {
                    String string = "c";
                    break;
                }
                case 'D': {
                    String string = "d";
                    break;
                }
                case 'F': {
                    String string = "f";
                    break;
                }
                case 'I': {
                    String string = "i";
                    break;
                }
                case 'J': {
                    String string = "l";
                    break;
                }
                case 'S': {
                    String string = "s";
                    break;
                }
                case 'Z': {
                    String string = "bl";
                    incrementLetter = false;
                    break;
                }
                case 'L': {
                    void var5_15;
                    int start = type.lastIndexOf(47) + 1;
                    int startDollar = type.lastIndexOf(36) + 1;
                    if (startDollar > start && startDollar < type.length() - 1) {
                        start = startDollar;
                    } else if (start == 0) {
                        start = 1;
                    }
                    char first = type.charAt(start);
                    char firstLc = Character.toLowerCase(first);
                    if (first == firstLc) {
                        Object var5_13 = null;
                    } else {
                        String string = firstLc + type.substring(start + 1, type.length() - 1);
                    }
                    if (!AsmMethodRemapper.isValidJavaIdentifier((String)var5_15)) {
                        String string = isArg ? "arg" : "lv";
                    }
                    incrementLetter = false;
                    break;
                }
                default: {
                    throw new IllegalStateException();
                }
            }
            boolean hasPluralS = false;
            if (plural && !AsmMethodRemapper.isJavaKeyword(pluralVarName = (String)var5_18 + 's')) {
                String string = pluralVarName;
                hasPluralS = true;
            }
            if (incrementLetter) {
                void var5_21;
                int index = -1;
                while (this.nameCounts.putIfAbsent((String)var5_21, 1) != null || AsmMethodRemapper.isJavaKeyword((String)var5_21)) {
                    if (index < 0) {
                        index = AsmMethodRemapper.getNameIndex((String)var5_21, hasPluralS);
                    }
                    String string = AsmMethodRemapper.getIndexName(++index, plural);
                }
                return var5_21;
            }
            void baseVarName = var5_20;
            int count = this.nameCounts.compute((String)baseVarName, (k, v) -> v == null ? 1 : v + 1);
            if (count == 1) {
                if (!AsmMethodRemapper.isJavaKeyword((String)baseVarName)) return var5_20;
                String string = (String)var5_20 + '_';
            } else {
                String string = (String)baseVarName + Integer.toString(count);
            }
            while (this.nameCounts.putIfAbsent((String)var5_25, 1) != null) {
                String string = (String)baseVarName + Integer.toString(count++);
            }
            this.nameCounts.put((String)baseVarName, count);
            return var5_25;
        }

        private static int getNameIndex(String name, boolean plural) {
            int ret = 0;
            int max = name.length() - (plural ? 1 : 0);
            for (int i = 0; i < max; ++i) {
                ret = ret * 26 + name.charAt(i) - 97 + 1;
            }
            return ret - 1;
        }

        private static String getIndexName(int index, boolean plural) {
            int next;
            if (index < 26 && !plural) {
                return singleCharStrings[index];
            }
            StringBuilder ret = new StringBuilder(2);
            do {
                next = index / 26;
                int cur = index - next * 26;
                ret.append((char)(97 + cur));
            } while ((index = next - 1) >= 0);
            ret.reverse();
            if (plural) {
                ret.append('s');
            }
            return ret.toString();
        }

        private boolean isValidLvName(String s) {
            return AsmMethodRemapper.isValidJavaIdentifier(s) && !AsmMethodRemapper.isJavaKeyword(s) && (this.invalidLvNamePattern == null || !this.invalidLvNamePattern.matcher(s).matches());
        }

        private static boolean isValidJavaIdentifier(String s) {
            return s != null && !s.isEmpty() && SourceVersion.isIdentifier(s) && !s.codePoints().anyMatch(Character::isIdentifierIgnorable);
        }

        private static boolean isJavaKeyword(String s) {
            return SourceVersion.isKeyword(s);
        }
    }

    static class AsmRecordComponentRemapper
    extends RecordComponentRemapper {
        AsmRecordComponentRemapper(RecordComponentVisitor recordComponentVisitor, AsmRemapper remapper) {
            super(recordComponentVisitor, remapper);
        }

        @Override
        public AnnotationVisitor createAnnotationRemapper(String descriptor, AnnotationVisitor annotationVisitor) {
            return new AsmAnnotationRemapper(descriptor, annotationVisitor, (AsmRemapper)this.remapper);
        }
    }

    static class AsmAnnotationRemapper
    extends AnnotationVisitor {
        protected final String descriptor;
        protected final AsmRemapper remapper;

        AsmAnnotationRemapper(String descriptor, AnnotationVisitor annotationVisitor, AsmRemapper remapper) {
            super(589824, annotationVisitor);
            this.descriptor = descriptor;
            this.remapper = remapper;
        }

        @Override
        public void visit(String name, Object value) {
            super.visit(this.mapAnnotationAttributeName(name, AsmAnnotationRemapper.getDescriptor(value)), this.remapper.mapValue(value));
        }

        @Override
        public void visitEnum(String name, String descriptor, String value) {
            super.visitEnum(this.mapAnnotationAttributeName(name, descriptor), this.remapper.mapDesc(descriptor), this.remapper.mapFieldName(Type.getType(descriptor).getInternalName(), value, descriptor));
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String descriptor) {
            AnnotationVisitor annotationVisitor = super.visitAnnotation(this.mapAnnotationAttributeName(name, descriptor), this.remapper.mapDesc(descriptor));
            if (annotationVisitor == null) {
                return null;
            }
            return annotationVisitor == this.av ? this : this.createAnnotationRemapper(descriptor, annotationVisitor);
        }

        public AnnotationVisitor createAnnotationRemapper(String descriptor, AnnotationVisitor annotationVisitor) {
            return new AsmAnnotationRemapper(descriptor, annotationVisitor, this.remapper);
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            return new AsmArrayAttributeAnnotationRemapper(name, desc -> super.visitArray(this.mapAnnotationAttributeName(name, desc == null ? null : "[" + desc)), this.remapper);
        }

        protected String mapAnnotationAttributeName(String name, String attributeDesc) {
            if (this.descriptor == null || name == null) {
                return name;
            }
            return this.remapper.mapAnnotationAttributeName(this.descriptor, name, attributeDesc);
        }

        protected static String getDescriptor(Object value) {
            if (value instanceof Type) {
                return ((Type)value).getDescriptor();
            }
            Class<?> cls = value.getClass();
            if (Byte.class.isAssignableFrom(cls)) {
                return "B";
            }
            if (Boolean.class.isAssignableFrom(cls)) {
                return "Z";
            }
            if (Character.class.isAssignableFrom(cls)) {
                return "C";
            }
            if (Short.class.isAssignableFrom(cls)) {
                return "S";
            }
            if (Integer.class.isAssignableFrom(cls)) {
                return "I";
            }
            if (Long.class.isAssignableFrom(cls)) {
                return "J";
            }
            if (Float.class.isAssignableFrom(cls)) {
                return "F";
            }
            if (Double.class.isAssignableFrom(cls)) {
                return "D";
            }
            return Type.getDescriptor(cls);
        }

        private static class AsmArrayAttributeAnnotationRemapper
        extends AsmAnnotationRemapper {
            protected final String arrayName;
            protected final Function<String, AnnotationVisitor> avSupplier;

            AsmArrayAttributeAnnotationRemapper(String arrayName, Function<String, AnnotationVisitor> avSupplier, AsmRemapper remapper) {
                super(null, null, remapper);
                this.arrayName = arrayName;
                this.avSupplier = Objects.requireNonNull(avSupplier);
            }

            @Override
            public void visit(String name, Object value) {
                if (this.av == null) {
                    this.av = this.avSupplier.apply(AsmArrayAttributeAnnotationRemapper.getDescriptor(value));
                }
                super.visit(name, value);
            }

            @Override
            public void visitEnum(String name, String descriptor, String value) {
                if (this.av == null) {
                    this.av = this.avSupplier.apply(descriptor);
                }
                super.visitEnum(name, descriptor, value);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String name, String descriptor) {
                if (this.av == null) {
                    this.av = this.avSupplier.apply(descriptor);
                }
                return super.visitAnnotation(name, descriptor);
            }

            @Override
            public AnnotationVisitor visitArray(String name) {
                return new AsmArrayAttributeAnnotationRemapper(name, desc -> {
                    if (this.av == null) {
                        this.av = this.avSupplier.apply(desc == null ? null : "[" + desc);
                    }
                    return super.visitArray(this.mapAnnotationAttributeName(name, desc == null ? null : "[" + desc));
                }, this.remapper);
            }

            @Override
            public void visitEnd() {
                if (this.av == null) {
                    this.av = this.avSupplier.apply(null);
                }
                super.visitEnd();
            }
        }
    }
}

