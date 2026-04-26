/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.AccessibleObjects;
import org.apache.commons.lang3.reflect.MemberUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.commons.lang3.stream.LangCollectors;

public class MethodUtils {
    private static final Comparator<Method> METHOD_BY_SIGNATURE = Comparator.comparing(Method::toString);

    private static int distance(Class<?>[] fromClassArray, Class<?>[] toClassArray) {
        int answer = 0;
        if (!ClassUtils.isAssignable(fromClassArray, toClassArray, true)) {
            return -1;
        }
        for (int offset = 0; offset < fromClassArray.length; ++offset) {
            Class<?> aClass = fromClassArray[offset];
            Class<?> toClass = toClassArray[offset];
            if (aClass == null || aClass.equals(toClass)) continue;
            if (ClassUtils.isAssignable(aClass, toClass, true) && !ClassUtils.isAssignable(aClass, toClass, false)) {
                ++answer;
                continue;
            }
            answer += 2;
        }
        return answer;
    }

    public static Method getAccessibleMethod(Class<?> cls, Method method) {
        Class<?>[] parameterTypes;
        if (!MemberUtils.isPublic(method)) {
            return null;
        }
        if (ClassUtils.isPublic(cls)) {
            return method;
        }
        String methodName = method.getName();
        Method method2 = MethodUtils.getAccessibleMethodFromInterfaceNest(cls, methodName, parameterTypes = method.getParameterTypes());
        return method2 != null ? method2 : MethodUtils.getAccessibleMethodFromSuperclass(cls, methodName, parameterTypes);
    }

    public static Method getAccessibleMethod(Class<?> cls, String methodName, Class<?> ... parameterTypes) {
        return MethodUtils.getAccessibleMethod(MethodUtils.getMethodObject(cls, methodName, parameterTypes));
    }

    public static Method getAccessibleMethod(Method method) {
        return method != null ? MethodUtils.getAccessibleMethod(method.getDeclaringClass(), method) : null;
    }

    private static Method getAccessibleMethodFromInterfaceNest(Class<?> cls, String methodName, Class<?> ... parameterTypes) {
        while (cls != null) {
            Class<?>[] interfaces;
            for (Class<?> anInterface : interfaces = cls.getInterfaces()) {
                if (!ClassUtils.isPublic(anInterface)) continue;
                try {
                    return anInterface.getDeclaredMethod(methodName, parameterTypes);
                }
                catch (NoSuchMethodException noSuchMethodException) {
                    Method method = MethodUtils.getAccessibleMethodFromInterfaceNest(anInterface, methodName, parameterTypes);
                    if (method == null) continue;
                    return method;
                }
            }
            cls = cls.getSuperclass();
        }
        return null;
    }

    private static Method getAccessibleMethodFromSuperclass(Class<?> cls, String methodName, Class<?> ... parameterTypes) {
        for (Class<?> parentClass = cls.getSuperclass(); parentClass != null; parentClass = parentClass.getSuperclass()) {
            if (!ClassUtils.isPublic(parentClass)) continue;
            return MethodUtils.getMethodObject(parentClass, methodName, parameterTypes);
        }
        return null;
    }

    private static List<Class<?>> getAllSuperclassesAndInterfaces(Class<?> cls) {
        if (cls == null) {
            return null;
        }
        ArrayList allSuperClassesAndInterfaces = new ArrayList();
        List<Class<?>> allSuperclasses = ClassUtils.getAllSuperclasses(cls);
        int superClassIndex = 0;
        List<Class<?>> allInterfaces = ClassUtils.getAllInterfaces(cls);
        int interfaceIndex = 0;
        while (interfaceIndex < allInterfaces.size() || superClassIndex < allSuperclasses.size()) {
            Class<?> acls = interfaceIndex >= allInterfaces.size() || superClassIndex < allSuperclasses.size() && superClassIndex < interfaceIndex ? allSuperclasses.get(superClassIndex++) : allInterfaces.get(interfaceIndex++);
            allSuperClassesAndInterfaces.add(acls);
        }
        return allSuperClassesAndInterfaces;
    }

    public static <A extends Annotation> A getAnnotation(Method method, Class<A> annotationCls, boolean searchSupers, boolean ignoreAccess) {
        Objects.requireNonNull(method, "method");
        Objects.requireNonNull(annotationCls, "annotationCls");
        if (!ignoreAccess && !MemberUtils.isAccessible(method)) {
            return null;
        }
        A annotation = method.getAnnotation(annotationCls);
        if (annotation == null && searchSupers) {
            Class<?> mcls = method.getDeclaringClass();
            List<Class<?>> classes = MethodUtils.getAllSuperclassesAndInterfaces(mcls);
            for (Class<?> acls : classes) {
                Method equivalentMethod = ignoreAccess ? MethodUtils.getMatchingMethod(acls, method.getName(), method.getParameterTypes()) : MethodUtils.getMatchingAccessibleMethod(acls, method.getName(), method.getParameterTypes());
                if (equivalentMethod == null || (annotation = equivalentMethod.getAnnotation(annotationCls)) == null) continue;
                break;
            }
        }
        return annotation;
    }

