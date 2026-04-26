/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.classtweaker.impl;

import java.util.Objects;
import net.fabricmc.loader.impl.lib.classtweaker.api.InjectedInterface;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

public class InjectedInterfaceImpl
implements InjectedInterface {
    private final String injectedInterface;

    public InjectedInterfaceImpl(String injectedInterface) {
        this.injectedInterface = injectedInterface;
    }

    @Override
    public String getInterfaceName() {
        if (!this.hasGenerics()) {
            return this.injectedInterface;
        }
        RawTypeFromSignatureVisitor rawTypeFromSignatureVisitor = new RawTypeFromSignatureVisitor();
        new SignatureReader("L" + this.injectedInterface + ";").accept(rawTypeFromSignatureVisitor);
        return rawTypeFromSignatureVisitor.rawType.toString();
    }

    @Override
    public String getInterfaceSignature() {
        return "L" + this.injectedInterface + ";";
    }

    @Override
    public boolean hasGenerics() {
        return this.injectedInterface.contains("<");
    }

    public int hashCode() {
        return Objects.hash(this.injectedInterface);
    }

    private static final class RawTypeFromSignatureVisitor
    extends SignatureVisitor {
        private final StringBuilder rawType = new StringBuilder();

        RawTypeFromSignatureVisitor() {
            super(589824);
        }

        @Override
        public void visitClassType(String name) {
            this.rawType.append(name);
        }

        @Override
        public void visitInnerClassType(String name) {
            this.rawType.append('$').append(name);
        }

        @Override
        public SignatureVisitor visitTypeArgument(char wildcard) {
            return new SignatureVisitor(589824){};
        }
    }
}

