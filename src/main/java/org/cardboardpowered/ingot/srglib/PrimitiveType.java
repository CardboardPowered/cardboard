package org.cardboardpowered.ingot.srglib;

import java.util.function.UnaryOperator;


/**
 * An enumeration of java's 8 primitive types and 'VOID.
 *
 * void is actually a valid java type (believe it or not)
 */
public enum PrimitiveType implements JavaType {
    BYTE('B'),
    SHORT('S'),
    INT('I'),
    LONG('J'),
    FLOAT('F'),
    DOUBLE('D'),
    CHAR('C'),
    BOOLEAN('Z'),
    VOID('V');

    @Override
    public String getInternalName() {
        return getName();
    }

    @Override
    public String getDescriptor() {
        return String.valueOf(descriptorChar);
    }

    @Override
    public String getName() {
        return this.name().toLowerCase();
    }

    @Override
    public JavaTypeSort getSort() {
        return JavaTypeSort.PRIMITIVE_TYPE;
    }

    @Override
    public JavaType mapClass(UnaryOperator<JavaType> func) {
        return this;
    }

    private final char descriptorChar;
    private PrimitiveType(char descriptorChar) {
        this.descriptorChar = descriptorChar;
    }


    private static final PrimitiveType[] byDescriptorChar = new PrimitiveType[128];
    static {
        for (PrimitiveType type : values()) {
            assert byDescriptorChar[type.descriptorChar] == null : "Duplicate descriptors with char " + type.descriptorChar;
            byDescriptorChar[type.descriptorChar] = type;
        }
    }

    public static PrimitiveType fromDescriptorChar(char descriptorChar) {
        PrimitiveType primitiveType;
        if (descriptorChar <= byDescriptorChar.length && (primitiveType = byDescriptorChar[descriptorChar]) != null) {
            return primitiveType;
        }
        throw new IllegalArgumentException("Invalid descriptor char: " + descriptorChar);
    }

    @Override
    public String toString() {
        return getName();
    }
}