    private static Method getInvokeMethod(boolean forceAccess, String methodName, Class<?>[] parameterTypes, Class<? extends Object> cls) {
        Method method;
        if (forceAccess) {
            method = MethodUtils.getMatchingMethod(cls, methodName, parameterTypes);
            AccessibleObjects.setAccessible(method);
        } else {
            method = MethodUtils.getMatchingAccessibleMethod(cls, methodName, parameterTypes);
        }
        return method;
    }

    public static Method getMatchingAccessibleMethod(Class<?> cls, String methodName, Class<?> ... requestTypes) {
        Method candidate = MethodUtils.getMethodObject(cls, methodName, requestTypes);
        if (candidate != null) {
            return MemberUtils.setAccessibleWorkaround(candidate);
        }
        Method[] methods = cls.getMethods();
        List matchingMethods = Stream.of(methods).filter(method -> method.getName().equals(methodName) && MemberUtils.isMatchingMethod(method, requestTypes)).collect(Collectors.toList());
        matchingMethods.sort(METHOD_BY_SIGNATURE);
        Method bestMatch = null;
        for (Method method2 : matchingMethods) {
            Method accessibleMethod = MethodUtils.getAccessibleMethod(method2);
            if (accessibleMethod == null || bestMatch != null && MemberUtils.compareMethodFit(accessibleMethod, bestMatch, requestTypes) >= 0) continue;
            bestMatch = accessibleMethod;
        }
        if (bestMatch != null) {
            MemberUtils.setAccessibleWorkaround(bestMatch);
            if (bestMatch.isVarArgs()) {
                Class<?>[] bestMatchParameterTypes = bestMatch.getParameterTypes();
                Class<?> varArgType = bestMatchParameterTypes[bestMatchParameterTypes.length - 1].getComponentType();
                for (int paramIdx = bestMatchParameterTypes.length - 1; paramIdx < requestTypes.length; ++paramIdx) {
                    Class<?> parameterType = requestTypes[paramIdx];
                    if (ClassUtils.isAssignable(parameterType, varArgType, true)) continue;
                    return null;
                }
            }
        }
        return bestMatch;
    }

    public static Method getMatchingMethod(Class<?> cls, String methodName, Class<?> ... parameterTypes) {
        Objects.requireNonNull(cls, "cls");
        Validate.notEmpty(methodName, "methodName", new Object[0]);
        List methods = Stream.of(cls.getDeclaredMethods()).filter(method -> method.getName().equals(methodName)).collect(Collectors.toList());
        List<Class<?>> allSuperclassesAndInterfaces = MethodUtils.getAllSuperclassesAndInterfaces(cls);
        Collections.reverse(allSuperclassesAndInterfaces);
        allSuperclassesAndInterfaces.stream().map(Class::getDeclaredMethods).flatMap(Stream::of).filter(method -> method.getName().equals(methodName)).forEach(methods::add);
        for (Method method2 : methods) {
            if (!Arrays.deepEquals(method2.getParameterTypes(), parameterTypes)) continue;
            return method2;
        }
        TreeMap candidates = new TreeMap();
        methods.stream().filter(method -> ClassUtils.isAssignable(parameterTypes, method.getParameterTypes(), true)).forEach(method -> {
            int distance = MethodUtils.distance(parameterTypes, method.getParameterTypes());
            List candidatesAtDistance = candidates.computeIfAbsent(distance, k -> new ArrayList());
            candidatesAtDistance.add(method);
        });
        if (candidates.isEmpty()) {
            return null;
        }
        List bestCandidates = (List)candidates.values().iterator().next();
        if (bestCandidates.size() == 1 || !Objects.equals(((Method)bestCandidates.get(0)).getDeclaringClass(), ((Method)bestCandidates.get(1)).getDeclaringClass())) {
            return (Method)bestCandidates.get(0);
        }
        throw new IllegalStateException(String.format("Found multiple candidates for method %s on class %s : %s", methodName + Stream.of(parameterTypes).map(String::valueOf).collect(Collectors.joining(",", "(", ")")), cls.getName(), bestCandidates.stream().map(Method::toString).collect(Collectors.joining(",", "[", "]"))));
    }

    public static Method getMethodObject(Class<?> cls, String name, Class<?> ... parameterTypes) {
        try {
            return name != null && cls != null ? cls.getMethod(name, parameterTypes) : null;
        }
        catch (NoSuchMethodException | SecurityException e) {
            return null;
        }
    }

