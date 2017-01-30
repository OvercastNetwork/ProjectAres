package tc.oc.commons.core.inject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import javax.inject.Named;

import com.google.common.collect.Lists;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import org.junit.Test;
import tc.oc.test.InjectedTestCase;

import static org.junit.Assert.*;
import static tc.oc.test.Assert.*;

public class InjectableMethodTest extends InjectedTestCase {

    @Test
    public void constant() throws Exception {
        class Woot {
            String foo() {
                return "hi";
            }
        }

        InjectableMethod<?> method = InjectableMethod.forDeclaredMethod(new Woot(), "foo");
        String hi = Guice.createInjector(method.bindingModule()).getInstance(String.class);

        assertEquals("hi", hi);
    }

    @Test
    public void voidReturnType() throws Exception {
        class Woot {
            void foo() {}
        }

        InjectableMethod<?> method = InjectableMethod.forDeclaredMethod(new Woot(), "foo");
        assertThrows(CreationException.class, () ->
            Guice.createInjector(method.bindingModule())
        );
    }

    @Test
    public void dependencies() throws Exception {
        class Woot {
            String foo(int i, short s) {
                return i + " " + s;
            }
        }

        InjectableMethod<?> method = InjectableMethod.forDeclaredMethod(new Woot(), "foo", int.class, short.class);
        String hi = Guice.createInjector(
            method.bindingModule(),
            binder -> {
                binder.bind(int.class).toInstance(123);
                binder.bind(short.class).toInstance((short) 456);
            }
        ).getInstance(String.class);

        assertEquals("123 456", hi);
    }

    @Test
    public void qualifiedReturnType() throws Exception {
        class Woot {
            @Named("q") String foo() {
                return "hi";
            }
        }

        InjectableMethod<?> method = InjectableMethod.forDeclaredMethod(new Woot(), "foo");
        String hi = Guice.createInjector(method.bindingModule()).getInstance(Key.get(String.class, Names.named("q")));

        assertEquals("hi", hi);
    }

    @Test
    public void qualifiedDependency() throws Exception {
        class Woot {
            String foo(@Named("q") int i) {
                return String.valueOf(i);
            }
        }

        InjectableMethod<?> method = InjectableMethod.forDeclaredMethod(new Woot(), "foo", int.class);
        String hi = Guice.createInjector(
            method.bindingModule(),
            binder -> binder.bind(int.class)
                            .annotatedWith(Names.named("q"))
                            .toInstance(123)
        ).getInstance(String.class);


        assertEquals("123", hi);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Yay {}

    @Test
    public void scanForMethods() throws Exception {
        class Woot {
            @Yay String foo() {
                return "foo";
            }

            String bar() {
                return "bar";
            }
        }

        List<InjectableMethod<?>> methods = InjectableMethod.forAnnotatedMethods(TypeLiteral.get(Woot.class), new Woot(), Yay.class);
        Injector injector = Guice.createInjector(Lists.transform(methods, InjectableMethod::bindingModule));

        assertEquals("foo", injector.getInstance(String.class));
    }
}
