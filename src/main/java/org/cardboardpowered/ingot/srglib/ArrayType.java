package org.cardboardpowered.ingot.srglib;

import java.util.function.UnaryOperator;

import static java.util.Objects.*;

/**
 * An array type
 */
public class ArrayType implements JavaType {
    private final JavaType elementType;
    public ArrayType(JavaType elementType) {
        this.elementType = requireNonNull(elementType);
    }

    /**
     * Return the element type of this array.
     *
     * @return the element type
     */
    @Override
    public JavaType getElementType() {
        return elementType;
    }

    @Override
    public JavaTypeSort getSort() {
        return JavaTypeSort.ARRAY_TYPE;
    }

    @Override
    public String getInternalName() {
        return elementType.getInternalName() + "[]";
    }

    @Override
    public String getDescriptor() {
        return "[" + elementType.getDescriptor();
    }

    @Override
    public String getName() {
        return elementType.getName() + "[]";
    }

    @Override
    public JavaType mapClass(UnaryOperator<JavaType> func) {
        int dimensions = 1;
        JavaType elementType = this.getElementType();
        while (elementType.isArrayType()) {
            elementType = elementType.getElementType();
            dimensions += 1;
        }
        return JavaType.createArray(dimensions, elementType.mapClass(func));
    }

    private int hashCode = 0;
    @Override
    public int hashCode() {
        int hashCode = this.hashCode;
        if (hashCode == 0) {
            JavaType innermostType = this.elementType;
            int dimensions = 1;
            while (innermostType instanceof ArrayType) {
                innermostType = ((ArrayType) innermostType).elementType;
                dimensions++;
            }
            hashCode = dimensions + ~innermostType.hashCode();
            if (hashCode == 0) hashCode = 1; // Make sure it's not zero so we never trigger again
            this.hashCode = hashCode;
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj != null
                && obj.getClass() == ArrayType.class
                && obj.hashCode() == this.hashCode()
                && this.getElementType().equals(((ArrayType) obj).getElementType());
    }

    @Override
    public String toString() {
        return getName();
    }
}