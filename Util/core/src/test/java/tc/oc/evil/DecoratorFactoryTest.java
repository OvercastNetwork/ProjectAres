package tc.oc.evil;

import javax.inject.Inject;

import com.google.inject.Binder;
import org.junit.Test;
import tc.oc.test.InjectedTestCase;

import static org.junit.Assert.*;
import static tc.oc.test.Assert.*;

public class DecoratorFactoryTest extends InjectedTestCase {
    
    @Inject DecoratorFactory factory;

    @Override
    protected void configure(Binder binder) {
        super.configure(binder);
        binder.bind(DecoratorFactory.class).toInstance(DecoratorFactory.get());
    }

    interface I {
        String name();
        int age();
    }

    static class C implements I {
        final String name;
        final int age;

        C(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override public String name() {
            return name;
        }

        @Override public int age() {
            return age;
        }
    }

    @Test
    public void decorateInterface() throws Exception {
        abstract class D implements I, Decorator<I> {
            @Override public String name() {
                return "yep";
            }

            @Override public I delegate() {
                return new C("nope", 123);
            }
        }

        Class<? extends I> ti = factory.implement(I.class, D.class);
        I i = factory.create(I.class, D.class, new Class<?>[]{DecoratorFactoryTest.class}, new Object[]{this});
        assertEquals("yep", i.name()); // override
        assertEquals(123, i.age()); // delegated
    }

    @Test
    public void decorateClass() throws Exception {
        abstract class D extends C implements Decorator<C> {
            D() { super("nope", 456); }

            @Override public String name() {
                return "yep";
            }

            @Override public C delegate() {
                return new C("nah", 123);
            }
        }

        Class<? extends C> tc = factory.implement(C.class, D.class);
        C c = factory.create(C.class, D.class, new Class[]{DecoratorFactoryTest.class}, new Object[]{this});
        assertEquals("yep", c.name()); // override
        assertEquals(123, c.age()); // delegated
    }

    @Test
    public void missingMethod() throws Exception {
        abstract class D implements I, Decorator<I> {
            abstract void missing();

            @Override public I delegate() {
                return new C("nope", 123);
            }
        }

        assertThrows(IllegalStateException.class,
                     () -> factory.implement(I.class, D.class));
    }

    interface J {
        String name();
    }

    @Test
    public void delegateShadowedMethod() throws Exception {
        // The proxy should forward J#name() to I, even though J is not assignable to I.
        // This is a tricky case.
        abstract class D implements J, I, Decorator<I> {
            @Override public I delegate() {
                return new C("yep", 123);
            }
        }
        J j = factory.create(I.class, D.class, new Class[]{DecoratorFactoryTest.class}, new Object[]{this});
        assertEquals("yep", j.name());
    }
}