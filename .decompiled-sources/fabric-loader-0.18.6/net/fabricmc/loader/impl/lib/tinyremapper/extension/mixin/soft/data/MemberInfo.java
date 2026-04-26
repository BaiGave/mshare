/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.soft.data;

import java.util.Objects;
import net.fabricmc.loader.impl.lib.tinyremapper.api.TrMember;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.common.StringUtility;

public final class MemberInfo {
    private final String owner;
    private final String name;
    private final String quantifier;
    private final String desc;

    public MemberInfo(String owner, String name, String quantifier, String desc) {
        this.owner = Objects.requireNonNull(owner);
        this.name = Objects.requireNonNull(name);
        this.quantifier = Objects.requireNonNull(quantifier);
        this.desc = Objects.requireNonNull(desc);
    }

    public String getOwner() {
        return this.owner;
    }

    public String getName() {
        return this.name;
    }

    public String getQuantifier() {
        return this.quantifier;
    }

    public String getDesc() {
        return this.desc;
    }

    public TrMember.MemberType getType() {
        if (this.desc.isEmpty()) {
            return null;
        }
        return StringUtility.isMethodDesc(this.desc) ? TrMember.MemberType.METHOD : TrMember.MemberType.FIELD;
    }

    public boolean isFullyQualified() {
        return !this.owner.isEmpty() && !this.name.isEmpty() && !this.desc.isEmpty();
    }

    public static boolean isRegex(String str) {
        return str.endsWith("/");
    }

    public static boolean isDynamic(String str) {
        return str.startsWith("@");
    }

    public static MemberInfo parse(String str) {
        if (MemberInfo.isRegex(str) || MemberInfo.isDynamic(str)) {
            return null;
        }
        str = str.replaceAll("\\s", "");
        String descriptor = "";
        String quantifier = "";
        String name = "";
        String owner = "";
        int sep = str.indexOf(40);
        if (sep >= 0) {
            descriptor = str.substring(sep);
            str = str.substring(0, sep);
        } else {
            sep = str.indexOf(":");
            if (sep >= 0) {
                descriptor = str.substring(sep + 1);
                str = str.substring(0, sep);
            }
        }
        sep = str.indexOf(42);
        if (sep >= 0) {
            quantifier = str.substring(sep);
            str = str.substring(0, sep);
        } else {
            sep = str.indexOf(43);
            if (sep >= 0) {
                quantifier = str.substring(sep);
                str = str.substring(0, sep);
            } else {
                sep = str.indexOf(123);
                if (sep >= 0) {
                    quantifier = str.substring(sep);
                    str = str.substring(0, sep);
                }
            }
        }
        sep = str.indexOf(59);
        if (sep >= 0) {
            owner = StringUtility.classDescToName(str.substring(0, sep + 1));
            str = str.substring(sep + 1);
        } else {
            sep = str.lastIndexOf(46);
            if (sep >= 0) {
                owner = str.substring(0, sep).replace('.', '/');
                str = str.substring(sep + 1);
            }
        }
        if (str.contains("/") || str.contains(".")) {
            owner = str.replace('.', '/');
        } else {
            name = str;
        }
        return new MemberInfo(owner, name, quantifier, descriptor);
    }

    public String toString() {
        String owner = this.getOwner().isEmpty() ? "" : StringUtility.classNameToDesc(this.getOwner());
        return owner + this.name + this.quantifier + this.formattedDesc();
    }

    private String formattedDesc() {
        String desc = this.getDesc();
        if (desc.isEmpty()) {
            return "";
        }
        if (Objects.equals((Object)this.getType(), (Object)TrMember.MemberType.FIELD)) {
            return ":" + desc;
        }
        return desc;
    }
}

