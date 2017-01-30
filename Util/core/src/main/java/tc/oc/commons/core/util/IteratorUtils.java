package tc.oc.commons.core.util;

import java.lang.reflect.Method;
import java.util.Iterator;

import com.google.common.reflect.TypeToken;
import tc.oc.commons.core.reflect.Methods;

public final class IteratorUtils {
    private IteratorUtils() {}

    private static final Method NEXT_METHOD = Methods.method(Iterator.class, "next");

    public static <T> TypeToken<T> elementType(TypeToken<? extends Iterator<T>> iteratorType) {
        return (TypeToken<T>) iteratorType.method(NEXT_METHOD).getReturnType();
    }

}
