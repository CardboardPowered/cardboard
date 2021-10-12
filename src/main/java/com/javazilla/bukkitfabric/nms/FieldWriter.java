package com.javazilla.bukkitfabric.nms;

import java.lang.reflect.Field;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.HorizontalConnectingBlock;

public class FieldWriter<T> {

    public static <T> FieldWriter<T> of(@Nonnull Class<?> holder, @Nonnull String name) {
        return new FieldWriter<>(Ref.findField(holder, name));
    }

    public static <T> FieldWriter<T> of(@Nonnull Class<?> holder, @Nonnull String name, @Nonnull Class<T> fieldType) {
        System.out.println("Debug: Name1: " + holder.getName());
        Field field = null;
        if (holder == HorizontalConnectingBlock.class) {
            if (name.equals("g")) {
                name = "collisionShapes";
            }
            if (name.equals("h")) {
                name = "boundingShapes";
            }
        }
        if (null == field)
            field = Ref.findField(holder, name);

        if (field.getType().isAssignableFrom(fieldType)) {
            return new FieldWriter<>(field);
        } else {
            throw new RuntimeException("Field type missmatch, expected super " + fieldType + ", got " + field.getType());
        }
    }

    protected final Field field;
    protected final boolean staticfinal;

    public FieldWriter(@Nonnull Field field) {
        this.field = Ref.setAccessible(field);
        this.staticfinal = Ref.isStaticFinalField(field);
    }

    public @Nullable void set(@Nullable Object holder, @Nullable T value) {
        if (!staticfinal) {
            try {
                field.set(holder, value);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            Ref.setStaticFinalFieldValue(field, value);
        }
    }

}
