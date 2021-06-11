package org.bukkit.craftbukkit.persistence;

import com.google.common.primitives.Primitives;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;
import org.apache.commons.lang3.Validate;
import org.bukkit.persistence.PersistentDataContainer;

/**
 * This class represents a registry that contains the used adapters for.
 */
public final class CraftPersistentDataTypeRegistry {

    private final Function<Class, TagAdapter> CREATE_ADAPTER = this::createAdapter;

    private class TagAdapter<T, Z extends NbtElement> {

        private final Function<T, Z> builder;
        private final Function<Z, T> extractor;

        private final Class<T> primitiveType;
        private final Class<Z> TagType;

        public TagAdapter(Class<T> primitiveType, Class<Z> TagType, Function<T, Z> builder, Function<Z, T> extractor) {
            this.primitiveType = primitiveType;
            this.TagType = TagType;
            this.builder = builder;
            this.extractor = extractor;
        }

        T extract(NbtElement base) {
            Validate.isInstanceOf(TagType, base, "The provided Tag was of the type %s. Expected type %s", base.getClass().getSimpleName(), TagType.getSimpleName());
            return this.extractor.apply(TagType.cast(base));
        }

        Z build(Object value) {
            Validate.isInstanceOf(primitiveType, value, "The provided value was of the type %s. Expected type %s", value.getClass().getSimpleName(), primitiveType.getSimpleName());
            return this.builder.apply(primitiveType.cast(value));
        }

        boolean isInstance(NbtElement base) {
            return this.TagType.isInstance(base);
        }
    }

    private final Map<Class, TagAdapter> adapters = new HashMap<>();

    private <T> TagAdapter createAdapter(Class<T> type) {
        if (!Primitives.isWrapperType(type))
            type = Primitives.wrap(type); // Make sure we will always "switch" over the wrapper types

        // Primitives
        if (Objects.equals(Byte.class, type))
            return createAdapter(Byte.class, NbtByte.class, NbtByte::of, NbtByte::byteValue);

        if (Objects.equals(Short.class, type))
            return createAdapter(Short.class, NbtShort.class, NbtShort::of, NbtShort::shortValue);

        if (Objects.equals(Integer.class, type))
            return createAdapter(Integer.class, NbtInt.class, NbtInt::of, NbtInt::intValue);

        if (Objects.equals(Long.class, type))
            return createAdapter(Long.class, NbtLong.class, NbtLong::of, NbtLong::longValue);

        if (Objects.equals(Float.class, type))
            return createAdapter(Float.class, NbtFloat.class, NbtFloat::of, NbtFloat::floatValue);

        if (Objects.equals(Double.class, type))
            return createAdapter(Double.class, NbtDouble.class, NbtDouble::of, NbtDouble::doubleValue);

        // String
        if (Objects.equals(String.class, type))
            return createAdapter(String.class, NbtString.class, NbtString::of, NbtString::asString);

        // Primitive Arrays
        if (Objects.equals(byte[].class, type))
            return createAdapter(byte[].class, NbtByteArray.class, array -> new NbtByteArray(Arrays.copyOf(array, array.length)), n -> Arrays.copyOf(n.getByteArray(), n.size()));

        if (Objects.equals(int[].class, type))
            return createAdapter(int[].class, NbtIntArray.class, array -> new NbtIntArray(Arrays.copyOf(array, array.length)), n -> Arrays.copyOf(n.getIntArray(), n.size()));

        if (Objects.equals(long[].class, type))
            return createAdapter(long[].class, NbtLongArray.class, array -> new NbtLongArray(Arrays.copyOf(array, array.length)), n -> Arrays.copyOf(n.getLongArray(), n.size()));

        // Note that this will map the interface PersistentMetadataContainer directly to the CraftBukkit implementation
        // Passing any other instance of this form to the tag type registry will throw a ClassCastException as defined in TagAdapter#build
        if (Objects.equals(PersistentDataContainer.class, type)) {
            return createAdapter(CraftPersistentDataContainer.class, NbtCompound.class, CraftPersistentDataContainer::toTagCompound, tag -> {
                CraftPersistentDataContainer container = new CraftPersistentDataContainer(this);
                for (String key : tag.getKeys())
                    container.put(key, tag.get(key));
                return container;
            });
        }

        throw new IllegalArgumentException("Could not find a valid TagAdapter implementation for the requested type " + type.getSimpleName());
    }

    private <T, Z extends NbtElement> TagAdapter<T, Z> createAdapter(Class<T> primitiveType, Class<Z> TagType, Function<T, Z> builder, Function<Z, T> extractor) {
        return new TagAdapter<>(primitiveType, TagType, builder, extractor);
    }

    public <T> NbtElement wrap(Class<T> type, T value) {
        return this.adapters.computeIfAbsent(type, CREATE_ADAPTER).build(value);
    }

    public <T> boolean isInstanceOf(Class<T> type, NbtElement base) {
        return this.adapters.computeIfAbsent(type, CREATE_ADAPTER).isInstance(base);
    }

    public <T> T extract(Class<T> type, NbtElement tag) throws ClassCastException, IllegalArgumentException {
        TagAdapter adapter = this.adapters.computeIfAbsent(type, CREATE_ADAPTER);
        Validate.isTrue(adapter.isInstance(tag), "`The found tag instance cannot store %s as it is a %s", type.getSimpleName(), tag.getClass().getSimpleName());

        Object foundValue = adapter.extract(tag);
        Validate.isInstanceOf(type, foundValue, "The found object is of the type %s. Expected type %s", foundValue.getClass().getSimpleName(), type.getSimpleName());
        return type.cast(foundValue);
    }

}