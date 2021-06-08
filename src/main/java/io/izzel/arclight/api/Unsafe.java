package io.izzel.arclight.api;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Objects;

@SuppressWarnings("all")
public class Unsafe {

    private static final sun.misc.Unsafe unsafe;
    private static final MethodHandles.Lookup lookup;
    private static final MethodHandle defineClass;

    static {
        try {
            Field theUnsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (sun.misc.Unsafe) theUnsafe.get(null);
            Unsafe.ensureClassInitialized(MethodHandles.Lookup.class);
            Field field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            Object base = unsafe.staticFieldBase(field);
            long offset = unsafe.staticFieldOffset(field);
            lookup = (MethodHandles.Lookup) unsafe.getObject(base, offset);
            MethodHandle mh;
            try {
                Method sunMisc = unsafe.getClass().getMethod("defineClass", String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class);
                mh = lookup.unreflect(sunMisc).bindTo(unsafe);
            } catch (Exception e) {
                Class<?> jdkInternalUnsafe = Class.forName("jdk.internal.misc.Unsafe");
                Field internalUnsafeField = jdkInternalUnsafe.getDeclaredField("theUnsafe");
                Object internalUnsafe = unsafe.getObject(unsafe.staticFieldBase(internalUnsafeField), unsafe.staticFieldOffset(internalUnsafeField));
                Method internalDefineClass = jdkInternalUnsafe.getMethod("defineClass", String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class);
                mh = lookup.unreflect(internalDefineClass).bindTo(internalUnsafe);
            }
            defineClass = Objects.requireNonNull(mh);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T getStatic(Class<?> cl, String name) {
        try {
            Unsafe.ensureClassInitialized(cl);
            Field field = cl.getDeclaredField(name);
            Object materialByNameBase = Unsafe.staticFieldBase(field);
            long materialByNameOffset = Unsafe.staticFieldOffset(field);
            return (T) Unsafe.getObject(materialByNameBase, materialByNameOffset);
        } catch (Exception e) {
            return null;
        }
    }

    public static MethodHandles.Lookup lookup() {
        return lookup;
    }

    public static sun.misc.Unsafe getUnsafe() {
        return unsafe;
    }

    public static Object getObject(Object o, long l) {
        return unsafe.getObject(o, l);
    }

    public static void putObject(Object o, long l, Object o1) {
        unsafe.putObject(o, l, o1);
    }

    public static long staticFieldOffset(Field field) {
        return unsafe.staticFieldOffset(field);
    }

    public static long objectFieldOffset(Field field) {
        return unsafe.objectFieldOffset(field);
    }

    public static Object staticFieldBase(Field field) {
        return unsafe.staticFieldBase(field);
    }

    public static void ensureClassInitialized(Class<?> aClass) {
        unsafe.ensureClassInitialized(aClass);
    }

    private static Class<?> getCallerClass() {
        return INSTANCE.getClassContext()[3];
    }

    private static final CallerClass INSTANCE = new CallerClass();

    private static class CallerClass extends SecurityManager {

        @Override
        public Class<?>[] getClassContext() {
            return super.getClassContext();
        }
    }

}