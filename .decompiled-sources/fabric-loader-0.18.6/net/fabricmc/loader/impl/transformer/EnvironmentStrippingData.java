/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.transformer;

import java.util.Collection;
import java.util.HashSet;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.fabricmc.api.EnvironmentInterfaces;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class EnvironmentStrippingData
extends ClassVisitor {
    private static final String ENVIRONMENT_DESCRIPTOR = Type.getDescriptor(Environment.class);
    private static final String ENVIRONMENT_INTERFACE_DESCRIPTOR = Type.getDescriptor(EnvironmentInterface.class);
    private static final String ENVIRONMENT_INTERFACES_DESCRIPTOR = Type.getDescriptor(EnvironmentInterfaces.class);
    private final String envType;
    private boolean stripEntireClass = false;
    private final Collection<String> stripInterfaces = new HashSet<String>();
    private final Collection<String> stripFields = new HashSet<String>();
    private final Collection<String> stripMethods = new HashSet<String>();

    private AnnotationVisitor visitMemberAnnotation(String descriptor, boolean visible, Runnable onEnvMismatch) {
        if (ENVIRONMENT_DESCRIPTOR.equals(descriptor)) {
            return new EnvironmentAnnotationVisitor(this.api, onEnvMismatch);
        }
        return null;
    }

    public EnvironmentStrippingData(int api, String envType) {
        super(api);
        this.envType = envType;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (ENVIRONMENT_DESCRIPTOR.equals(descriptor)) {
            return new EnvironmentAnnotationVisitor(this.api, () -> {
                this.stripEntireClass = true;
            });
        }
        if (ENVIRONMENT_INTERFACE_DESCRIPTOR.equals(descriptor)) {
            return new EnvironmentInterfaceAnnotationVisitor(this.api);
        }
        if (ENVIRONMENT_INTERFACES_DESCRIPTOR.equals(descriptor)) {
            return new AnnotationVisitor(this.api){

                @Override
                public AnnotationVisitor visitArray(String name) {
                    if ("value".equals(name)) {
                        return new AnnotationVisitor(this.api){

                            @Override
                            public AnnotationVisitor visitAnnotation(String name, String descriptor) {
                                return new EnvironmentInterfaceAnnotationVisitor(this.api);
                            }
                        };
                    }
                    return null;
                }
            };
        }
        return null;
    }

    @Override
    public FieldVisitor visitField(int access, final String name, final String descriptor, String signature, Object value) {
        return new FieldVisitor(this, this.api){
            final /* synthetic */ EnvironmentStrippingData this$0;
            {
                this.this$0 = this$0;
                super(arg0);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String annotationDescriptor, boolean visible) {
                return this.this$0.visitMemberAnnotation(annotationDescriptor, visible, () -> this.this$0.stripFields.add(name + descriptor));
            }
        };
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        final String methodId = name + descriptor;
        return new MethodVisitor(this, this.api){
            final /* synthetic */ EnvironmentStrippingData this$0;
            {
                this.this$0 = this$0;
                super(arg0);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                return this.this$0.visitMemberAnnotation(descriptor, visible, () -> this.this$0.stripMethods.add(methodId));
            }
        };
    }

    public boolean stripEntireClass() {
        return this.stripEntireClass;
    }

    public Collection<String> getStripInterfaces() {
        return this.stripInterfaces;
    }

    public Collection<String> getStripFields() {
        return this.stripFields;
    }

    public Collection<String> getStripMethods() {
        return this.stripMethods;
    }

    public boolean isEmpty() {
        return this.stripInterfaces.isEmpty() && this.stripFields.isEmpty() && this.stripMethods.isEmpty();
    }

    private class EnvironmentAnnotationVisitor
    extends AnnotationVisitor {
        private final Runnable onEnvMismatch;

        private EnvironmentAnnotationVisitor(int api, Runnable onEnvMismatch) {
            super(api);
            this.onEnvMismatch = onEnvMismatch;
        }

        @Override
        public void visitEnum(String name, String descriptor, String value) {
            if ("value".equals(name) && !EnvironmentStrippingData.this.envType.equals(value)) {
                this.onEnvMismatch.run();
            }
        }
    }

    private class EnvironmentInterfaceAnnotationVisitor
    extends AnnotationVisitor {
        private boolean envMismatch;
        private Type itf;

        private EnvironmentInterfaceAnnotationVisitor(int api) {
            super(api);
        }

        @Override
        public void visitEnum(String name, String descriptor, String value) {
            if ("value".equals(name) && !EnvironmentStrippingData.this.envType.equals(value)) {
                this.envMismatch = true;
            }
        }

        @Override
        public void visit(String name, Object value) {
            if ("itf".equals(name)) {
                this.itf = (Type)value;
            }
        }

        @Override
        public void visitEnd() {
            if (this.envMismatch) {
                EnvironmentStrippingData.this.stripInterfaces.add(this.itf.getInternalName());
            }
        }
    }
}

