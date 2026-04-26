/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.specs;

public interface ILogAble {
    public static final ILogAble CONSOLE = new ILogAble(){

        @Override
        public void log(String message) {
            System.out.println(message);
        }
    };

    public void log(String var1);
}

