package tc.oc.commons.reflect;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import junit.framework.TestCase;
import tc.oc.commons.core.reflect.Types;

public class TypeCoercionTest extends TestCase {

    public void testPrimitives() throws Exception {
        assertTrue(Types.isConvertibleForInvocation(int.class, int.class));
    }

    public void testPrimitivePromotion() throws Exception {
        assertTrue(Types.isConvertibleForInvocation(long.class, int.class));
    }

    public void testPrimitiveDemotion() throws Exception {
        assertFalse(Types.isConvertibleForInvocation(int.class, long.class));
    }

    public void testUnboxing() throws Exception {
        assertTrue(Types.isConvertibleForInvocation(int.class, Integer.class));
    }

    public void testBoxing() throws Exception {
        assertTrue(Types.isConvertibleForInvocation(Integer.class, int.class));
    }

    public void testUnboxingPlusPromotion() throws Exception {
        assertTrue(Types.isConvertibleForInvocation(long.class, Integer.class));
    }

    public void testBoxingPlusPromotion() throws Exception {
        assertFalse(Types.isConvertibleForInvocation(Long.class, int.class));
    }

    public void testObjects() throws Exception {
        assertTrue(Types.isConvertibleForInvocation(String.class, String.class));
    }

    public void testUpcasting() throws Exception {
        assertTrue(Types.isConvertibleForInvocation(Object.class, String.class));
    }

    public void testDowncasting() throws Exception {
        assertFalse(Types.isConvertibleForInvocation(String.class, Object.class));
    }

    public void testBoxingPlusUpcasting() throws Exception {
        assertTrue(Types.isConvertibleForInvocation(Number.class, int.class));
    }

    public void testGenerics() throws Exception {
        // This just delegates directly to TypeToken, so we don't need to test every possible case
        assertTrue(Types.isConvertibleForInvocation(new TypeToken<Set<? extends Number>>(){},
                                                    new TypeToken<ImmutableSet<Integer>>(){}));
    }

    public void testVoid() throws Exception {
        assertTrue(Types.isConvertibleForInvocation(void.class, void.class));
    }
}
