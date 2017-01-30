package tc.oc.commons.core.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionVisitor {
    public boolean visit(Class<?> cls) {
        return true;
    }

    public void visit(Class<?> container, Method method) {
    }

    public void visit(Class<?> container, Field field) {
    }
}
