package tc.oc.commons.core.util;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;

import tc.oc.commons.core.reflect.Methods;

public class ArrayUtils {
    private ArrayUtils() {}

    public static final int NOT_FOUND_INDEX = -1;
    
    public static <T> T fromEnd(T[] array, int index) {
        return array[array.length - 1 - index];
    }

    public static <T> int indexOf(T[] array, T value) {
        if(array == null) return NOT_FOUND_INDEX;

        if(value == null) {
            for(int i = 0; i < array.length; i++) {
                if(array[i] == null) return i;
            }
        } else {
            for(int i = 0; i < array.length; i++) {
                if(value.equals(array[i])) return i;
            }
        }

        return NOT_FOUND_INDEX;
    }

    public static <T> boolean contains(T[] array, T value) {
        return indexOf(array, value) != NOT_FOUND_INDEX;
    }

    /**
     * Create a new array of given size, with the same component type as the given array
     */
    public static <T> T[] sameType(T[] array, int size) {
        return (T[]) Array.newInstance(array.getClass().getComponentType(), size);
    }

    /**
     * Similar to {@link Arrays#copyOfRange}, but from can be negative,
     * which will copy to an offset in the destination array.
     */
    public static <T> T[] copyOfRange(T[] src, int from, int to) {
        final T[] dest = sameType(src, to - from);
        System.arraycopy(src, Math.max(0, from),
                         dest, Math.max(0, -from),
                         Math.min(to, src.length));
        return dest;
    }

    public static <T> T[] append(T[] a, T...b) {
        if(b.length == 0) return a;
        if(a.length == 0) return b;

        T[] result = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public static <T> T[] prepend(T element, T[] array) {
        final T[] result = copyOfRange(array, -1, array.length);
        result[0] = element;
        return result;
    }

    public static <T> void copy(Iterable<? extends T> src, T[] dest, int destPos) {
        for(T t : src) {
            if(destPos >= dest.length) break;
            dest[destPos++] = t;
        }
    }

    public static <T> T first(T[] array, Predicate<T> test, T def) {
        for(T t : array) {
            if(test.test(t)) return t;
        }
        return def;
    }

    public static @Nullable <T> T first(T[] array, Predicate<T> test) {
        return first(array, test, null);
    }

    private static final Method APPLY_METHOD = Methods.method(Function.class, "apply", Object.class);

    public static <T, U> U[] transform(T[] input, Class<U> outputType, Function<T, U> function) {
        return transform(input, (U[]) Array.newInstance(outputType, input.length), function);
    }

    public static <T, U> U[] transform(T[] input, U[] output, Function<T, U> function) {
        for(int i = 0; i < input.length; i++) {
            output[i] = function.apply(input[i]);
        }
        return output;
    }

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[]{};
    public static Object[] zeroObjects() { return EMPTY_OBJECT_ARRAY; }

    public static List<Boolean> asList(boolean[] array) {
        return new AbstractList<Boolean>() {
            @Override public int size() { return array.length; }
            @Override public Boolean get(int index) { return array[index]; }
        };
    }

    public static List<Character> asList(char[] array) {
        return new AbstractList<Character>() {
            @Override public int size() { return array.length; }
            @Override public Character get(int index) { return array[index]; }
        };
    }

    public static List<Byte> asList(byte[] array) {
        return new AbstractList<Byte>() {
            @Override public int size() { return array.length; }
            @Override public Byte get(int index) { return array[index]; }
        };
    }

    public static List<Short> asList(short[] array) {
        return new AbstractList<Short>() {
            @Override public int size() { return array.length; }
            @Override public Short get(int index) { return array[index]; }
        };
    }

    public static List<Integer> asList(int[] array) {
        return new AbstractList<Integer>() {
            @Override public int size() { return array.length; }
            @Override public Integer get(int index) { return array[index]; }
        };
    }

    public static List<Long> asList(long[] array) {
        return new AbstractList<Long>() {
            @Override public int size() { return array.length; }
            @Override public Long get(int index) { return array[index]; }
        };
    }

    public static List<Float> asList(float[] array) {
        return new AbstractList<Float>() {
            @Override public int size() { return array.length; }
            @Override public Float get(int index) { return array[index]; }
        };
    }

    public static List<Double> asList(double[] array) {
        return new AbstractList<Double>() {
            @Override public int size() { return array.length; }
            @Override public Double get(int index) { return array[index]; }
        };
    }

    public static List<?> asList(Object array) {
        if(!array.getClass().isArray()) {
            throw new IllegalArgumentException("Not an array");
        }

        final Class<?> type = array.getClass().getComponentType();
        if(!type.isPrimitive()) {
            return Arrays.asList((Object[]) array);
        } else if(boolean.class.equals(type)) {
            return asList((boolean[]) array);
        } else if(char.class.equals(type)) {
            return asList((char[]) array);
        } else if(byte.class.equals(type)) {
            return asList((byte[]) array);
        } else if(short.class.equals(type)) {
            return asList((short[]) array);
        } else if(int.class.equals(type)) {
            return asList((int[]) array);
        } else if(long.class.equals(type)) {
            return asList((long[]) array);
        } else if(float.class.equals(type)) {
            return asList((float[]) array);
        } else if(double.class.equals(type)) {
            return asList((double[]) array);
        }

        throw new IllegalArgumentException("Weird array type: " + type);
    }

    public static <T> List<T> asSubList(int start, int end, T... array) {
        return new AbstractList() {
            @Override
            public int size() {
                return end - start;
            }

            @Override
            public Object get(int index) {
                return array[start + index];
            }
        };
    }

    public static <T> List<T> asSubListFrom(int start, T... array) {
        return asSubList(start, array.length, array);
    }

    public static <T> List<T> asSubListTo(int end, T... array) {
        return asSubList(0, end, array);
    }
}
