/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.transformer;

import java.util.ArrayList;
import java.util.Collection;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class ClassStripper
extends ClassVisitor {
    private final Collection<String> stripInterfaces;
    private final Collection<String> stripFields;
    private final Collection<String> stripMethods;
    private String className;

    public ClassStripper(int api, ClassVisitor classVisitor, Collection<String> stripInterfaces, Collection<String> stripFields, Collection<String> stripMethods) {
        super(api, classVisitor);
        this.stripInterfaces = stripInterfaces;
        this.stripFields = stripFields;
        this.stripMethods = stripMethods;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        if (!this.stripInterfaces.isEmpty()) {
            ArrayList<String> interfacesList = new ArrayList<String>();
            for (String itf : interfaces) {
                if (this.stripInterfaces.contains(itf)) continue;
                interfacesList.add(itf);
            }
            interfaces = interfacesList.toArray(new String[0]);
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        if (this.stripFields.contains(name + descriptor)) {
            return null;
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        int opcodeToStrip;
        if (this.stripMethods.contains(name + descriptor)) {
            return null;
        }
        MethodVisitor ret = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (this.stripFields.isEmpty()) {
            return ret;
        }
        switch (name) {
            case "<clinit>": {
                opcodeToStrip = 179;
                break;
            }
            case "<init>": {
                opcodeToStrip = 181;
                break;
            }
            default: {
                return ret;
            }
        }
        return new MethodVisitor(this, this.api, ret){
            final /* synthetic */ ClassStripper this$0;
            {
                this.this$0 = this$0;
                super(arg0, arg1);
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                if (opcode != opcodeToStrip || !owner.equals(this.this$0.className) || !this.this$0.stripFields.contains(name + descriptor)) {
                    super.visitFieldInsn(opcode, owner, name, descriptor);
                } else {
                    super.visitInsn(Type.getType(descriptor).getSize() == 2 ? 88 : 87);
                    if (opcode == 181) {
                        super.visitInsn(87);
                    }
                }
            }
        };
    }
}

