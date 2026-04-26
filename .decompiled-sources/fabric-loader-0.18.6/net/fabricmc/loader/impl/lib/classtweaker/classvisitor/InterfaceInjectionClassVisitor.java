/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.classtweaker.classvisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.loader.impl.lib.classtweaker.api.ClassTweaker;
import net.fabricmc.loader.impl.lib.classtweaker.api.InjectedInterface;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

public class InterfaceInjectionClassVisitor
extends ClassVisitor {
    private final ClassTweaker classTweaker;
    private List<InjectedInterface> injectedInterfaces;
    private final Set<String> knownInnerClasses = new HashSet<String>();

    public InterfaceInjectionClassVisitor(int api, ClassVisitor classVisitor, ClassTweaker classTweaker) {
        super(api, classVisitor);
        this.classTweaker = classTweaker;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        StringBuilder newSignature;
        this.injectedInterfaces = this.classTweaker.getInjectedInterfaces(name);
        if (this.injectedInterfaces.isEmpty()) {
            super.visit(version, access, name, signature, superName, interfaces);
            return;
        }
        LinkedHashSet<String> modifiedInterfaces = new LinkedHashSet<String>();
        Collections.addAll(modifiedInterfaces, interfaces);
        StringBuilder stringBuilder = newSignature = signature == null ? null : new StringBuilder(signature);
        if (newSignature == null && this.injectedInterfaces.stream().anyMatch(InjectedInterface::hasGenerics)) {
            newSignature = new StringBuilder("L").append(superName).append(";");
            for (String baseInterface : interfaces) {
                newSignature.append("L").append(baseInterface).append(";");
            }
        }
        for (InjectedInterface injectedInterface : this.injectedInterfaces) {
            if (!modifiedInterfaces.add(injectedInterface.getInterfaceName()) || newSignature == null) continue;
            newSignature.append(injectedInterface.getInterfaceSignature());
        }
        if (newSignature != null) {
            signature = newSignature.toString();
            SignatureReader reader = new SignatureReader(signature);
            GenericsChecker checker = new GenericsChecker(589824, name, this.injectedInterfaces);
            reader.accept(checker);
            checker.check();
        }
        super.visit(version, access, name, signature, superName, modifiedInterfaces.toArray(new String[0]));
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        this.knownInnerClasses.add(name);
        super.visitInnerClass(name, outerName, innerName, access);
    }

    @Override
    public void visitEnd() {
        for (InjectedInterface itf : this.injectedInterfaces) {
            String innerName;
            String outerName;
            String ifaceName = itf.getInterfaceName();
            if (this.knownInnerClasses.contains(ifaceName)) continue;
            int simpleNameIdx = ifaceName.lastIndexOf(47);
            String simpleName = simpleNameIdx == -1 ? ifaceName : ifaceName.substring(simpleNameIdx + 1);
            int lastIdx = -1;
            int dollarIdx = -1;
            while ((dollarIdx = simpleName.indexOf(36, dollarIdx + 1)) != -1) {
                if (dollarIdx - lastIdx == 1) continue;
                if (lastIdx != -1) {
                    outerName = ifaceName.substring(0, simpleNameIdx + 1 + lastIdx);
                    innerName = simpleName.substring(lastIdx + 1, dollarIdx);
                    super.visitInnerClass(outerName + '$' + innerName, outerName, innerName, 1545);
                }
                lastIdx = dollarIdx;
            }
            if (lastIdx == -1 || lastIdx == simpleName.length()) continue;
            outerName = ifaceName.substring(0, simpleNameIdx + 1 + lastIdx);
            innerName = simpleName.substring(lastIdx + 1);
            super.visitInnerClass(outerName + '$' + innerName, outerName, innerName, 1545);
        }
        super.visitEnd();
    }

    private static class GenericsChecker
    extends SignatureVisitor {
        private final String className;
        private final List<String> typeParameters;
        private final List<InjectedInterface> injectedInterfaces;

        GenericsChecker(int asmVersion, String className, List<InjectedInterface> injectedInterfaces) {
            super(asmVersion);
            this.className = className;
            this.typeParameters = new ArrayList<String>();
            this.injectedInterfaces = injectedInterfaces;
        }

        @Override
        public void visitFormalTypeParameter(String name) {
            this.typeParameters.add(name);
            super.visitFormalTypeParameter(name);
        }

        public void check() {
            for (InjectedInterface injectedInterface : this.injectedInterfaces) {
                if (!injectedInterface.hasGenerics()) continue;
                SignatureReader reader = new SignatureReader(injectedInterface.getInterfaceSignature());
                GenericsConfirm confirm = new GenericsConfirm(589824, this.className, injectedInterface.getInterfaceName(), this.typeParameters);
                reader.accept(confirm);
            }
        }

        public static class GenericsConfirm
        extends SignatureVisitor {
            private final String className;
            private final String interfaceName;
            private final List<String> acceptedTypeVariables;

            GenericsConfirm(int asmVersion, String className, String interfaceName, List<String> acceptedTypeVariables) {
                super(asmVersion);
                this.className = className;
                this.interfaceName = interfaceName;
                this.acceptedTypeVariables = acceptedTypeVariables;
            }

            @Override
            public void visitTypeVariable(String name) {
                if (!this.acceptedTypeVariables.contains(name)) {
                    throw new IllegalStateException("Interface " + this.interfaceName + " attempted to use a type variable named " + name + " which is not present in the " + this.className + " class");
                }
                super.visitTypeVariable(name);
            }
        }
    }
}

