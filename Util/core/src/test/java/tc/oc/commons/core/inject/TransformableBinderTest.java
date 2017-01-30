package tc.oc.commons.core.inject;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.junit.Test;
import tc.oc.test.InjectedTestCase;

import static org.junit.Assert.*;
import static tc.oc.test.Assert.*;

public class TransformableBinderTest extends InjectedTestCase {

    @Test
    public void noTransformers() throws Exception {
        String hi = Guice.createInjector(binder -> {
            TransformableBinder<String> tb = new TransformableBinder<>(binder, String.class);
            tb.bindOriginal().toInstance("hi");
        }).getInstance(String.class);

        assertEquals("hi", hi);
    }

    @Test
    public void someTransformers() throws Exception {
        String hi = Guice.createInjector(binder -> {
            TransformableBinder<String> tb = new TransformableBinder<>(binder, String.class);
            tb.bindOriginal().toInstance("hello");
            tb.bindTransformer().toInstance(p -> p.get().toUpperCase());
            tb.bindTransformer().toInstance(p -> p.get().replace('L', 'X'));
        }).getInstance(String.class);

        assertEquals("HEXXO", hi);
    }

    @Test
    public void originalKeyAnnotationsPreserved() throws Exception {
        class Woot {
            @Inject @Named("upper") String upper;
            @Inject @Named("lower") String lower;
        }

        Woot woot = new Woot();
        Guice.createInjector(binder -> {
            TransformableBinder<String> upper = new TransformableBinder<>(binder, Key.get(String.class, Names.named("upper")));
            upper.bindOriginal().toInstance("Hello");
            upper.bindTransformer().toInstance(p -> p.get().toUpperCase());

            TransformableBinder<String> lower = new TransformableBinder<>(binder, Key.get(String.class, Names.named("lower")));
            lower.bindOriginal().toInstance("Hello");
            lower.bindTransformer().toInstance(p -> p.get().toLowerCase());
        }).injectMembers(woot);

        assertEquals("HELLO", woot.upper);
        assertEquals("hello", woot.lower);
    }

    @Test
    public void transformersAppliedInBindingOrder() throws Exception {
        String hi = Guice.createInjector(binder -> {
            TransformableBinder<String> tb = new TransformableBinder<>(binder, String.class);
            tb.bindOriginal().toInstance("0");
            for(int i = 1; i <= 3; i++) {
                final int fi = i;
                tb.bindTransformer().toInstance(p -> fi + p.get() + fi);
            }
        }).getInstance(String.class);

        assertEquals("3210123", hi);
    }

    @Test
    public void originalBindingRequired() throws Exception {
        assertThrows(CreationException.class, () -> {
            Guice.createInjector(binder -> {
                new TransformableBinder<>(binder, String.class);
            });
        });
    }

    @Test
    public void regularBindingConflictsWithTransformableBinding() throws Exception {
        assertThrows(CreationException.class, () -> {
            Guice.createInjector(binder -> {
                binder.bind(String.class).toInstance("hi");
                new TransformableBinder<>(binder, String.class);
            });
        });
    }
}