    public static List<Method> getMethodsListWithAnnotation(Class<?> cls, Class<? extends Annotation> annotationCls) {
        return MethodUtils.getMethodsListWithAnnotation(cls, annotationCls, false, false);
    }

    public static List<Method> getMethodsListWithAnnotation(Class<?> cls, Class<? extends Annotation> annotationCls, boolean searchSupers, boolean ignoreAccess) {
        Objects.requireNonNull(cls, "cls");
        Objects.requireNonNull(annotationCls, "annotationCls");
        ArrayList classes = searchSupers ? MethodUtils.getAllSuperclassesAndInterfaces(cls) : new ArrayList();
        classes.add(0, cls);
        ArrayList<Method> annotatedMethods = new ArrayList<Method>();
        classes.forEach(acls -> {
            Method[] methods = ignoreAccess ? acls.getDeclaredMethods() : acls.getMethods();
            Stream.of(methods).filter(method -> method.isAnnotationPresent(annotationCls)).forEachOrdered(annotatedMethods::add);
        });
        return annotatedMethods;
    }

    public static Method[] getMethodsWithAnnotation(Class<?> cls, Class<? extends Annotation> annotationCls) {
        return MethodUtils.getMethodsWithAnnotation(cls, annotationCls, false, false);
    }

    public static Method[] getMethodsWithAnnotation(Class<?> cls, Class<? extends Annotation> annotationCls, boolean searchSupers, boolean ignoreAccess) {
        return MethodUtils.getMethodsListWithAnnotation(cls, annotationCls, searchSupers, ignoreAccess).toArray(ArrayUtils.EMPTY_METHOD_ARRAY);
    }

    public static Set<Method> getOverrideHierarchy(Method method, ClassUtils.Interfaces interfacesBehavior) {
        Objects.requireNonNull(method, "method");
        LinkedHashSet<Method> result = new LinkedHashSet<Method>();
        result.add(method);
        Object[] parameterTypes = method.getParameterTypes();
        Class<?> declaringClass = method.getDeclaringClass();
        Iterator<Class<?>> hierarchy = ClassUtils.hierarchy(declaringClass, interfacesBehavior).iterator();
        hierarchy.next();
        block0: while (hierarchy.hasNext()) {
            Class<?> c = hierarchy.next();
            Method m = MethodUtils.getMatchingAccessibleMethod(c, method.getName(), parameterTypes);
            if (m == null) continue;
            if (Arrays.equals(m.getParameterTypes(), parameterTypes)) {
                result.add(m);
                continue;
            }
            Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(declaringClass, m.getDeclaringClass());
            for (int i = 0; i < parameterTypes.length; ++i) {
                Type parentType;
                Type childType = TypeUtils.unrollVariables(typeArguments, method.getGenericParameterTypes()[i]);
                if (!TypeUtils.equals(childType, parentType = TypeUtils.unrollVariables(typeArguments, m.getGenericParameterTypes()[i]))) continue block0;
            }
            result.add(m);
        }
        return result;
    }

    public static Object invokeExactMethod(Object object, String methodName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return MethodUtils.invokeExactMethod(object, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, null);
    }

    public static Object invokeExactMethod(Object object, String methodName, Object ... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object[] actuals = ArrayUtils.nullToEmpty(args);
        return MethodUtils.invokeExactMethod(object, methodName, actuals, ClassUtils.toClass(actuals));
    }

    public static Object invokeExactMethod(Object object, String methodName, Object[] args, Class<?>[] parameterTypes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?> cls = Objects.requireNonNull(object, "object").getClass();
        Class<?>[] paramTypes = ArrayUtils.nullToEmpty(parameterTypes);
        Method method = MethodUtils.getAccessibleMethod(cls, methodName, paramTypes);
        MethodUtils.requireNonNull(method, cls, methodName, paramTypes);
        return method.invoke(object, ArrayUtils.nullToEmpty(args));
    }

    public static Object invokeExactStaticMethod(Class<?> cls, String methodName, Object ... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object[] actuals = ArrayUtils.nullToEmpty(args);
        return MethodUtils.invokeExactStaticMethod(cls, methodName, actuals, ClassUtils.toClass(actuals));
    }

    public static Object invokeExactStaticMethod(Class<?> cls, String methodName, Object[] args, Class<?>[] parameterTypes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?>[] paramTypes = ArrayUtils.nullToEmpty(parameterTypes);
        Method method = MethodUtils.getAccessibleMethod(cls, methodName, ArrayUtils.nullToEmpty(paramTypes));
        MethodUtils.requireNonNull(method, cls, methodName, paramTypes);
        return method.invoke(null, ArrayUtils.nullToEmpty(args));
    }

