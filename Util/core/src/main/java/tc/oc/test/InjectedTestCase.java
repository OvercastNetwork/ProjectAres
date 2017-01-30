package tc.oc.test;

import javax.inject.Inject;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;

/* For your cut and paste convenience

import static org.junit.Assert.*;
import static tc.oc.test.Assert.*;

*/

public class InjectedTestCase {

    @Inject protected Injector injector;

    protected void configure(Binder binder) {}

    @Before
    public void createInjector() throws Exception {
        Guice.createInjector(
            this::configure,
            binder -> binder.requestInjection(InjectedTestCase.this)
        );
    }
}
