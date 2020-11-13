package org.cardboardpowered.ingot.srglib;

import java.util.LinkedList;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * A method's signature, containing its parameter and return types/
 */
public final class MethodSignature {
    private final List<JavaType> parameterTypes;
    private final JavaType returnType;

    private MethodSignature(List<JavaType> parameterTypes, JavaType returnType) {
        this.parameterTypes = requireNonNull(parameterTypes, "Null parameter types");
        this.returnType = requireNonNull(returnType, "Null return type");
        for (JavaType parameterType : parameterTypes) {
            if(parameterType == PrimitiveType.VOID) throw new IllegalArgumentException("Void parameter!");
        }
    }

    public List<JavaType> getParameterTypes() {
        return parameterTypes;
    }

    public JavaType getReturnType() {
        return returnType;
    }

    public MethodSignature mapTypes(UnaryOperator<JavaType> transformer) {
        JavaType newReturnType = returnType.mapClass(transformer);
        List<JavaType> newParameterTypes = this.parameterTypes.stream().map(type -> type.mapClass(transformer)).collect(Collectors.toList());
        MethodSignature result = create(newParameterTypes, newReturnType);
        if (result.equals(this)) {
            return this;
        } else {
            return result;
        }
    }

    private String descriptor;

    /**
     * Return the bytecode descriptor of this method type.
     *
     * @return the bytecode descriptor
     */
    public String getDescriptor() {
        if (descriptor == null) {
            descriptor = ImmutableLists.joinToString(
                    parameterTypes,
                    JavaType::getDescriptor,
                    "",
                    "(",
                    ")"
            ) + returnType.getDescriptor();
        }
        return descriptor;
    }

    public String toString() {
        return ImmutableLists.joinToString(
                parameterTypes,
                JavaType::getSimpleName,
                ",",
                "(",
                ")"
        ) + returnType.getSimpleName();
    }

    private int hash;

    @Override
    public int hashCode() {
        int hash = this.hash;
        if (hash == 0) {
            hash = returnType == PrimitiveType.VOID ? 0 : returnType.hashCode();
            if (!parameterTypes.isEmpty()) {
                hash ^= parameterTypes.hashCode();
            }
            this.hash = hash;
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj != null && obj.getClass() == MethodSignature.class
                && this.hashCode() == obj.hashCode()
                && this.returnType.equals(((MethodSignature) obj).returnType)
                && this.parameterTypes.equals(((MethodSignature) obj).parameterTypes);
    }

    /**
     * Parse the specified bytecode method descriptor into a signature object.
     *
     * @param descriptor the bytecode descriptor
     * @return a new signature object
     * @throws IllegalArgumentException if the signature is invalid
     */
    public static MethodSignature fromDescriptor(String descriptor) {
        if(descriptor.length() <= 2 && descriptor.charAt(0) != '(')
            throw new IllegalArgumentException("Invalid descriptor: " + descriptor);
        int lastArgChar = descriptor.indexOf(')');
        if(lastArgChar < 0) throw new IllegalArgumentException("Invalid descriptor: " + descriptor);
        List<JavaType> parameterTypes = new LinkedList<>();
        for (int index = 1; index < lastArgChar; index++) {
            char c = descriptor.charAt(index);
            final int arrayDimensions;
            if (c == '[') {
                int dimensions = 1;
                while ((c = descriptor.charAt(index + dimensions)) == '[') dimensions++;
                index += dimensions;
                arrayDimensions = dimensions;
            } else {
                arrayDimensions = 0;
            }
            final JavaType result;
            if (c == 'L') {
                int endIndex = descriptor.indexOf(';', index);
                if(endIndex < 0 || endIndex >= lastArgChar)
                    throw new IllegalArgumentException("Invalid descriptor: " + descriptor);
                String internalName = descriptor.substring(index + 1, endIndex);
                result = JavaType.fromInternalName(internalName);
                index = endIndex;
            } else {
                result = PrimitiveType.fromDescriptorChar(c);
            }
            parameterTypes.add(JavaType.createArray(arrayDimensions, result));
        }
        JavaType returnType = JavaType.fromDescriptor(descriptor.substring(lastArgChar + 1));
        return create(parameterTypes, returnType);
    }

    public static MethodSignature create(List<JavaType> parameterTypes, JavaType returnType) {
        return new MethodSignature(parameterTypes, returnType);
    }
}