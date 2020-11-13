package org.cardboardpowered.ingot.srglib;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public final class ImmutableLists {
    private ImmutableLists() {}

    @SuppressWarnings("unchecked")
    public static <T, U> List<U> transform(List<T> original, Function<T, U> transformer) {
        int size = requireNonNull(original, "Null original list").size();
        Object[] result = new Object[size];
        for (int i = 0; i < size; i++) {
            T originalElement = original.get(i);
            U newElement = transformer.apply(originalElement);
            requireNonNull(newElement, "Transformer produced null value for input: " + originalElement);
            result[i] = newElement;
        }
        return (List<U>) Arrays.asList(result);
    }


    public static <T> String joinToString(List<T> list, Function<T, String> asString, String delimiter) {
        return joinToString(list, asString, delimiter, "", "");
    }

    public static <T> String joinToString(
            List<T> list,
            Function<T, String> asString,
            String delimiter,
            String prefix,
            String suffix
    ) {
        int size = requireNonNull(list, "Null list").size();
        int delimiterLength = requireNonNull(delimiter, "Null delimiter").length();
        int prefixLength = requireNonNull(prefix, "Null prefix").length();
        int suffixLength = requireNonNull(suffix, "Null suffix").length();
        String[] strings = new String[size];
        int neededChars = prefixLength + suffixLength + (Math.max(0, size - 1)) * delimiterLength;
        for (int i = 0; i < size; i++) {
            T element = list.get(i);
            String str = asString.apply(element);
            strings[i] = str;
            neededChars += str.length();
        }
        char[] result = new char[neededChars];
        int resultSize = 0;
        prefix.getChars(0, prefixLength, result, resultSize);
        resultSize += prefixLength;
        for (int i = 0; i < size; i++) {
            String str = strings[i];
            if (i > 0) {
                // Prefix it with the delimiter
                delimiter.getChars(0, delimiterLength, result, resultSize);
                resultSize += delimiterLength;
            }
            int length = str.length();
            str.getChars(0, length, result, resultSize);
            resultSize += length;
        }
        suffix.getChars(0, suffixLength, result, resultSize);
        resultSize += suffixLength;
        assert result.length == resultSize;
        return String.valueOf(result);
    }
}