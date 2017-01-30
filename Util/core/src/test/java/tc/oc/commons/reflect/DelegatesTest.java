package tc.oc.commons.reflect;

import org.junit.Test;
import tc.oc.commons.core.reflect.Delegates;

import static org.junit.Assert.*;
import static tc.oc.test.Assert.*;

public class DelegatesTest {

    static class Thing {
        private static int staticField = 123;

        private static String staticMethod(int n) {
            return String.valueOf(n);
        }

        int instanceField;

        private Thing(int n) {
            instanceField = n;
        }
    }

    interface StaticFields {
        int staticField();
    }

    interface StaticMethods {
        String staticMethod(int n);
    }

    interface Constructors {
        Thing create(int n);
    }

    @Test
    public void staticFieldDelegate() throws Exception {
        StaticFields p = Delegates.newStaticFieldDelegate(StaticFields.class, Thing.class);
        assertEquals(123, p.staticField());
    }

    @Test
    public void staticMethodDelegate() throws Exception {
        StaticMethods p = Delegates.newStaticMethodDelegate(StaticMethods.class, Thing.class);
        assertEquals("123", p.staticMethod(123));
    }

    @Test
    public void constructorDelegate() throws Exception {
        Constructors p = Delegates.newConstructorDelegate(Constructors.class, Thing.class);
        assertEquals(123, p.create(123).instanceField);
    }
}

