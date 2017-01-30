package tc.oc.commons.reflect;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import com.google.common.reflect.TypeToken;
import junit.framework.TestCase;
import tc.oc.commons.core.reflect.MethodFormException;
import tc.oc.commons.core.reflect.Methods;
import tc.oc.commons.core.util.ThrowingRunnable;

import static tc.oc.test.Assert.*;

public class DynamicLambdaTest extends TestCase {

    static void staticRunnable() {}

    static String staticFunction(Integer i) {
        return String.valueOf(i);
    }

    public void testStaticMethodAsRunnable() throws Exception {
        Methods.lambda(Runnable.class, getClass().getDeclaredMethod("staticRunnable"), null);
    }

    public void testStaticMethodAsFunction() throws Exception {
        final Function<Integer, String> lambda = Methods.lambda(new TypeToken<Function<Integer, String>>(){},
                                                                getClass().getDeclaredMethod("staticFunction", Integer.class),
                                                                null);
        assertEquals("123", lambda.apply(123));
    }

    public void testInstanceMethodAsRunnable() throws Exception {
        final boolean[] flag = new boolean[1];
        class C { void runnable() { flag[0] = true; } }
        final Runnable lambda = Methods.lambda(Runnable.class, C.class.getDeclaredMethod("runnable"), new C());
        lambda.run();
        assertTrue(flag[0]);
    }

    public void testInstanceMethodAsFunction() throws Exception {
        class C {
            String function(Integer i) {
                return String.valueOf(i);
            }
        }

        final Function<Integer, String> lambda = Methods.lambda(new TypeToken<Function<Integer, String>>(){},
                                                                C.class.getDeclaredMethod("function", Integer.class),
                                                                new C());
        assertEquals("123", lambda.apply(123));
    }

    public void testWidenParameter() throws Exception {
        class C { void woot(Object foo) {} }
        final Consumer<String> lambda = Methods.lambda(new TypeToken<Consumer<String>>(){}, C.class.getDeclaredMethod("woot", Object.class), new C());
        lambda.accept("hi");
    }

    public void testBoxParameter() throws Exception {
        class C { void woot(Integer foo) {} }
        final IntConsumer lambda = Methods.lambda(IntConsumer.class, C.class.getDeclaredMethod("woot", Integer.class), new C());
        lambda.accept(123);
    }

    public void testUnboxParameter() throws Exception {
        class C { void woot(int foo) {} }
        final Consumer<Integer> lambda = Methods.lambda(new TypeToken<Consumer<Integer>>(){}, C.class.getDeclaredMethod("woot", int.class), new C());
        lambda.accept(123);
    }

    public void testPromoteParameter() throws Exception {
        class C { void woot(long foo) {} }
        final IntConsumer lambda = Methods.lambda(IntConsumer.class, C.class.getDeclaredMethod("woot", long.class), new C());
        lambda.accept(123);
    }

    public void testNarrowParameterThrows() throws Exception {
        class C { void woot(String foo) {} }
        assertThrows(MethodFormException.class, () ->
            Methods.lambda(new TypeToken<Consumer<Object>>(){}, C.class.getDeclaredMethod("woot", String.class), new C())
        );
    }

    public void testDemoteParameterThrows() throws Exception {
        class C { void woot(int foo) {} }
        assertThrows(MethodFormException.class, () ->
            Methods.lambda(LongConsumer.class, C.class.getDeclaredMethod("woot", int.class), new C())
        );
    }

    public void testMissingParameterThrows() throws Exception {
        class C { void woot() {} }
        assertThrows(MethodFormException.class, () ->
            // Missing parameter
            Methods.lambda(Consumer.class, C.class.getDeclaredMethod("woot"), new C())
        );
    }

    public void testExtraParameterThrows() throws Exception {
        class C { void woot(Object doo) {} }
        assertThrows(MethodFormException.class, () ->
            Methods.lambda(Runnable.class, C.class.getDeclaredMethod("woot", Object.class), new C())
        );
    }

    public void testWidenReturnType() throws Exception {
        class C { String woot() { return null; } }
        final Supplier<Object> lambda = Methods.lambda(new TypeToken<Supplier<Object>>(){}, C.class.getDeclaredMethod("woot"), new C());
        lambda.get();
    }

    public void testUnboxReturnType() throws Exception {
        class C { Integer woot() { return 123; } }
        final IntSupplier lambda = Methods.lambda(IntSupplier.class, C.class.getDeclaredMethod("woot"), new C());
        lambda.getAsInt();
    }

    public void testBoxReturnType() throws Exception {
        class C { int woot() { return 123; } }
        final Supplier<Integer> lambda = Methods.lambda(new TypeToken<Supplier<Integer>>(){}, C.class.getDeclaredMethod("woot"), new C());
        lambda.get();
    }

    public void testPromoteReturnType() throws Exception {
        class C { int woot() { return 123; } }
        final LongSupplier lambda = Methods.lambda(LongSupplier.class, C.class.getDeclaredMethod("woot"), new C());
        lambda.getAsLong();
    }

    public void testNarrowReturnTypeThrows() throws Exception {
        class C { Object woot() { return new Object(); } }
        assertThrows(MethodFormException.class, () ->
            Methods.lambda(new TypeToken<Supplier<String>>(){}, C.class.getDeclaredMethod("woot"), new C())
        );
    }

    public void testDemoteReturnTypeThrows() throws Exception {
        class C { long woot() { return 123L; } }
        assertThrows(MethodFormException.class, () ->
            Methods.lambda(IntSupplier.class, C.class.getDeclaredMethod("woot"), new C())
        );
    }

    public void testValidGenericImplementation() throws Exception {
        class C<T, R> { R woot(T t) { return null; } }
        Methods.lambda(new TypeToken<Function<Number, String>>(){}, C.class.getDeclaredMethod("woot", Object.class), new C<Number, String>(){});
    }

    public void testInvalidGenericImplementationThrows() throws Exception {
        class C<T, R> { R woot(T t) { return null; } }
        final TypeToken<Function<Number, String>> samType = new TypeToken<Function<Number, String>>() {};
        final Method woot = C.class.getDeclaredMethod("woot", Object.class);
        assertThrows(MethodFormException.class, () -> Methods.lambda(samType, woot, new C<String, String>(){})); // bad return type
        assertThrows(MethodFormException.class, () -> Methods.lambda(samType, woot, new C<Number, Number>(){})); // bad param type
    }

    public void testHandledException() throws Exception {
        class C { void woot() throws IOException {} }
        Methods.lambda(ThrowingRunnable.class, C.class.getDeclaredMethod("woot"), new C()).run();
        Methods.lambda(new TypeToken<ThrowingRunnable<IOException>>(){}, C.class.getDeclaredMethod("woot"), new C()).run();
    }

    public void testUnhandledExceptionThrows() throws Exception {
        class C { void woot() throws IOException {} }
        assertThrows(MethodFormException.class, () -> Methods.lambda(Runnable.class, C.class.getDeclaredMethod("woot"), new C()));
        assertThrows(MethodFormException.class, () -> Methods.lambda(new TypeToken<ThrowingRunnable<EOFException>>(){}, C.class.getDeclaredMethod("woot"), new C()));
    }
}
