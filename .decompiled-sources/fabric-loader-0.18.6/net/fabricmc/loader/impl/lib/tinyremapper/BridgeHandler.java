/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.tinyremapper;

import net.fabricmc.loader.impl.lib.tinyremapper.AsmRemapper;
import net.fabricmc.loader.impl.lib.tinyremapper.ClassInstance;
import net.fabricmc.loader.impl.lib.tinyremapper.MemberInstance;
import net.fabricmc.loader.impl.lib.tinyremapper.TinyRemapper;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

final class BridgeHandler {
    BridgeHandler() {
    }

    public static MemberInstance getTarget(MemberInstance bridgeMethod) {
        assert (bridgeMethod.isBridge());
        MemberInstance ret = bridgeMethod.bridgeTarget;
        if (ret != null) {
            return ret;
        }
        String bridgeId = bridgeMethod.getId();
        int descStart = bridgeId.indexOf(40);
        for (MemberInstance m : bridgeMethod.cls.getMembers()) {
            if (m == bridgeMethod || !m.isVirtual() || m.isBridge() || !BridgeHandler.isBridged(bridgeId, m.getId(), descStart, bridgeMethod.getContext())) continue;
            bridgeMethod.bridgeTarget = m;
            return m;
        }
        return null;
    }

    private static boolean isBridged(String bridgeId, String targetId, int descStart, TinyRemapper.MrjState context) {
        int posTarget;
        int posBridge;
        int argsEndTarget;
        if (!bridgeId.regionMatches(0, targetId, 0, descStart + 1)) {
            return false;
        }
        int argsEndBridge = bridgeId.lastIndexOf(41);
        if (!ClassInstance.isAssignableFrom(bridgeId, argsEndBridge + 1, targetId, (argsEndTarget = targetId.lastIndexOf(41)) + 1, context)) {
            return false;
        }
        for (posTarget = posBridge = descStart + 1; posBridge < argsEndBridge && posTarget < argsEndTarget; ++posBridge, ++posTarget) {
            if (!ClassInstance.isAssignableFrom(bridgeId, posBridge, targetId, posTarget, context)) {
                return false;
            }
            char type = bridgeId.charAt(posBridge);
            while (type == '[') {
                type = bridgeId.charAt(++posBridge);
            }
            if (type == 'L') {
                posBridge = bridgeId.indexOf(59, posBridge + 1);
            }
            type = targetId.charAt(posTarget);
            while (type == '[') {
                type = targetId.charAt(++posTarget);
            }
            if (type != 'L') continue;
            posTarget = targetId.indexOf(59, posTarget + 1);
        }
        return posBridge == argsEndBridge && posTarget == argsEndTarget;
    }

    public static void generateCompatBridges(ClassInstance cls, AsmRemapper remapper, ClassVisitor out) {
        block0: for (MemberInstance m : cls.getMembers()) {
            String mappedName;
            String bridgedName = m.getNewBridgedName();
            if (bridgedName == null || (mappedName = m.getNewMappedName()) == null || bridgedName.equals(mappedName)) continue;
            for (MemberInstance o : cls.getMembers()) {
                if (o == m || !o.desc.equals(m.desc) || !remapper.mapMethodName(cls.getName(), o.name, o.desc).equals(mappedName)) continue;
                continue block0;
            }
            String mappedDesc = remapper.mapDesc(m.desc);
            int lvSize = 1;
            MethodVisitor mv = out.visitMethod(m.access | 0x40 | 0x1000, mappedName, mappedDesc, null, null);
            mv.visitCode();
            mv.visitVarInsn(25, 0);
            if (!mappedDesc.startsWith("()")) {
                for (Type type : Type.getArgumentTypes(mappedDesc)) {
                    mv.visitVarInsn(type.getOpcode(21), lvSize);
                    lvSize += type.getSize();
                }
            }
            mv.visitMethodInsn(182, remapper.map(cls.getName()), bridgedName, mappedDesc, cls.isInterface());
            Type retType = Type.getReturnType(mappedDesc);
            mv.visitInsn(retType.getOpcode(172));
            mv.visitMaxs(Math.max(lvSize, retType.getSize()), lvSize);
            mv.visitEnd();
        }
    }
}

