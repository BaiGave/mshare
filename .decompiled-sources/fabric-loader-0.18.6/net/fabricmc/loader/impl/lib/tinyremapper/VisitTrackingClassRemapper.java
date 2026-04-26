/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.tinyremapper;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.RecordComponentVisitor;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

public abstract class VisitTrackingClassRemapper
extends ClassRemapper {
    public VisitTrackingClassRemapper(ClassVisitor classVisitor, Remapper remapper) {
        super(classVisitor, remapper);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.onVisit(VisitKind.INITIAL);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitSource(String source, String debug) {
        this.onVisit(VisitKind.SOURCE);
        super.visitSource(source, debug);
    }

    @Override
    public ModuleVisitor visitModule(String name, int access, String version) {
        this.onVisit(VisitKind.MODULE);
        return super.visitModule(name, access, version);
    }

    @Override
    public void visitNestHost(String nestHost) {
        this.onVisit(VisitKind.NEST_HOST);
        super.visitNestHost(nestHost);
    }

    @Override
    public void visitPermittedSubclass(String permittedSubclass) {
        this.onVisit(VisitKind.PERMITTED_SUBCLASS);
        super.visitPermittedSubclass(permittedSubclass);
    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {
        this.onVisit(VisitKind.OUTER_CLASS);
        super.visitOuterClass(owner, name, descriptor);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        this.onVisit(VisitKind.ANNOTATION);
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        this.onVisit(VisitKind.TYPE_ANNOTATION);
        return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        this.onVisit(VisitKind.ATTRIBUTE);
        super.visitAttribute(attribute);
    }

    @Override
    public void visitNestMember(String nestMember) {
        this.onVisit(VisitKind.NEST_MEMBER);
        super.visitNestMember(nestMember);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        this.onVisit(VisitKind.INNER_CLASS);
        super.visitInnerClass(name, outerName, innerName, access);
    }

    @Override
    public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
        this.onVisit(VisitKind.RECORD_COMPONENT);
        return super.visitRecordComponent(name, descriptor, signature);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        this.onVisit(VisitKind.FIELD);
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        this.onVisit(VisitKind.METHOD);
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        this.onVisit(VisitKind.END);
        super.visitEnd();
    }

    protected abstract void onVisit(VisitKind var1);

    protected static enum VisitKind {
        INITIAL,
        SOURCE,
        MODULE,
        NEST_HOST,
        PERMITTED_SUBCLASS,
        OUTER_CLASS,
        ANNOTATION,
        TYPE_ANNOTATION,
        ATTRIBUTE,
        NEST_MEMBER,
        INNER_CLASS,
        RECORD_COMPONENT,
        FIELD,
        METHOD,
        END;

    }
}

