package tc.oc.commons.reflect;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Test;
import tc.oc.commons.core.reflect.AnnotationBase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AnnotationsTest {

    @Retention(RetentionPolicy.RUNTIME)
    @interface Woot {
        int number() default 123;
        String string() default "Hi!";
    }

    class WootImpl extends AnnotationBase implements Woot {

        final int number;
        final String string;

        WootImpl(int number, String string) {
            this.number = number;
            this.string = string;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Woot.class;
        }

        @Override
        public int number() {
            return number;
        }

        @Override
        public String string() {
            return string;
        }
    }

    @Test
    public void equality() throws Throwable {
        @Woot(number=456, string="Bye!") class C {}
        final Woot a = C.class.getAnnotation(Woot.class);
        final Woot b = new WootImpl(456, "Bye!");

        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertEquals(a.hashCode(), b.hashCode());
    }
}
