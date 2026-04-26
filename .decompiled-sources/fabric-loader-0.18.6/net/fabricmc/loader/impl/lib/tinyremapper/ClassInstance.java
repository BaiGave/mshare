/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.tinyremapper;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Predicate;
import net.fabricmc.loader.impl.lib.tinyremapper.BridgeHandler;
import net.fabricmc.loader.impl.lib.tinyremapper.InputTag;
import net.fabricmc.loader.impl.lib.tinyremapper.MemberInstance;
import net.fabricmc.loader.impl.lib.tinyremapper.TinyRemapper;
import net.fabricmc.loader.impl.lib.tinyremapper.api.TrClass;
import net.fabricmc.loader.impl.lib.tinyremapper.api.TrEnvironment;
import net.fabricmc.loader.impl.lib.tinyremapper.api.TrField;
import net.fabricmc.loader.impl.lib.tinyremapper.api.TrMember;
import net.fabricmc.loader.impl.lib.tinyremapper.api.TrMethod;

public final class ClassInstance
implements TrClass {
    private static final MemberInstance nullMember = new MemberInstance(null, null, null, null, 0, 0);
    private static final AtomicReferenceFieldUpdater<ClassInstance, InputTag[]> inputTagsUpdater = AtomicReferenceFieldUpdater.newUpdater(ClassInstance.class, InputTag[].class, "inputTags");
    final TinyRemapper tr;
    private TinyRemapper.MrjState context;
    final boolean isInput;
    private volatile InputTag[] inputTags;
    final Path srcPath;
    byte[] data;
    private ClassInstance mrjOrigin;
    private final Map<String, MemberInstance> members = new HashMap<String, MemberInstance>();
    private final ConcurrentMap<String, MemberInstance> resolvedMembers = new ConcurrentHashMap<String, MemberInstance>();
    final Set<ClassInstance> parents = new HashSet<ClassInstance>();
    final Set<ClassInstance> children = new HashSet<ClassInstance>();
    private String name;
    private int classVersion;
    private int mrjVersion;
    private String superName;
    private String signature;
    private int access;
    private String[] interfaces;

    ClassInstance(TinyRemapper tr, boolean isInput, InputTag[] inputTags, Path srcFile, byte[] data) {
        assert (!isInput || data != null);
        this.tr = tr;
        this.isInput = isInput;
        this.inputTags = inputTags;
        this.srcPath = srcFile;
        this.data = data;
        this.mrjOrigin = this;
    }

    void init(String name, int classVersion, int mrjVersion, String signature, String superName, int access, String[] interfaces) {
        this.name = name;
        this.classVersion = classVersion;
        this.mrjVersion = mrjVersion;
        this.superName = superName;
        this.signature = signature;
        this.access = access;
        this.interfaces = interfaces;
    }

    void setContext(TinyRemapper.MrjState context) {
        this.context = context;
    }

    TinyRemapper.MrjState getContext() {
        return this.context;
    }

    MemberInstance addMember(MemberInstance member) {
        return this.members.put(member.getId(), member);
    }

    void addInputTags(InputTag[] tags) {
        InputTag[] newTags;
        InputTag[] oldTags;
        if (tags == null || tags.length == 0) {
            return;
        }
        do {
            boolean found;
            if ((oldTags = this.inputTags) == null) {
                newTags = tags;
                continue;
            }
            int missingTags = 0;
            for (InputTag newTag : tags) {
                found = false;
                for (InputTag oldTag : oldTags) {
                    if (newTag != oldTag) continue;
                    found = true;
                    break;
                }
                if (found) continue;
                ++missingTags;
            }
            if (missingTags == 0) {
                return;
            }
            newTags = Arrays.copyOf(oldTags, oldTags.length + missingTags);
            for (InputTag newTag : tags) {
                found = false;
                for (InputTag oldTag : oldTags) {
                    if (newTag != oldTag) continue;
                    found = true;
                    break;
                }
                if (found) continue;
                newTags[newTags.length - missingTags] = newTag;
                --missingTags;
            }
            assert (missingTags == 0);
        } while (!inputTagsUpdater.compareAndSet(this, oldTags, newTags));
    }

    InputTag[] getInputTags() {
        return this.inputTags;
    }

    boolean hasAnyInputTag(InputTag[] reqTags) {
        InputTag[] availTags = this.inputTags;
        if (availTags == null) {
            return true;
        }
        for (InputTag reqTag : reqTags) {
            for (InputTag availTag : availTags) {
                if (availTag != reqTag) continue;
                return true;
            }
        }
        return false;
    }

    @Override
    public TrEnvironment getEnvironment() {
        return this.context;
    }

    @Override
    public int getAccess() {
        return this.access;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public int getClassVersion() {
        return this.classVersion;
    }

    public int getMrjVersion() {
        return this.mrjVersion;
    }

    public String getSuperName() {
        return this.superName;
    }

    public ClassInstance getSuperClass() {
        for (ClassInstance cls : this.parents) {
            if (cls.isInterface()) continue;
            return cls;
        }
        return null;
    }

    String[] getInterfaceNames0() {
        return this.interfaces;
    }

    public boolean isPublicOrPrivate() {
        return (this.access & 3) != 0;
    }

    public boolean isMrjCopy() {
        return this.mrjOrigin != this;
    }

    public ClassInstance getMrjOrigin() {
        return this.mrjOrigin;
    }

    void propagate(TrMember.MemberType type, String originatingCls, String idSrc, String nameDst, TinyRemapper.Direction dir, boolean isVirtual, boolean fromBridge, boolean first, Set<ClassInstance> visitedUp, Set<ClassInstance> visitedDown) {
        MemberInstance member = this.getMember(type, idSrc);
        if (member != null) {
            if (!first && !isVirtual) {
                return;
            }
            if (first || (member.access & 0xA) == 0 || this.tr.propagatePrivate || !this.tr.forcePropagation.isEmpty() && this.tr.forcePropagation.contains(this.name.replace('/', '.') + "." + member.name)) {
                if (!member.setNewName(nameDst, fromBridge)) {
                    this.tr.conflicts.computeIfAbsent(member, x -> Collections.newSetFromMap(new ConcurrentHashMap())).add(originatingCls + "/" + nameDst);
                } else {
                    member.newNameOriginatingCls = originatingCls;
                }
            }
            if (first && ((member.access & 2) != 0 || type == TrMember.MemberType.METHOD && this.isInterface() && !isVirtual)) {
                return;
            }
            if (this.tr.propagateBridges != TinyRemapper.LinkedMethodPropagation.DISABLED && member.cls.isInput && isVirtual && (member.access & 0x40) != 0) {
                assert (member.type == TrMember.MemberType.METHOD);
                MemberInstance bridgeTarget = BridgeHandler.getTarget(member);
                if (bridgeTarget != null) {
                    Set<ClassInstance> visitedUpBridge = Collections.newSetFromMap(new IdentityHashMap());
                    Set<ClassInstance> visitedDownBridge = Collections.newSetFromMap(new IdentityHashMap());
                    visitedUpBridge.add(member.cls);
                    visitedDownBridge.add(member.cls);
                    this.propagate(TrMember.MemberType.METHOD, originatingCls, bridgeTarget.getId(), nameDst, TinyRemapper.Direction.DOWN, true, this.tr.propagateBridges == TinyRemapper.LinkedMethodPropagation.COMPATIBLE, false, visitedUpBridge, visitedDownBridge);
                }
            }
        } else assert (!(first || type != TrMember.MemberType.FIELD && this.isInterface() && !isVirtual));
        assert (isVirtual || dir == TinyRemapper.Direction.DOWN);
        if (dir == TinyRemapper.Direction.ANY || dir == TinyRemapper.Direction.UP || isVirtual && member != null && (member.access & 0xA) == 0) {
            for (ClassInstance node : this.parents) {
                if (!visitedUp.add(node)) continue;
                node.propagate(type, originatingCls, idSrc, nameDst, TinyRemapper.Direction.UP, isVirtual, fromBridge, false, visitedUp, visitedDown);
            }
        }
        if (dir == TinyRemapper.Direction.ANY || dir == TinyRemapper.Direction.DOWN || isVirtual && member != null && (member.access & 0xA) == 0) {
            for (ClassInstance node : this.children) {
                if (!visitedDown.add(node)) continue;
                node.propagate(type, originatingCls, idSrc, nameDst, TinyRemapper.Direction.DOWN, isVirtual, fromBridge, false, visitedUp, visitedDown);
            }
        }
    }

    @Override
    public boolean isAssignableFrom(TrClass cls) {
        return cls instanceof ClassInstance && this.isAssignableFrom((ClassInstance)cls);
    }

    public boolean isAssignableFrom(ClassInstance cls) {
        if (cls == this) {
            return true;
        }
        if (this.isInterface()) {
            Set visited = Collections.newSetFromMap(new IdentityHashMap());
            ArrayDeque<ClassInstance> queue = new ArrayDeque<ClassInstance>();
            visited.add(cls);
            do {
                for (ClassInstance parent : cls.parents) {
                    if (parent == this) {
                        return true;
                    }
                    if (!visited.add(parent)) continue;
                    queue.addLast(parent);
                }
            } while ((cls = (ClassInstance)queue.pollFirst()) != null);
        } else {
            ClassInstance superCls;
            block2: do {
                superCls = null;
                for (ClassInstance c : cls.parents) {
                    if (c.isInterface()) continue;
                    if (c == this) {
                        return true;
                    }
                    superCls = c;
                    continue block2;
                }
            } while ((cls = superCls) != null);
        }
        return false;
    }

    static boolean isAssignableFrom(String superDesc, int superDescStart, String subDesc, int subDescStart, TinyRemapper.MrjState context) {
        int subDescEnd;
        char superType = superDesc.charAt(superDescStart);
        char subType = subDesc.charAt(subDescStart);
        if (superType == '[') {
            do {
                if (subType != '[') {
                    return false;
                }
                superType = superDesc.charAt(++superDescStart);
                subType = subDesc.charAt(++subDescStart);
            } while (superType == '[');
            return superType == subType && (superType != 'L' || superDesc.regionMatches(superDescStart + 1, subDesc, subDescStart + 1, superDesc.indexOf(59, superDescStart + 1) + 1));
        }
        if (superType != 'L') {
            return superType == subType;
        }
        if (subType != 'L' && subType != '[') {
            return false;
        }
        ++subDescStart;
        if (superDesc.startsWith("java/lang/Object;", ++superDescStart)) {
            return true;
        }
        if (subType != 'L') {
            return false;
        }
        int superDescEnd = superDesc.indexOf(59, superDescStart);
        int superDescLen = superDescEnd - superDescStart;
        if (superDescLen == (subDescEnd = subDesc.indexOf(59, subDescStart)) - subDescStart && superDesc.regionMatches(superDescStart, subDesc, subDescStart, superDescLen)) {
            return true;
        }
        String superName = superDesc.substring(superDescStart, superDescEnd);
        String subName = subDesc.substring(subDescStart, subDescEnd);
        ClassInstance superCls = context.getClass(superName);
        if (superCls != null && superCls.children.isEmpty()) {
            return false;
        }
        ClassInstance subCls = context.getClass(subName);
        if (subCls != null) {
            if (superCls == null || superCls.isInterface()) {
                Set visited = Collections.newSetFromMap(new IdentityHashMap());
                ArrayDeque<ClassInstance> queue = new ArrayDeque<ClassInstance>();
                visited.add(subCls);
                do {
                    for (ClassInstance parent : subCls.parents) {
                        if (parent.name.equals(superName)) {
                            return true;
                        }
                        if (!visited.add(parent)) continue;
                        queue.addLast(parent);
                    }
                } while ((subCls = (ClassInstance)queue.pollFirst()) != null);
            } else {
                String curSuperName;
                do {
                    if ((curSuperName = subCls.superName).equals(superName)) {
                        return true;
                    }
                    if (!curSuperName.equals("java/lang/Object")) continue;
                    return false;
                } while ((subCls = context.getClass(curSuperName)) != null);
            }
        } else if (superCls != null) {
            Set visited = Collections.newSetFromMap(new IdentityHashMap());
            ArrayDeque<ClassInstance> queue = new ArrayDeque<ClassInstance>();
            visited.add(superCls);
            do {
                for (ClassInstance child : superCls.children) {
                    if (child.name.equals(subName)) {
                        return true;
                    }
                    if (!visited.add(child)) continue;
                    queue.addLast(child);
                }
            } while ((superCls = (ClassInstance)queue.pollFirst()) != null);
        }
        return false;
    }

    public MemberInstance getMember(TrMember.MemberType type, String id) {
        return this.members.get(id);
    }

    @Override
    public Collection<? extends TrMethod> getMethods() {
        ArrayList<MemberInstance> ret = new ArrayList<MemberInstance>(this.members.size());
        for (MemberInstance m : this.members.values()) {
            if (!m.isMethod()) continue;
            ret.add(m);
        }
        return ret;
    }

    public Collection<MemberInstance> getMembers() {
        return this.members.values();
    }

    @Override
    public Collection<TrField> getFields(String name, String desc, boolean isDescPrefix, Predicate<TrField> filter, Collection<TrField> out) {
        if (out == null) {
            out = new ArrayList<TrField>(this.members.size());
        }
        for (MemberInstance m : this.members.values()) {
            if (!m.isField() || !ClassInstance.matches(m, name, desc, isDescPrefix, filter)) continue;
            out.add(m);
        }
        return out;
    }

    @Override
    public Collection<TrMethod> getMethods(String name, String desc, boolean isDescPrefix, Predicate<TrMethod> filter, Collection<TrMethod> out) {
        if (out == null) {
            out = new ArrayList<TrMethod>(this.members.size());
        }
        for (MemberInstance m : this.members.values()) {
            if (!m.isMethod() || !ClassInstance.matches(m, name, desc, isDescPrefix, filter)) continue;
            out.add(m);
        }
        return out;
    }

    public MemberInstance resolve(TrMember.MemberType type, String id) {
        MemberInstance member = this.getMember(type, id);
        if (member != null) {
            return member;
        }
        member = (MemberInstance)this.resolvedMembers.get(id);
        if (member == null) {
            MemberInstance memberInstance = member = type == TrMember.MemberType.FIELD ? this.resolveField(id) : this.resolveMethod(id);
            assert (member != null);
            MemberInstance prev = this.resolvedMembers.putIfAbsent(id, member);
            if (prev != null) {
                member = prev;
            }
        }
        return member != nullMember ? member : null;
    }

    private MemberInstance resolveField(String id) {
        block4: {
            MemberInstance parentMember;
            ArrayDeque<ClassInstance> queue = new ArrayDeque<ClassInstance>();
            Set visited = Collections.newSetFromMap(new IdentityHashMap());
            visited.add(this);
            ClassInstance context = this;
            do {
                ClassInstance cls = context;
                do {
                    for (ClassInstance parent : cls.parents) {
                        if (!parent.isInterface() || !visited.add(parent)) continue;
                        MemberInstance ret = parent.getMember(TrMember.MemberType.FIELD, id);
                        if (ret != null) {
                            return ret;
                        }
                        queue.addLast(parent);
                    }
                } while ((cls = (ClassInstance)queue.pollLast()) != null);
                cls = context;
                if ((context = cls.getSuperClass()) == null) break block4;
            } while ((parentMember = context.getMember(TrMember.MemberType.FIELD, id)) == null);
            return parentMember;
        }
        return nullMember;
    }

    @Override
    public Collection<TrField> resolveFields(String name, String desc, boolean isDescPrefix, Predicate<TrField> filter, Collection<TrField> out) {
        if (name != null && (desc != null && !isDescPrefix || this.tr.ignoreFieldDesc)) {
            MemberInstance ret = this.resolve(TrMember.MemberType.FIELD, MemberInstance.getFieldId(name, desc, this.tr.ignoreFieldDesc));
            if (ret != null && filter != null && !filter.test(ret)) {
                ret = null;
            }
            if (out == null) {
                return ret == null || filter != null ? Collections.emptyList() : Collections.singletonList(ret);
            }
            if (ret != null) {
                out.add(ret);
            }
            return out;
        }
        if (out == null) {
            out = new ArrayList<TrField>();
        }
        for (MemberInstance member : this.getMembers()) {
            if (!member.isField()) continue;
            ClassInstance.addMatching(member, name, desc, isDescPrefix, filter, out);
        }
        ArrayDeque<ClassInstance> queue = new ArrayDeque<ClassInstance>();
        Set visited = Collections.newSetFromMap(new IdentityHashMap());
        visited.add(this);
        ClassInstance context = this;
        block1: while (true) {
            ClassInstance cls = context;
            do {
                for (ClassInstance parent : cls.parents) {
                    if (!parent.isInterface() || !visited.add(parent)) continue;
                    for (MemberInstance member : parent.getMembers()) {
                        if (!member.isField()) continue;
                        ClassInstance.addMatching(member, name, desc, isDescPrefix, filter, out);
                    }
                    queue.addLast(parent);
                }
            } while ((cls = (ClassInstance)queue.pollLast()) != null);
            cls = context;
            if ((context = cls.getSuperClass()) == null) break;
            Iterator<Object> iterator = context.getMembers().iterator();
            while (true) {
                if (!iterator.hasNext()) continue block1;
                MemberInstance member = (MemberInstance)iterator.next();
                if (!member.isField()) continue;
                ClassInstance.addMatching(member, name, desc, isDescPrefix, filter, out);
            }
            break;
        }
        return out;
    }

    private MemberInstance resolveMethod(String id) {
        ClassInstance cls = this;
        while ((cls = cls.getSuperClass()) != null) {
            MemberInstance ret = cls.getMember(TrMember.MemberType.METHOD, id);
            if (ret == null) continue;
            return ret;
        }
        ArrayDeque<ClassInstance> queue = new ArrayDeque<ClassInstance>();
        Set visited = Collections.newSetFromMap(new IdentityHashMap());
        visited.add(this);
        ArrayList<MemberInstance> matchedMethods = new ArrayList<MemberInstance>();
        boolean hasNonAbstract = false;
        cls = this;
        do {
            for (ClassInstance parent : cls.parents) {
                MemberInstance parentMember;
                if (!visited.add(parent)) continue;
                if (parent.isInterface() && (parentMember = parent.getMember(TrMember.MemberType.METHOD, id)) != null && parentMember.isVirtual()) {
                    if (!parentMember.isAbstract()) {
                        hasNonAbstract = true;
                    }
                    matchedMethods.add(parentMember);
                    continue;
                }
                queue.addLast(parent);
            }
        } while ((cls = (ClassInstance)queue.pollFirst()) != null);
        if (hasNonAbstract && matchedMethods.size() > 1) {
            block3: for (MemberInstance member : matchedMethods) {
                if (member.isAbstract()) continue;
                for (MemberInstance m : matchedMethods) {
                    if (m == member || !member.cls.isAssignableFrom(m.cls)) continue;
                    continue block3;
                }
                return member;
            }
        }
        if (!matchedMethods.isEmpty()) {
            return (MemberInstance)matchedMethods.get(0);
        }
        return nullMember;
    }

    @Override
    public Collection<TrMethod> resolveMethods(String name, String desc, boolean isDescPrefix, Predicate<TrMethod> filter, Collection<TrMethod> out) {
        if (name != null && desc != null && !isDescPrefix) {
            MemberInstance ret = this.resolve(TrMember.MemberType.METHOD, MemberInstance.getMethodId(name, desc));
            if (ret != null && filter != null && !filter.test(ret)) {
                ret = null;
            }
            if (out == null) {
                return ret == null || filter != null ? Collections.emptyList() : Collections.singletonList(ret);
            }
            if (ret != null) {
                out.add(ret);
            }
            return out;
        }
        if (out == null) {
            out = new ArrayList<TrMethod>();
        }
        for (MemberInstance memberInstance : this.getMembers()) {
            if (!memberInstance.isMethod()) continue;
            ClassInstance.addMatching(memberInstance, name, desc, isDescPrefix, filter, out);
        }
        ClassInstance cls = this;
        while ((cls = cls.getSuperClass()) != null) {
            for (MemberInstance member : cls.getMembers()) {
                if (!member.isMethod()) continue;
                ClassInstance.addMatching(member, name, desc, isDescPrefix, filter, out);
            }
        }
        ArrayDeque<ClassInstance> arrayDeque = new ArrayDeque<ClassInstance>();
        Set visited = Collections.newSetFromMap(new IdentityHashMap());
        visited.add(this);
        HashMap<String, List> matchedMethodsMap = new HashMap<String, List>();
        boolean hasNonAbstract = false;
        cls = this;
        do {
            for (ClassInstance parent : cls.parents) {
                if (!visited.add(parent)) continue;
                if (parent.isInterface()) {
                    for (MemberInstance memberInstance : parent.getMembers()) {
                        if (!memberInstance.isMethod() || !ClassInstance.matches(memberInstance, name, desc, isDescPrefix, filter) || !ClassInstance.addUnique(memberInstance, matchedMethodsMap.computeIfAbsent(memberInstance.getId(), ignore -> new ArrayList())) || memberInstance.isAbstract()) continue;
                        hasNonAbstract = true;
                    }
                }
                arrayDeque.addLast(parent);
            }
        } while ((cls = (ClassInstance)arrayDeque.pollFirst()) != null);
        block6: for (List matchedMethods : matchedMethodsMap.values()) {
            if (matchedMethods.isEmpty()) continue;
            if (hasNonAbstract && matchedMethods.size() > 1) {
                block7: for (TrMethod trMethod : matchedMethods) {
                    if (trMethod.isAbstract()) continue;
                    for (TrMember m : matchedMethods) {
                        if (m == trMethod || !trMethod.getOwner().isAssignableFrom(m.getOwner())) continue;
                        continue block7;
                    }
                    ClassInstance.addUnique(trMethod, out);
                    continue block6;
                }
            }
            ClassInstance.addUnique((TrMethod)matchedMethods.get(0), out);
        }
        return out;
    }

    private static <T extends TrMember> boolean matches(T member, String name, String desc, boolean isDescPrefix, Predicate<T> filter) {
        return (name == null || name.equals(member.getName())) && (desc == null || !isDescPrefix && member.getDesc().equals(desc) || isDescPrefix && member.getDesc().startsWith(desc)) && (filter == null || filter.test(member));
    }

    private static <T extends TrMember> boolean addUnique(T member, Collection<T> out) {
        for (TrMember m : out) {
            if (!m.getName().equals(member.getName()) || !m.getDesc().equals(member.getDesc())) continue;
            return false;
        }
        out.add(member);
        return true;
    }

    private static <T extends TrMember> void addMatching(T member, String name, String desc, boolean isDescPrefix, Predicate<T> filter, Collection<T> out) {
        if (ClassInstance.matches(member, name, desc, isDescPrefix, filter)) {
            ClassInstance.addUnique(member, out);
        }
    }

    ClassInstance constructMrjCopy(TinyRemapper.MrjState newContext) {
        ClassInstance copy = new ClassInstance(this.tr, false, this.inputTags, this.srcPath, this.data);
        copy.init(this.name, this.classVersion, this.mrjVersion, this.signature, this.superName, this.access, this.interfaces);
        copy.setContext(newContext);
        for (MemberInstance member : this.members.values()) {
            copy.addMember(new MemberInstance(member.type, copy, member.name, member.desc, member.access, member.index));
        }
        copy.mrjOrigin = this.mrjOrigin;
        return copy;
    }

    @Override
    public boolean isInput() {
        return this.isInput;
    }

    public String toString() {
        return this.name;
    }

    public static String getMrjName(String clsName, int mrjVersion) {
        if (mrjVersion != -1) {
            return "/META-INF/versions/" + mrjVersion + "/" + clsName;
        }
        return clsName;
    }
}