    public static Object invokeMethod(Object object, boolean forceAccess, String methodName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return MethodUtils.invokeMethod(object, forceAccess, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, null);
    }

    public static Object invokeMethod(Object object, boolean forceAccess, String methodName, Object ... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object[] actuals = ArrayUtils.nullToEmpty(args);
        return MethodUtils.invokeMethod(object, forceAccess, methodName, actuals, ClassUtils.toClass(actuals));
    }

    public static Object invokeMethod(Object object, boolean forceAccess, String methodName, Object[] args, Class<?>[] parameterTypes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?> cls = Objects.requireNonNull(object, "object").getClass();
        Class<?>[] paramTypes = ArrayUtils.nullToEmpty(parameterTypes);
        Method method = MethodUtils.getInvokeMethod(forceAccess, methodName, paramTypes, cls);
        MethodUtils.requireNonNull(method, cls, methodName, paramTypes);
        return method.invoke(object, MethodUtils.toVarArgs(method, ArrayUtils.nullToEmpty(args)));
    }

    public static Object invokeMethod(Object object, String methodName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return MethodUtils.invokeMethod(object, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, null);
    }

    public static Object invokeMethod(Object object, String methodName, Object ... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object[] actuals = ArrayUtils.nullToEmpty(args);
        return MethodUtils.invokeMethod(object, methodName, actuals, ClassUtils.toClass(actuals));
    }

    public static Object invokeMethod(Object object, String methodName, Object[] args, Class<?>[] parameterTypes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return MethodUtils.invokeMethod(object, false, methodName, args, parameterTypes);
    }

    public static Object invokeStaticMethod(Class<?> cls, String methodName, Object ... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object[] actuals = ArrayUtils.nullToEmpty(args);
        return MethodUtils.invokeStaticMethod(cls, methodName, actuals, ClassUtils.toClass(actuals));
    }

    public static Object invokeStaticMethod(Class<?> cls, String methodName, Object[] args, Class<?>[] parameterTypes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?>[] paramTypes = ArrayUtils.nullToEmpty(parameterTypes);
        Method method = MethodUtils.getMatchingAccessibleMethod(cls, methodName, paramTypes);
        MethodUtils.requireNonNull(method, cls, methodName, paramTypes);
        return method.invoke(null, MethodUtils.toVarArgs(method, ArrayUtils.nullToEmpty(args)));
    }

    private static Method requireNonNull(Method method, Class<?> cls, String methodName, Class<?>[] parameterTypes) throws NoSuchMethodException {
        if (method == null) {
            throw new NoSuchMethodException(String.format("No method: %s.%s(%s)", cls.getName(), methodName, Stream.of(parameterTypes).map(Class::getName).collect(LangCollectors.joining(", "))));
        }
        return method;
    }

    static Object[] toVarArgs(Executable executable, Object[] args) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return executable.isVarArgs() ? MethodUtils.toVarArgs(args, executable.getParameterTypes()) : args;
    }

    private static Object[] toVarArgs(Object[] args, Class<?>[] methodParameterTypes) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Object lastArg;
        int mptLength = methodParameterTypes.length;
        if (args.length == mptLength && ((lastArg = args[args.length - 1]) == null || lastArg.getClass().equals(methodParameterTypes[mptLength - 1]))) {
            return args;
        }
        Object[] newArgs = ArrayUtils.arraycopy(args, 0, 0, mptLength - 1, () -> new Object[mptLength]);
        Class<?> varArgComponentType = methodParameterTypes[mptLength - 1].getComponentType();
        Class<?> varArgComponentWrappedType = ClassUtils.primitiveToWrapper(varArgComponentType);
        int varArgLength = args.length - mptLength + 1;
        Object varArgsArray = Array.newInstance(varArgComponentWrappedType, varArgLength);
        boolean primitiveOrWrapper = ClassUtils.isPrimitiveOrWrapper(varArgComponentWrappedType);
        for (int i = 0; i < varArgLength; ++i) {
            Object arg = args[mptLength - 1 + i];
            try {
                Array.set(varArgsArray, i, primitiveOrWrapper ? varArgComponentWrappedType.getConstructor(ClassUtils.wrapperToPrimitive(varArgComponentWrappedType)).newInstance(arg) : varArgComponentWrappedType.cast(arg));
                continue;
            }
            catch (InstantiationException e) {
                throw new IllegalArgumentException("Cannot convert vararg #" + i, e);
            }
        }
        if (varArgComponentType.isPrimitive()) {
            varArgsArray = ArrayUtils.toPrimitive(varArgsArray);
        }
        newArgs[mptLength - 1] = varArgsArray;
        return newArgs;
    }

    @Deprecated
    public MethodUtils() {
    }
}

