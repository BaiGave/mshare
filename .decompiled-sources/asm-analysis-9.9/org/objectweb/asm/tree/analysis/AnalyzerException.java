/*
 * Decompiled with CFR 0.152.
 */
package org.objectweb.asm.tree.analysis;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.Value;

public class AnalyzerException
extends Exception {
    private static final long serialVersionUID = 3154190448018943333L;
    public final transient AbstractInsnNode node;

    public AnalyzerException(AbstractInsnNode insn, String message) {
        super(message);
        this.node = insn;
    }

    public AnalyzerException(AbstractInsnNode insn, String message, Throwable cause) {
        super(message, cause);
        this.node = insn;
    }

    public AnalyzerException(AbstractInsnNode insn, String message, Object expected, Value actual) {
        super(AnalyzerException.stringConcat$1(message == null ? "Expected " : AnalyzerException.stringConcat$0(message), String.valueOf(expected), String.valueOf(actual)));
        this.node = insn;
    }

    private static /* synthetic */ String stringConcat$0(String string) {
        return string + ": expected ";
    }

    private static /* synthetic */ String stringConcat$1(String string, String string2, String string3) {
        return string + string2 + ", but found " + string3;
    }
}

