package tc.oc.commons.reflect;

import junit.framework.TestCase;
import tc.oc.commons.core.reflect.Methods;

import static tc.oc.commons.core.reflect.Methods.isCallable;

public class CallableMethodTest extends TestCase {

    interface Interface {
        void interfaceMethod();
        default void defaultMethod() {}
    }

    public void testConcreteMethodIsCallable() throws Exception {
        class C { public void woot() {} }
        assertTrue(isCallable(C.class.getMethod("woot")));
    }

    public void testAbstractMethodIsNotCallable() throws Exception {
        abstract class C { public abstract void woot(); }
        assertFalse(isCallable(C.class.getMethod("woot")));
    }

    public void testInterfaceMethodIsNotCallable() throws Exception {
        assertFalse(isCallable(Interface.class.getMethod("interfaceMethod")));
    }

    public void testDefaultMethodIsCallable() throws Exception {
        assertTrue(isCallable(Interface.class.getMethod("defaultMethod")));
    }

    public void testRespondsToConcreteMethod() throws Exception {
        class C { public void woot() {} }
        assertTrue(Methods.respondsTo(C.class, C.class.getMethod("woot")));
    }

    public void testDoesNotRespondToAbstractMethod() throws Exception {
        abstract class C { public abstract void woot(); }
        assertFalse(Methods.respondsTo(C.class, C.class.getMethod("woot")));
    }

    public void testDoesNotRespondToInterfaceMethod() throws Exception {
        assertFalse(Methods.respondsTo(Interface.class, Interface.class.getMethod("interfaceMethod")));
    }

    public void testRespondsToDefaultMethod() throws Exception {
        assertTrue(Methods.respondsTo(Interface.class, Interface.class.getMethod("defaultMethod")));
    }

    public void testRespondsToInheritedConcreteMethod() throws Exception {
        class A { public void woot() {} }
        class B extends A {}
        assertTrue(Methods.respondsTo(B.class, A.class.getMethod("woot")));
    }

    public void testRespondsToInheritedDefaultMethod() throws Exception {
        abstract class C implements Interface {}
        assertTrue(Methods.respondsTo(C.class, Interface.class.getMethod("defaultMethod")));
    }

    public void testRespondsToInterfaceMethodOverride() throws Exception {
        class C implements Interface { @Override public void interfaceMethod() {} }
        assertTrue(Methods.respondsTo(C.class, Interface.class.getMethod("interfaceMethod")));
    }

    public void testRespondsToAbstractMethodOverride() throws Exception {
        abstract class A { public abstract void woot(); }
        class B extends A { @Override public void woot() {} }
        assertTrue(Methods.respondsTo(B.class, A.class.getMethod("woot")));
    }

    public void testRespondsToOverrideWithMatchingParams() throws Exception {
        abstract class A { public abstract void woot(int x, String y); }
        class B extends A { @Override public void woot(int x, String y) {} }
        assertTrue(Methods.respondsTo(B.class, A.class.getMethod("woot", Integer.TYPE, String.class)));
    }
}
