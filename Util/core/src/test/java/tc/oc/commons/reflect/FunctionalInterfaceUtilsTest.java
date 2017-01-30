package tc.oc.commons.reflect;

import junit.framework.TestCase;
import tc.oc.commons.core.reflect.Methods;

public class FunctionalInterfaceUtilsTest extends TestCase {
    interface SingleMethodAbstract {
        void abs();
    }

    interface SingleMethodDefault {
        default void def() {}
    }

    interface MultipleMethodsNoneAbstract {
        default void def1() {}
        default void def2() {}
    }

    interface MultipleMethodsOneAbstract {
        default void def() {}
        void abs();
    }

    interface MultipleMethodsMultipleAbstract {
        void abs1();
        void abs2();
    }

    interface WithStaticMethods {
        static void stat1() {}
        void abs();
    }

    public void testDetectFunctionalInterface() throws Exception {
        assertEquals(SingleMethodAbstract.class.getMethod("abs"), Methods.trySamMethod(SingleMethodAbstract.class));
        assertEquals(SingleMethodDefault.class.getMethod("def"), Methods.trySamMethod(SingleMethodDefault.class));
        assertNull(Methods.trySamMethod(MultipleMethodsNoneAbstract.class));
        assertEquals(MultipleMethodsOneAbstract.class.getMethod("abs"), Methods.trySamMethod(MultipleMethodsOneAbstract.class));
        assertNull(Methods.trySamMethod(MultipleMethodsMultipleAbstract.class));
        assertEquals(WithStaticMethods.class.getMethod("abs"), Methods.trySamMethod(WithStaticMethods.class));
    }
}
