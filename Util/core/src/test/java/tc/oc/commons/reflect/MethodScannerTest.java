package tc.oc.commons.reflect;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.function.Predicate;

import org.junit.Test;
import tc.oc.commons.core.reflect.MethodScanner;
import tc.oc.commons.core.reflect.Methods;
import tc.oc.test.InjectedTestCase;

import static tc.oc.test.Assert.*;

public class MethodScannerTest extends InjectedTestCase {

    static final Predicate<Method> filter = method -> !Object.class.equals(method.getDeclaringClass());

    @Test
    public void simple() throws Exception {
        class Woot {
            void foo() {}
            void bar() {}
        }

        Collection<Method> methods = new MethodScanner<>(Woot.class, filter).methods();
        assertSet(methods, Methods.declaredMethod(Woot.class, "foo"), Methods.declaredMethod(Woot.class, "bar"));
    }

    @Test
    public void inheritFromSuperclass() throws Exception {
        class Woot {
            void foo() {}
        }
        class Donk extends Woot {
            void bar() {}
        }

        Collection<Method> methods = new MethodScanner<>(Donk.class, filter).methods();
        assertSet(methods, Methods.declaredMethod(Woot.class, "foo"), Methods.declaredMethod(Donk.class, "bar"));
    }

    @Test
    public void overridden() throws Exception {
        class Woot {
            void foo() {}
        }
        class Donk extends Woot {
            @Override void foo() {}
        }

        Collection<Method> methods = new MethodScanner<>(Donk.class, filter).methods();
        assertSet(methods, Methods.declaredMethod(Donk.class, "foo"));
    }

    @Test
    public void privateMethodsWithSameSignature() throws Exception {
        class Woot {
            private void foo() {}
        }
        class Donk extends Woot {
            private void foo() {}
        }

        Collection<Method> methods = new MethodScanner<>(Donk.class, filter).methods();
        assertSet(methods, Methods.declaredMethod(Woot.class, "foo"), Methods.declaredMethod(Donk.class, "foo"));
    }

    interface Blah {
        default void foo() {}
    }

    @Test
    public void inheritFromInterface() throws Exception {
        class Woot implements Blah {}

        Collection<Method> methods = new MethodScanner<>(Woot.class, filter).methods();
        assertSet(methods, Methods.declaredMethod(Blah.class, "foo"));
    }
}
