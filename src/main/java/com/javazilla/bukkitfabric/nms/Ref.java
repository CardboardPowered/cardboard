package com.javazilla.bukkitfabric.nms;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.javazilla.bukkitfabric.BukkitFabricMod;

import net.minecraft.block.CarpetBlock;
import net.minecraft.block.HorizontalConnectingBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.LilyPadBlock;
import net.minecraft.network.NetworkState;
import net.minecraft.util.shape.VoxelShapes;
import sun.misc.Unsafe;

// protocolsupport.utils.reflection.ReflectionUtils
public class Ref {

    private Ref() {
    }

    public static @Nonnull String toStringAllFields(@Nonnull Object obj) {
        StringJoiner joiner = new StringJoiner(", ");
        Class<?> clazz = obj.getClass();
        do {
            try {
                for (Field field : clazz.getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        setAccessible(field);
                        Object value = field.get(obj);
                        if ((value == null) || !value.getClass().isArray()) {
                            joiner.add(field.getName() + ": " + Objects.toString(value));
                        } else {
                            joiner.add(field.getName() + ": " + Arrays.deepToString(new Object[] {value}));
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException ("Unable to get object fields values", e);
            }
        } while ((clazz = clazz.getSuperclass()) != null);
        return obj.getClass().getName() + "(" + joiner.toString() + ")";
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> generifyClass(Class<?> clazz) {
        return (Class<T>) clazz;
    }

    @SuppressWarnings("unchecked")
    public static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }

    public static @Nonnull <T extends AccessibleObject> T setAccessible(@Nonnull T object) {
        object.setAccessible(true);
        return object;
    }

    public static @Nonnull Field findField(Class<?> clazz, @Nonnull String name) {
        if (name.equals("h")) {
            try {
                return setAccessible( NetworkState.class.getDeclaredField("HANDLER_STATE_MAP") );
            } catch (NoSuchFieldException | SecurityException e) {
                e.printStackTrace();
            }
        }
        if (name.equals("j")) {
            try {
                return setAccessible( NetworkState.class.getDeclaredField("packetHandlers") );
            } catch (NoSuchFieldException | SecurityException e) {
                e.printStackTrace();
            }
        }
        if (name.equals("a")) {
            try {
                return Class.forName("net.minecraft.network.NetworkState$PacketHandler").getDeclaredField("packetIds");
            } catch (NoSuchFieldException | SecurityException | ClassNotFoundException e) {
                // TODO Auto-generated catch block
                BukkitFabricMod.LOGGER.info("DEBUG: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
        if (name.equals("g")) {
            try {
                return  setAccessible( HorizontalConnectingBlock.class.getDeclaredField("collisionShapes") );
            } catch (NoSuchFieldException | SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (null != clazz) { 
            do {
               for (Field field : clazz.getDeclaredFields()) {
                    if (field.getName().equals(name)) {
                        return setAccessible(field);
                    }
                }
            } while ((clazz = clazz.getSuperclass()) != null);
        }
        throw new RuntimeException ("Can't find field " + name + " in class " + clazz + " or it's superclasses");
    }

    public static @Nonnull Method findMethod(@Nonnull Class<?> clazz, @Nonnull String name, @Nonnegative int paramlength) {
        do {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(name) && (method.getParameterTypes().length == paramlength)) {
                    return setAccessible(method);
                }
            }
        } while ((clazz = clazz.getSuperclass()) != null);
        throw new RuntimeException ("Can't find method " + name + " with params length " + paramlength + " in class " + clazz + " or it's superclasses");
    }

    public static boolean isStaticFinalField(@Nonnull Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers);
    }

    public static void setStaticFinalFieldValue(@Nonnull Class<?> clazz, @Nonnull String name, @Nullable Object value) {
        System.out.println("Debug: Name: " + clazz.getName());
        if (clazz == LilyPadBlock.class) {
            if (name.equals("a")) {
                name = "SHAPE"; // field_11728 
            }
        }
        if (clazz == LadderBlock.class) {
            if (name.equals("d")) {
                name = "EAST_SHAPE"; // field_11255 
            }
            if (name.equals("e")) {
                name = "WEST_SHAPE"; // field_11252 
            }
            if (name.equals("f")) {
                name = "SOUTH_SHAPE"; // field_11254 
            }
            if (name.equals("g")) {
                name = "NORTH_SHAPE"; // field_11256 
            }
        }
        if (clazz == CarpetBlock.class) {
            if (name.equals("a")) {
                name = "SHAPE"; // field_11783 
            }
        }
        setStaticFinalFieldValue(findField(clazz, name), value);
    }

    public static void setStaticFinalFieldValue(@Nonnull Field field, @Nullable Object value) {
        try {
            sffs.set(field, value);
        } catch (RuntimeException  t) {
            throw t;
        } catch (Throwable t) {
            throw new RuntimeException (t);
        }
    }


    protected static interface StaticFinalFieldSetter {

        public void set(Field field, Object value);

    }

    protected static class UnsafeStaticFinalFieldSetter implements StaticFinalFieldSetter {

        protected final Unsafe unsafe;

        protected UnsafeStaticFinalFieldSetter() {
            try {
                unsafe = ((Unsafe) setAccessible(Unsafe.class.getDeclaredField("theUnsafe")).get(null));
            } catch (Throwable e) {
                throw new RuntimeException (e);
            }
        }

        @Override
        public void set(Field field, Object value) {
            try {
                if ((value != null) && !field.getType().isInstance(value)) {
                    System.out.println( "Can't set field type " + field.getType().getName() + " to " + value.getClass().getName() );
                    throw new IllegalArgumentException("Can't set field type " + field.getType().getName() + " to " + value.getClass().getName());
                   // return;
                }
                unsafe.putObjectVolatile(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), value);
            } catch (Throwable e) {
                throw new RuntimeException (e);
            }
        }

    }

    protected static class MethodHandleIMPLLookupStaticFinalFieldSetter implements StaticFinalFieldSetter {

        protected final MethodHandles.Lookup implLookup;

        protected MethodHandleIMPLLookupStaticFinalFieldSetter() {
            try {
                implLookup = ((MethodHandles.Lookup) setAccessible(MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP")).get(null));
            } catch (Throwable e) {
                throw new RuntimeException (e);
            }
        }

        @Override
        public void set(Field field, Object value) {
            try {
                implLookup
                .findSetter(Field.class, "modifiers", int.class)
                .invokeExact(field, field.getModifiers() & ~Modifier.FINAL);
                setAccessible(field).set(null, value);
            } catch (Throwable e) {
                throw new RuntimeException (e);
            }
        }

    }

    protected static class ErrorStaticFinalFieldSetter implements StaticFinalFieldSetter {

        private final RuntimeException  exception;

        protected ErrorStaticFinalFieldSetter(RuntimeException  exception) {
            this.exception = exception;
        }

        @Override
        public void set(Field field, Object value) {
            throw exception;
        }

    }

    private static final StaticFinalFieldSetter sffs = initSFFS();

    private static StaticFinalFieldSetter initSFFS() {
        RuntimeException  exception = new RuntimeException ("Static final field setter initialization failed");
        try {
            return new UnsafeStaticFinalFieldSetter();
        } catch (Throwable t) {
            exception.addSuppressed(t);
        }
        try {
            return new MethodHandleIMPLLookupStaticFinalFieldSetter();
        } catch (Throwable t) {
            exception.addSuppressed(t);
        }
        return new ErrorStaticFinalFieldSetter(exception);
    }

}
