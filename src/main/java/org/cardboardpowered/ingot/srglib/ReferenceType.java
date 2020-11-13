package org.cardboardpowered.ingot.srglib;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * A java reference type
 */
public class ReferenceType implements JavaType {
    private final String name, internalName;
    private static final Pattern TYPE_NAME_PATTERN = Pattern.compile("([\\w$_]+\\.)*([\\w$_]+)");
    public ReferenceType(String name) {
        this.name = requireNonNull(name, "Null name");
        if (!TYPE_NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid class name: " + name);
        }
        this.internalName = name.replace('.', '/');
    }

    @Override
    public JavaTypeSort getSort() {
        return JavaTypeSort.REFERENCE_TYPE;
    }

    @Override
    public JavaType mapClass(UnaryOperator<JavaType> func) {
        return func.apply(this);
    }

    @Override
    public String getInternalName() {
        return internalName;
    }

    @Override
    public String getDescriptor() {
        return "L" + internalName + ";";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSimpleName() {
        int index = name.lastIndexOf('.');
        return index < 0 ? name : name.substring(index + 1);
    }

    @Override
    public String getPackageName() {
        int index = name.lastIndexOf('.');
        return index < 0 ? "" : name.substring(0, index);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReferenceType that = (ReferenceType) o;
        return name.equals(that.name);
    }

    @Override
    public String toString() {
        return getName();
    }
}