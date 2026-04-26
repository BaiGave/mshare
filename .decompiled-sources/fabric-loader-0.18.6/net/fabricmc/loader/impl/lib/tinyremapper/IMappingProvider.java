/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.tinyremapper;

@FunctionalInterface
public interface IMappingProvider {
    public void load(MappingAcceptor var1);

    public static final class Member {
        public String owner;
        public String name;
        public String desc;

        public Member(String owner, String name, String desc) {
            this.owner = owner;
            this.name = name;
            this.desc = desc;
        }
    }

    public static interface MappingAcceptor {
        public void acceptClass(String var1, String var2);

        public void acceptMethod(Member var1, String var2);

        public void acceptMethodArg(Member var1, int var2, String var3);

        public void acceptMethodVar(Member var1, int var2, int var3, int var4, String var5);

        public void acceptField(Member var1, String var2);
    }
}

