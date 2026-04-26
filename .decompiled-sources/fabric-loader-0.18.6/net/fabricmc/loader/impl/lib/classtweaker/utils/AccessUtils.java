/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.classtweaker.utils;

public class AccessUtils {
    public static int makePublic(int i) {
        return i & 0xFFFFFFF9 | 1;
    }

    public static int makeProtected(int i) {
        if ((i & 1) != 0) {
            return i;
        }
        return i & 0xFFFFFFFD | 4;
    }

    public static int makeFinalIfPrivate(int access, String name, int ownerAccess) {
        if (name.equals("<init>")) {
            return access;
        }
        if ((ownerAccess & 0x200) != 0 || (access & 8) != 0) {
            return access;
        }
        if ((access & 2) != 0) {
            return access | 0x10;
        }
        return access;
    }

    public static int removeFinal(int i) {
        return i & 0xFFFFFFEF;
    }
}

