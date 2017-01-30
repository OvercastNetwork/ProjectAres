package tc.oc.commons.core.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Predicate;

import com.google.common.cache.LoadingCache;
import tc.oc.commons.core.util.CacheUtils;
import tc.oc.commons.core.util.ExceptionUtils;

public final class Annotations {
    private Annotations() {}

    public static Predicate<AnnotatedElement> annotatedWith(Class<? extends Annotation> annotation) {
        return element -> element.isAnnotationPresent(annotation);
    }

    private static final LoadingCache<Class<? extends Annotation>, Class<? extends Annotation>> annotationTypeCache = CacheUtils.newCache(type -> {
        Class<? extends Annotation> annotationType = null;
        for(Class<?> cls = type; cls != null; cls = cls.getSuperclass()) {
            for(Class<?> iface : cls.getInterfaces()) {
                if(iface.isAnnotation()) {
                    if(annotationType != null) {
                        throw new IllegalStateException("Multiple annotation types found for " + type.getSimpleName() +
                                                        ": " + annotationType.getName() + " and " + iface.getName());
                    }
                    annotationType = (Class<? extends Annotation>) iface;
                }
            }
        }
        if(annotationType == null) {
            throw new IllegalStateException("Can't find annotation type of " + type.getName());
        }
        return annotationType;
    });

    public static Class<? extends Annotation> annotationType(Class<? extends Annotation> type) {
        return annotationTypeCache.getUnchecked(type);
    }

    /**
     * Implements {@link Annotation#equals(Object)} as specified
     */
    public static boolean equals(Annotation a, Object obj) {
        if(!(obj instanceof Annotation)) return false;

        final Annotation b = (Annotation) obj;
        if(!a.annotationType().equals(b.annotationType())) return false;

        try {
            for(Method method : a.annotationType().getDeclaredMethods()) {
                method.setAccessible(true);
                if(!Objects.equals(method.invoke(a), method.invoke(b))) return false;
            }
        } catch(ReflectiveOperationException e) {
            throw ExceptionUtils.propagate(e);
        }

        return true;
    }

    /**
     * Implements {@link Annotation#hashCode()} as specified
     */
    public static int hashCode(Annotation annotation) {
        int hashCode = 0;
        try {
            for(Method method : annotation.annotationType().getDeclaredMethods()) {
                method.setAccessible(true);
                hashCode += (127 * method.getName().hashCode()) ^ Objects.hashCode(method.invoke(annotation));
            }
        } catch(ReflectiveOperationException e) {
            throw ExceptionUtils.propagate(e);
        }
        return hashCode;
    }

    public static String toString(Annotation annotation) {
        String text = "@" + annotation.annotationType().getSimpleName();
        boolean empty = false;
        try {
            for(Method method : annotation.annotationType().getDeclaredMethods()) {
                if(empty) {
                    empty = false;
                    text += "(";
                } else {
                    text += " ";
                }
                text += method.getName() + "=" + String.valueOf(method.invoke(annotation));
            }
        } catch(ReflectiveOperationException e) {
            throw ExceptionUtils.propagate(e);
        }
        if(!empty) {
            text += ")";
        }
        return text;
    }
}
