/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.api.metadata;

import net.fabricmc.api.EnvType;

public enum ModEnvironment {
    CLIENT,
    SERVER,
    UNIVERSAL;


    public boolean matches(EnvType type) {
        switch (this.ordinal()) {
            case 0: {
                return type == EnvType.CLIENT;
            }
            case 1: {
                return type == EnvType.SERVER;
            }
            case 2: {
                return true;
            }
        }
        return false;
    }
}

