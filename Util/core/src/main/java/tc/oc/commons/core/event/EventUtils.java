package tc.oc.commons.core.event;

import java.lang.reflect.Method;

import com.google.common.eventbus.Subscribe;
import com.google.common.reflect.TypeToken;

public final class EventUtils {
    private EventUtils() {}

    /**
     * Derived from {@link com.google.common.eventbus.AnnotatedSubscriberFinder#getAnnotatedMethodsInternal}
     */
    public static boolean hasSubscriberMethods(Class<?> type) {
        for(Class<?> cls : TypeToken.of(type).getTypes().rawTypes()) {
            for(Method method : cls.getMethods()) {
                if(method.isAnnotationPresent(Subscribe.class)) return true;
            }
        }
        return false;
    }

    public static boolean hasSubscriberMethods(Object obj) {
        return hasSubscriberMethods(obj.getClass());
    }
}
