/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.transformer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

public class PackageAccessFixer
extends ClassVisitor {
    private static int modAccess(int access) {
        if ((access & 7) != 2) {
            return access & 0xFFFFFFF8 | 1;
        }
        return access;
    }

    public PackageAccessFixer(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, PackageAccessFixer.modAccess(access), name, signature, superName, interfaces);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(name, outerName, innerName, PackageAccessFixer.modAccess(access));
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        return super.visitField(PackageAccessFixer.modAccess(access), name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return super.visitMethod(PackageAccessFixer.modAccess(access), name, descriptor, signature, exceptions);
    }
}

