/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.reflect;

import java.lang.reflect.AccessibleObject;

class AccessibleObjects {
    AccessibleObjects() {
    }

    static boolean isAccessible(AccessibleObject accessibleObject) {
        return accessibleObject == null || accessibleObject.isAccessible();
    }

    static boolean setAccessible(AccessibleObject accessibleObject) {
        if (!AccessibleObjects.isAccessible(accessibleObject)) {
            accessibleObject.setAccessible(true);
            return true;
        }
        return false;
    }
}

