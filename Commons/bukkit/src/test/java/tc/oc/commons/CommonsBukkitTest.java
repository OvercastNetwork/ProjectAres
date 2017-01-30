package tc.oc.commons;

import com.google.inject.Guice;
import org.junit.Before;
import tc.oc.commons.core.inject.TestModule;

public abstract class CommonsBukkitTest {
    @Before
    public void setUp() {
        Guice.createInjector(new TestModule()).injectMembers(this);
    }
}
