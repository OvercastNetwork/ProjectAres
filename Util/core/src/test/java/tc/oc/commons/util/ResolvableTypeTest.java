package tc.oc.commons.util;

import com.google.inject.TypeLiteral;
import junit.framework.TestCase;
import tc.oc.commons.core.reflect.ResolvableType;

public class ResolvableTypeTest extends TestCase {
    static class A<X> {
        TypeLiteral<X> resolved() {
            return new ResolvableType<X>(){}.in(getClass());
        }
    }

    static class AString extends A<String> {}

    public void testTypeResolution() {
        assertEquals(String.class, new AString().resolved().getRawType());
    }

    // TODO: more tests
}
