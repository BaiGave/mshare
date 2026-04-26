/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.util.mappings;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.fabricmc.loader.impl.lib.mappingio.tree.MappingTree;
import net.fabricmc.loader.impl.util.mappings.MixinRemapper;
import org.spongepowered.asm.mixin.transformer.ClassInfo;

public class MixinIntermediaryDevRemapper
extends MixinRemapper {
    private static final String ambiguousName = "<ambiguous>";
    private final Set<String> allPossibleClassNames = new HashSet<String>();
    private final Map<String, String> nameFieldLookup = new HashMap<String, String>();
    private final Map<String, String> nameMethodLookup = new HashMap<String, String>();
    private final Map<String, String> nameDescFieldLookup = new HashMap<String, String>();
    private final Map<String, String> nameDescMethodLookup = new HashMap<String, String>();

    public MixinIntermediaryDevRemapper(MappingTree mappings, String from, String to) {
        super(mappings, mappings.getNamespaceId(from), mappings.getNamespaceId(to));
        for (MappingTree.ClassMapping classMapping : mappings.getClasses()) {
            this.allPossibleClassNames.add(classMapping.getName(from));
            this.allPossibleClassNames.add(classMapping.getName(to));
            this.putMemberInLookup(this.fromId, this.toId, classMapping.getFields(), this.nameFieldLookup, this.nameDescFieldLookup);
            this.putMemberInLookup(this.fromId, this.toId, classMapping.getMethods(), this.nameMethodLookup, this.nameDescMethodLookup);
        }
    }

    private <T extends MappingTree.MemberMapping> void putMemberInLookup(int from, int to, Collection<T> descriptored, Map<String, String> nameMap, Map<String, String> nameDescMap) {
        for (MappingTree.MemberMapping field : descriptored) {
            String key;
            String nameFrom = field.getName(from);
            String descFrom = field.getDesc(from);
            String nameTo = field.getName(to);
            String prev = nameMap.putIfAbsent(nameFrom, nameTo);
            if (prev != null && prev != ambiguousName && !prev.equals(nameTo)) {
                nameDescMap.put(nameFrom, ambiguousName);
            }
            if ((prev = nameDescMap.putIfAbsent(key = MixinIntermediaryDevRemapper.getNameDescKey(nameFrom, descFrom), nameTo)) == null || prev == ambiguousName || prev.equals(nameTo)) continue;
            nameDescMap.put(key, ambiguousName);
        }
    }

    private void throwAmbiguousLookup(String type, String name, String desc) {
        throw new RuntimeException("Ambiguous Mixin: " + type + " lookup " + name + " " + desc + " is not unique");
    }

    private String mapMethodNameInner(String owner, String name, String desc) {
        String result = super.mapMethodName(owner, name, desc);
        if (result.equals(name)) {
            String otherClass = this.unmap(owner);
            return super.mapMethodName(otherClass, name, this.unmapDesc(desc));
        }
        return result;
    }

    private String mapFieldNameInner(String owner, String name, String desc) {
        String result = super.mapFieldName(owner, name, desc);
        if (result.equals(name)) {
            String otherClass = this.unmap(owner);
            return super.mapFieldName(otherClass, name, this.unmapDesc(desc));
        }
        return result;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public String mapMethodName(String owner, String name, String desc) {
        ClassInfo classInfo;
        if (owner == null || this.allPossibleClassNames.contains(owner)) {
            String newName = desc == null ? this.nameMethodLookup.get(name) : this.nameDescMethodLookup.get(MixinIntermediaryDevRemapper.getNameDescKey(name, desc));
            if (newName != null) {
                if (newName != ambiguousName) return newName;
                if (owner == null) {
                    this.throwAmbiguousLookup("method", name, desc);
                }
            } else {
                if (owner == null) {
                    return name;
                }
                String unmapOwner = this.unmap(owner);
                String unmapDesc = this.unmapDesc(desc);
                if (unmapOwner.equals(owner) && unmapDesc.equals(desc)) return name;
                return this.mapMethodName(unmapOwner, name, unmapDesc);
            }
        }
        if ((classInfo = ClassInfo.forName(this.map(owner))) == null) {
            return name;
        }
        ArrayDeque<ClassInfo> queue = new ArrayDeque<ClassInfo>();
        do {
            ClassInfo cSuper;
            String ownerO;
            String s;
            if (!(s = this.mapMethodNameInner(ownerO = this.unmap(classInfo.getName()), name, desc)).equals(name)) {
                return s;
            }
            if (classInfo.getSuperName() != null && !classInfo.getSuperName().startsWith("java/") && (cSuper = classInfo.getSuperClass()) != null) {
                queue.add(cSuper);
            }
            for (String itf : classInfo.getInterfaces()) {
                ClassInfo cItf;
                if (itf.startsWith("java/") || (cItf = ClassInfo.forName(itf)) == null) continue;
                queue.add(cItf);
            }
        } while ((classInfo = (ClassInfo)queue.poll()) != null);
        return name;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public String mapFieldName(String owner, String name, String desc) {
        if (owner == null || this.allPossibleClassNames.contains(owner)) {
            String newName = this.nameDescFieldLookup.get(MixinIntermediaryDevRemapper.getNameDescKey(name, desc));
            if (newName != null) {
                if (newName != ambiguousName) return newName;
                if (owner == null) {
                    this.throwAmbiguousLookup("field", name, desc);
                }
            } else {
                if (owner == null) {
                    return name;
                }
                String unmapOwner = this.unmap(owner);
                String unmapDesc = this.unmapDesc(desc);
                if (unmapOwner.equals(owner) && unmapDesc.equals(desc)) return name;
                return this.mapFieldName(unmapOwner, name, unmapDesc);
            }
        }
        for (ClassInfo c = ClassInfo.forName(this.map(owner)); c != null; c = c.getSuperClass()) {
            String nextOwner = this.unmap(c.getName());
            String s = this.mapFieldNameInner(nextOwner, name, desc);
            if (!s.equals(name)) {
                return s;
            }
            if (c.getSuperName() == null || c.getSuperName().startsWith("java/")) return name;
        }
        return name;
    }

    private static String getNameDescKey(String name, String descriptor) {
        return name + ";;" + descriptor;
    }
}

