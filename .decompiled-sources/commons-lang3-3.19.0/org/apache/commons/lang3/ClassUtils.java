/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class ClassUtils {
    private static final int MAX_DIMENSIONS = 255;
    private static final Comparator<Class<?>> COMPARATOR = (o1, o2) -> Objects.compare(ClassUtils.getName(o1), ClassUtils.getName(o2), String::compareTo);
    public static final char PACKAGE_SEPARATOR_CHAR = '.';
    public static final String PACKAGE_SEPARATOR = String.valueOf('.');
    public static final char INNER_CLASS_SEPARATOR_CHAR = '$';
    public static final String INNER_CLASS_SEPARATOR = String.valueOf('$');
    private static final Map<String, Class<?>> NAME_PRIMITIVE_MAP = new HashMap();
    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_MAP;
    private static final Map<Class<?>, Class<?>> WRAPPER_PRIMITIVE_MAP;
    private static final Map<String, String> ABBREVIATION_MAP;
    private static final Map<String, String> REVERSE_ABBREVIATION_MAP;

    public static Comparator<Class<?>> comparator() {
        return COMPARATOR;
    }

    public static List<String> convertClassesToClassNames(List<Class<?>> classes) {
        return classes == null ? null : classes.stream().map(e -> ClassUtils.getName(e, null)).collect(Collectors.toList());
    }

    public static List<Class<?>> convertClassNamesToClasses(List<String> classNames) {
        if (classNames == null) {
            return null;
        }
        ArrayList classes = new ArrayList(classNames.size());
        classNames.forEach(className -> {
            try {
                classes.add(Class.forName(className));
            }
            catch (Exception ex) {
                classes.add(null);
            }
        });
        return classes;
    }

    public static String getAbbreviatedName(Class<?> cls, int lengthHint) {
        if (cls == null) {
            return "";
        }
        return ClassUtils.getAbbreviatedName(cls.getName(), lengthHint);
    }

    public static String getAbbreviatedName(String className, int lengthHint) {
        if (lengthHint <= 0) {
            throw new IllegalArgumentException("len must be > 0");
        }
        if (className == null) {
            return "";
        }
        if (className.length() <= lengthHint) {
            return className;
        }
        char[] abbreviated = className.toCharArray();
        int target = 0;
        int source = 0;
        while (source < abbreviated.length) {
            int runAheadTarget = target;
            while (source < abbreviated.length && abbreviated[source] != '.') {
                abbreviated[runAheadTarget++] = abbreviated[source++];
            }
            if (ClassUtils.useFull(runAheadTarget, source, abbreviated.length, lengthHint) || ++target > runAheadTarget) {
                target = runAheadTarget;
            }
            if (source >= abbreviated.length) continue;
            abbreviated[target++] = abbreviated[source++];
        }
        return new String(abbreviated, 0, target);
    }

    public static List<Class<?>> getAllInterfaces(Class<?> cls) {
        if (cls == null) {
            return null;
        }
        LinkedHashSet interfacesFound = new LinkedHashSet();
        ClassUtils.getAllInterfaces(cls, interfacesFound);
        return new ArrayList(interfacesFound);
    }

    private static void getAllInterfaces(Class<?> cls, HashSet<Class<?>> interfacesFound) {
        while (cls != null) {
            Class<?>[] interfaces;
            for (Class<?> i : interfaces = cls.getInterfaces()) {
                if (!interfacesFound.add(i)) continue;
                ClassUtils.getAllInterfaces(i, interfacesFound);
            }
            cls = cls.getSuperclass();
        }
    }

    public static List<Class<?>> getAllSuperclasses(Class<?> cls) {
        if (cls == null) {
            return null;
        }
        ArrayList classes = new ArrayList();
        for (Class<?> superclass = cls.getSuperclass(); superclass != null; superclass = superclass.getSuperclass()) {
            classes.add(superclass);
        }
        return classes;
    }

    public static String getCanonicalName(Class<?> cls) {
        return ClassUtils.getCanonicalName(cls, "");
    }

    public static String getCanonicalName(Class<?> cls, String valueIfNull) {
        if (cls == null) {
            return valueIfNull;
        }
        String canonicalName = cls.getCanonicalName();
        return canonicalName == null ? valueIfNull : canonicalName;
    }

    public static String getCanonicalName(Object object) {
        return ClassUtils.getCanonicalName(object, "");
    }

    public static String getCanonicalName(Object object, String valueIfNull) {
        if (object == null) {
            return valueIfNull;
        }
        String canonicalName = object.getClass().getCanonicalName();
        return canonicalName == null ? valueIfNull : canonicalName;
    }

    private static String getCanonicalName(String name) {
        String className = StringUtils.deleteWhitespace(name);
        if (className == null) {
            return null;
        }
        int dim = 0;
        int len = className.length();
        while (dim < len && className.charAt(dim) == '[') {
            if (++dim <= 255) continue;
            throw new IllegalArgumentException(String.format("Maximum array dimension %d exceeded", 255));
        }
        if (dim >= len) {
            throw new IllegalArgumentException(String.format("Invalid class name %s", name));
        }
        if (dim < 1) {
            return className;
        }
        if ((className = className.substring(dim)).startsWith("L")) {
            if (!className.endsWith(";") || className.length() < 3) {
                throw new IllegalArgumentException(String.format("Invalid class name %s", name));
            }
            className = className.substring(1, className.length() - 1);
        } else if (className.length() == 1) {
            String primitive = REVERSE_ABBREVIATION_MAP.get(className.substring(0, 1));
            if (primitive == null) {
                throw new IllegalArgumentException(String.format("Invalid class name %s", name));
            }
            className = primitive;
        } else {
            throw new IllegalArgumentException(String.format("Invalid class name %s", name));
        }
        StringBuilder canonicalClassNameBuffer = new StringBuilder(className.length() + dim * 2);
        canonicalClassNameBuffer.append(className);
        for (int i = 0; i < dim; ++i) {
            canonicalClassNameBuffer.append("[]");
        }
        return canonicalClassNameBuffer.toString();
    }

    public static Class<?> getClass(ClassLoader classLoader, String className) throws ClassNotFoundException {
        return ClassUtils.getClass(classLoader, className, true);
    }

    public static Class<?> getClass(ClassLoader classLoader, String className, boolean initialize) throws ClassNotFoundException {
        String next = className;
        int lastDotIndex = -1;
        while (true) {
            try {
                Class<?> clazz = ClassUtils.getPrimitiveClass(next);
                return clazz != null ? clazz : Class.forName(ClassUtils.toCanonicalName(next), initialize, classLoader);
            }
            catch (ClassNotFoundException ex) {
                lastDotIndex = next.lastIndexOf(46);
                if (lastDotIndex == -1) continue;
                next = next.substring(0, lastDotIndex) + '$' + next.substring(lastDotIndex + 1);
                if (lastDotIndex != -1) continue;
                throw new ClassNotFoundException(next);
            }
            break;
        }
    }

    public static Class<?> getClass(String className) throws ClassNotFoundException {
        return ClassUtils.getClass(className, true);
    }

    public static Class<?> getClass(String className, boolean initialize) throws ClassNotFoundException {
        ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
        ClassLoader loader = contextCL == null ? ClassUtils.class.getClassLoader() : contextCL;
        return ClassUtils.getClass(loader, className, initialize);
    }

    public static <T> Class<T> getComponentType(Class<T[]> cls) {
        return cls == null ? null : cls.getComponentType();
    }

    public static String getName(Class<?> cls) {
        return ClassUtils.getName(cls, "");
    }

    public static String getName(Class<?> cls, String valueIfNull) {
        return ClassUtils.getName(cls, valueIfNull, false);
    }

    static String getName(Class<?> cls, String valueIfNull, boolean simple) {
        return cls == null ? valueIfNull : (simple ? cls.getSimpleName() : cls.getName());
    }

    public static String getName(Object object) {
        return ClassUtils.getName(object, "");
    }

    public static String getName(Object object, String valueIfNull) {
        return object == null ? valueIfNull : object.getClass().getName();
    }

    public static String getPackageCanonicalName(Class<?> cls) {
        if (cls == null) {
            return "";
        }
        return ClassUtils.getPackageCanonicalName(cls.getName());
    }

    public static String getPackageCanonicalName(Object object, String valueIfNull) {
        if (object == null) {
            return valueIfNull;
        }
        return ClassUtils.getPackageCanonicalName(object.getClass().getName());
    }

    public static String getPackageCanonicalName(String name) {
        return ClassUtils.getPackageName(ClassUtils.getCanonicalName(name));
    }

    public static String getPackageName(Class<?> cls) {
        if (cls == null) {
            return "";
        }
        return ClassUtils.getPackageName(cls.getName());
    }

    public static String getPackageName(Object object, String valueIfNull) {
        if (object == null) {
            return valueIfNull;
        }
        return ClassUtils.getPackageName(object.getClass());
    }

    public static String getPackageName(String className) {
        if (StringUtils.isEmpty(className)) {
            return "";
        }
        int i = 0;
        while (className.charAt(i) == '[') {
            ++i;
        }
        if ((className = className.substring(i)).charAt(0) == 'L' && className.charAt(className.length() - 1) == ';') {
            className = className.substring(1);
        }
        if ((i = className.lastIndexOf(46)) == -1) {
            return "";
        }
        return className.substring(0, i);
    }

    static Class<?> getPrimitiveClass(String className) {
        return NAME_PRIMITIVE_MAP.get(className);
    }

    public static Method getPublicMethod(Class<?> cls, String methodName, Class<?> ... parameterTypes) throws NoSuchMethodException {
        Method declaredMethod = cls.getMethod(methodName, parameterTypes);
        if (ClassUtils.isPublic(declaredMethod.getDeclaringClass())) {
            return declaredMethod;
        }
        ArrayList candidateClasses = new ArrayList(ClassUtils.getAllInterfaces(cls));
        candidateClasses.addAll(ClassUtils.getAllSuperclasses(cls));
        for (Class clazz : candidateClasses) {
            Method candidateMethod;
            if (!ClassUtils.isPublic(clazz)) continue;
            try {
                candidateMethod = clazz.getMethod(methodName, parameterTypes);
            }
            catch (NoSuchMethodException ex) {
                continue;
            }
            if (!Modifier.isPublic(candidateMethod.getDeclaringClass().getModifiers())) continue;
            return candidateMethod;
        }
        throw new NoSuchMethodException("Can't find a public method for " + methodName + " " + ArrayUtils.toString(parameterTypes));
    }

    public static String getShortCanonicalName(Class<?> cls) {
        return cls == null ? "" : ClassUtils.getShortCanonicalName(cls.getCanonicalName());
    }

    public static String getShortCanonicalName(Object object, String valueIfNull) {
        return object == null ? valueIfNull : ClassUtils.getShortCanonicalName(object.getClass());
    }

    public static String getShortCanonicalName(String canonicalName) {
        return ClassUtils.getShortClassName(ClassUtils.getCanonicalName(canonicalName));
    }

    public static String getShortClassName(Class<?> cls) {
        if (cls == null) {
            return "";
        }
        return ClassUtils.getShortClassName(cls.getName());
    }

    public static String getShortClassName(Object object, String valueIfNull) {
        if (object == null) {
            return valueIfNull;
        }
        return ClassUtils.getShortClassName(object.getClass());
    }

    public static String getShortClassName(String className) {
        int lastDotIdx;
        if (StringUtils.isEmpty(className)) {
            return "";
        }
        StringBuilder arrayPrefix = new StringBuilder();
        if (className.startsWith("[")) {
            while (className.charAt(0) == '[') {
                className = className.substring(1);
                arrayPrefix.append("[]");
            }
            if (className.charAt(0) == 'L' && className.charAt(className.length() - 1) == ';') {
                className = className.substring(1, className.length() - 1);
            }
            if (REVERSE_ABBREVIATION_MAP.containsKey(className)) {
                className = REVERSE_ABBREVIATION_MAP.get(className);
            }
        }
        int innerIdx = className.indexOf(36, (lastDotIdx = className.lastIndexOf(46)) == -1 ? 0 : lastDotIdx + 1);
        String out = className.substring(lastDotIdx + 1);
        if (innerIdx != -1) {
            out = out.replace('$', '.');
        }
        return out + arrayPrefix;
    }

    public static String getSimpleName(Class<?> cls) {
        return ClassUtils.getSimpleName(cls, "");
    }

    public static String getSimpleName(Class<?> cls, String valueIfNull) {
        return cls == null ? valueIfNull : cls.getSimpleName();
    }

    public static String getSimpleName(Object object) {
        return ClassUtils.getSimpleName(object, "");
    }

    public static String getSimpleName(Object object, String valueIfNull) {
        return object == null ? valueIfNull : object.getClass().getSimpleName();
    }

    public static Iterable<Class<?>> hierarchy(Class<?> type) {
        return ClassUtils.hierarchy(type, Interfaces.EXCLUDE);
    }

    public static Iterable<Class<?>> hierarchy(Class<?> type, Interfaces interfacesBehavior) {
        Iterable<Class<?>> classes = () -> {
            final AtomicReference<Class> next = new AtomicReference<Class>(type);
            return new Iterator<Class<?>>(){

                @Override
                public boolean hasNext() {
                    return next.get() != null;
                }

                @Override
                public Class<?> next() {
                    return (Class)next.getAndUpdate(Class::getSuperclass);
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        };
        if (interfacesBehavior != Interfaces.INCLUDE) {
            return classes;
        }
        return () -> {
            final HashSet seenInterfaces = new HashSet();
            final Iterator wrapped = classes.iterator();
            return new Iterator<Class<?>>(){
                Iterator<Class<?>> interfaces = Collections.emptyIterator();

                @Override
                public boolean hasNext() {
                    return this.interfaces.hasNext() || wrapped.hasNext();
                }

                @Override
                public Class<?> next() {
                    if (this.interfaces.hasNext()) {
                        Class<?> nextInterface = this.interfaces.next();
                        seenInterfaces.add(nextInterface);
                        return nextInterface;
                    }
                    Class nextSuperclass = (Class)wrapped.next();
                    LinkedHashSet currentInterfaces = new LinkedHashSet();
                    this.walkInterfaces(currentInterfaces, nextSuperclass);
                    this.interfaces = currentInterfaces.iterator();
                    return nextSuperclass;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }

                private void walkInterfaces(Set<Class<?>> addTo, Class<?> c) {
                    for (Class<?> iface : c.getInterfaces()) {
                        if (!seenInterfaces.contains(iface)) {
                            addTo.add(iface);
                        }
                        this.walkInterfaces(addTo, iface);
                    }
                }
            };
        };
    }

    public static boolean isAssignable(Class<?> cls, Class<?> toClass) {
        return ClassUtils.isAssignable(cls, toClass, true);
    }

    public static boolean isAssignable(Class<?> cls, Class<?> toClass, boolean autoboxing) {
        if (toClass == null) {
            return false;
        }
        if (cls == null) {
            return !toClass.isPrimitive();
        }
        if (autoboxing) {
            if (cls.isPrimitive() && !toClass.isPrimitive() && (cls = ClassUtils.primitiveToWrapper(cls)) == null) {
                return false;
            }
            if (toClass.isPrimitive() && !cls.isPrimitive() && (cls = ClassUtils.wrapperToPrimitive(cls)) == null) {
                return false;
            }
        }
        if (cls.equals(toClass)) {
            return true;
        }
        if (cls.isPrimitive()) {
            if (!toClass.isPrimitive()) {
                return false;
            }
            if (Integer.TYPE.equals(cls)) {
                return Long.TYPE.equals(toClass) || Float.TYPE.equals(toClass) || Double.TYPE.equals(toClass);
            }
            if (Long.TYPE.equals(cls)) {
                return Float.TYPE.equals(toClass) || Double.TYPE.equals(toClass);
            }
            if (Boolean.TYPE.equals(cls)) {
                return false;
            }
            if (Double.TYPE.equals(cls)) {
                return false;
            }
            if (Float.TYPE.equals(cls)) {
                return Double.TYPE.equals(toClass);
            }
            if (Character.TYPE.equals(cls) || Short.TYPE.equals(cls)) {
                return Integer.TYPE.equals(toClass) || Long.TYPE.equals(toClass) || Float.TYPE.equals(toClass) || Double.TYPE.equals(toClass);
            }
            if (Byte.TYPE.equals(cls)) {
                return Short.TYPE.equals(toClass) || Integer.TYPE.equals(toClass) || Long.TYPE.equals(toClass) || Float.TYPE.equals(toClass) || Double.TYPE.equals(toClass);
            }
            return false;
        }
        return toClass.isAssignableFrom(cls);
    }

    public static boolean isAssignable(Class<?>[] classArray, Class<?> ... toClassArray) {
        return ClassUtils.isAssignable(classArray, toClassArray, true);
    }

    public static boolean isAssignable(Class<?>[] classArray, Class<?>[] toClassArray, boolean autoboxing) {
        if (!ArrayUtils.isSameLength(classArray, toClassArray)) {
            return false;
        }
        classArray = ArrayUtils.nullToEmpty(classArray);
        toClassArray = ArrayUtils.nullToEmpty(toClassArray);
        for (int i = 0; i < classArray.length; ++i) {
            if (ClassUtils.isAssignable(classArray[i], toClassArray[i], autoboxing)) continue;
            return false;
        }
        return true;
    }

    public static boolean isInnerClass(Class<?> cls) {
        return cls != null && cls.getEnclosingClass() != null;
    }

    public static boolean isPrimitiveOrWrapper(Class<?> type) {
        return type != null && type.isPrimitive() || ClassUtils.isPrimitiveWrapper(type);
    }

    public static boolean isPrimitiveWrapper(Class<?> type) {
        return WRAPPER_PRIMITIVE_MAP.containsKey(type);
    }

    public static boolean isPublic(Class<?> cls) {
        return Modifier.isPublic(cls.getModifiers());
    }

    public static Class<?>[] primitivesToWrappers(Class<?> ... classes) {
        if (classes == null) {
            return null;
        }
        if (classes.length == 0) {
            return classes;
        }
        return ArrayUtils.setAll(new Class[classes.length], i -> ClassUtils.primitiveToWrapper(classes[i]));
    }

    public static Class<?> primitiveToWrapper(Class<?> cls) {
        return cls != null && cls.isPrimitive() ? PRIMITIVE_WRAPPER_MAP.get(cls) : cls;
    }

    private static String toCanonicalName(String className) {
        String canonicalName = StringUtils.deleteWhitespace(className);
        Objects.requireNonNull(canonicalName, "className");
        String arrayMarker = "[]";
        if (canonicalName.endsWith("[]")) {
            StringBuilder classNameBuffer = new StringBuilder();
            while (canonicalName.endsWith("[]")) {
                canonicalName = canonicalName.substring(0, canonicalName.length() - 2);
                classNameBuffer.append("[");
            }
            String abbreviation = ABBREVIATION_MAP.get(canonicalName);
            if (abbreviation != null) {
                classNameBuffer.append(abbreviation);
            } else {
                classNameBuffer.append("L").append(canonicalName).append(";");
            }
            canonicalName = classNameBuffer.toString();
        }
        return canonicalName;
    }

    public static Class<?>[] toClass(Object ... array) {
        if (array == null) {
            return null;
        }
        if (array.length == 0) {
            return ArrayUtils.EMPTY_CLASS_ARRAY;
        }
        return ArrayUtils.setAll(new Class[array.length], i -> array[i] == null ? null : array[i].getClass());
    }

    private static boolean useFull(int runAheadTarget, int source, int originalLength, int desiredLength) {
        return source >= originalLength || runAheadTarget + originalLength - source <= desiredLength;
    }

    public static Class<?>[] wrappersToPrimitives(Class<?> ... classes) {
        if (classes == null) {
            return null;
        }
        if (classes.length == 0) {
            return classes;
        }
        return ArrayUtils.setAll(new Class[classes.length], i -> ClassUtils.wrapperToPrimitive(classes[i]));
    }

    public static Class<?> wrapperToPrimitive(Class<?> cls) {
        return WRAPPER_PRIMITIVE_MAP.get(cls);
    }

    @Deprecated
    public ClassUtils() {
    }

    static {
        NAME_PRIMITIVE_MAP.put(Boolean.TYPE.getName(), Boolean.TYPE);
        NAME_PRIMITIVE_MAP.put(Byte.TYPE.getName(), Byte.TYPE);
        NAME_PRIMITIVE_MAP.put(Character.TYPE.getName(), Character.TYPE);
        NAME_PRIMITIVE_MAP.put(Double.TYPE.getName(), Double.TYPE);
        NAME_PRIMITIVE_MAP.put(Float.TYPE.getName(), Float.TYPE);
        NAME_PRIMITIVE_MAP.put(Integer.TYPE.getName(), Integer.TYPE);
        NAME_PRIMITIVE_MAP.put(Long.TYPE.getName(), Long.TYPE);
        NAME_PRIMITIVE_MAP.put(Short.TYPE.getName(), Short.TYPE);
        NAME_PRIMITIVE_MAP.put(Void.TYPE.getName(), Void.TYPE);
        PRIMITIVE_WRAPPER_MAP = new HashMap();
        PRIMITIVE_WRAPPER_MAP.put(Boolean.TYPE, Boolean.class);
        PRIMITIVE_WRAPPER_MAP.put(Byte.TYPE, Byte.class);
        PRIMITIVE_WRAPPER_MAP.put(Character.TYPE, Character.class);
        PRIMITIVE_WRAPPER_MAP.put(Short.TYPE, Short.class);
        PRIMITIVE_WRAPPER_MAP.put(Integer.TYPE, Integer.class);
        PRIMITIVE_WRAPPER_MAP.put(Long.TYPE, Long.class);
        PRIMITIVE_WRAPPER_MAP.put(Double.TYPE, Double.class);
        PRIMITIVE_WRAPPER_MAP.put(Float.TYPE, Float.class);
        PRIMITIVE_WRAPPER_MAP.put(Void.TYPE, Void.TYPE);
        WRAPPER_PRIMITIVE_MAP = new HashMap();
        PRIMITIVE_WRAPPER_MAP.forEach((primitiveClass, wrapperClass) -> {
            if (!primitiveClass.equals(wrapperClass)) {
                WRAPPER_PRIMITIVE_MAP.put((Class<?>)wrapperClass, (Class<?>)primitiveClass);
            }
        });
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Integer.TYPE.getName(), "I");
        map.put(Boolean.TYPE.getName(), "Z");
        map.put(Float.TYPE.getName(), "F");
        map.put(Long.TYPE.getName(), "J");
        map.put(Short.TYPE.getName(), "S");
        map.put(Byte.TYPE.getName(), "B");
        map.put(Double.TYPE.getName(), "D");
        map.put(Character.TYPE.getName(), "C");
        ABBREVIATION_MAP = Collections.unmodifiableMap(map);
        REVERSE_ABBREVIATION_MAP = Collections.unmodifiableMap(map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey)));
    }

    public static enum Interfaces {
        INCLUDE,
        EXCLUDE;

    }
}

