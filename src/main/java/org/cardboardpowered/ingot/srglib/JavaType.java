package org.cardboardpowered.ingot.srglib;

import java.util.Locale;
import java.util.function.UnaryOperator;

import static java.util.Objects.requireNonNull;

/**
 * A java type/class.
 */
public interface JavaType {

    /**
     * Return the internal name of this type.
     * <p>
     * The internal name of a class is its fully qualified name,
     * (as returned by Class.getName() where '.' are replaced by '/')
     * For primitives, this is identical to get_name()
     *
     * @return the internal name
     */
    String getInternalName();

    /**
     * Return the bytecode descriptor of this type
     *
     * @return this type's bytecode descriptor
     */
    String getDescriptor();

    /**
     * Return the name of this type, like returned by Class.getName()
     *
     * @return the name of this type
     */
    String getName();

    /**
     * Return the name of this type, with the package removed.
     * <p>
     * If this type doesn't have the package, it just returns its name.
     * </p>
     *
     * @return the simple name
     */
    default String getSimpleName() {
        return getName();
    }

    /**
     * Return if this type is a primitive type.
     *
     * @return if this type is primitive
     */
    default boolean isPrimitiveType() {
        return getSort() == JavaTypeSort.PRIMITIVE_TYPE;
    }

    /**
     * Return if this type is an array tyep.
     *
     * @return if it's an array.
     */
    default boolean isArrayType() {
        return getSort() == JavaTypeSort.ARRAY_TYPE;
    }

    /**
     * Return if this type is a reference type.
     *
     * @return if it's a reference type
     */
    default boolean isReferenceType() {
        return getSort() == JavaTypeSort.REFERENCE_TYPE;
    }

    JavaTypeSort getSort();

    /**
     * Apply the specified mapping to this type, based on its class name.
     * <p>
     * If type is an array, it remaps the innermost element type.
     * If the type is a class, it invokes the specified function
     * If the type is a primitive, it returns the same element type.
     * </p>
     *
     * @param func the mapping function to apply to this type
     * @return the new type
     */
    JavaType mapClass(UnaryOperator<JavaType> func);

    /**
     * Return this reference type's package, or an empty string if in the default package.
     *
     * @return the package name
     * @throws IllegalStateException if this type's not a reference type
     */
    default String getPackageName() {
        throw new IllegalStateException(getName() + " is not a reference type!");
    }

    /**
     * Return this array's element type.
     *
     * @return the element type
     * @throws IllegalStateException if this type's not a array type
     */
    default JavaType getElementType() {
        throw new IllegalStateException(getName() + " is not an array type!");
    }

    static JavaType createArray(int dimensions, JavaType elementType) {
        requireNonNull(elementType, "Null element type");
        if (dimensions == 0) return elementType;
        if(dimensions < 0) throw new IllegalArgumentException("Negative dimensions: " + dimensions);
        ArrayType result = new ArrayType(elementType);
        while (--dimensions > 0) {
            result = new ArrayType(result); // Keep nesting
        }
        return result;
    }

    static JavaType createArray(JavaType elementType) {
        return createArray(1, elementType);
    }

    /**
     * Return a {@link JavaType} with the given name.
     *
     * @param name the name of the type
     * @return the type
     * @throws IllegalArgumentException if the name is invalid
     */
    //@NonNull
    public static JavaType fromName(String name) {
        requireNonNull(name, "Null name");
        if (name.endsWith("[]")) {
            int dimensions = 0;
            do {
                dimensions += 1;
                name = name.substring(0, name.length() - 2);
            } while (name.endsWith("[]"));
            JavaType elementType = fromName(name);
            assert !(elementType instanceof ArrayType);
            ArrayType result;
            do {
                result = new ArrayType(elementType);
                elementType = result;
            } while (--dimensions > 0);
            return result;
        }
        try {
            return PrimitiveType.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            // Fallback to treating it as a reference-type/class
            try {
                return new ReferenceType(name);
            } catch (IllegalArgumentException e) {
                // Hide the true error ^_^
                throw new IllegalArgumentException("Invalid type name: " + name);
            }
        }
    }

    /**
     * Return a JavaType with the given internal name.
     *
     * @param internalName the internal name of the type.
     * @return a new JavaType
     */
    public static JavaType fromInternalName(String internalName) {
        requireNonNull(internalName, "Null internal name");
        return fromName(internalName.replace('/', '.'));
    }

    /**
     * Return a JavaType with the given descriptor
     *
     * @param descriptor the descriptor to parse
     * @return a new JavaType
     * @throws IllegalArgumentException if the descriptor is invalid
     */
    static JavaType fromDescriptor(String descriptor) {
        final int descriptorLength = requireNonNull(descriptor, "Null descriptor").length();
        switch (descriptorLength) {
            case 0:
                throw new IllegalArgumentException("Empty descriptor!");
            case 1:
                return PrimitiveType.fromDescriptorChar(descriptor.charAt(0));
            default:
                char firstChar = descriptor.charAt(0);
                switch (firstChar) {
                    case '[':
                        int dimensions = 1;
                        while (descriptor.charAt(dimensions) == '[') {
                            dimensions++;
                        }
                        return createArray(dimensions, fromDescriptor(descriptor.substring(dimensions)));
                    case 'L':
                        if (descriptor.charAt(descriptorLength - 1) == ';') {
                            char[] internalName = new char[descriptorLength - 2];
                            descriptor.getChars(1, descriptorLength - 1, internalName, 0); // slice(1, -1)
                            for (int i = 0; i < internalName.length; i++) {
                                char c = internalName[i];
                                if (c == '.') {
                                    internalName[i] = '/'; // Replace all '.' with '/'
                                }
                            }
                            return fromInternalName(String.valueOf(internalName));
                        }
                }
        }
        throw new IllegalArgumentException("Invalid descriptor: " + descriptor);
    }

}