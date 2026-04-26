/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.transfer.item;

public interface SpecialLogicAccess {
    default public boolean fabric_shouldSuppressSpecialLogic() {
        return false;
    }
}

