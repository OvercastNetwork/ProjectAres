package tc.oc.commons.core.inject;

import javax.annotation.Nullable;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * Improves on Guice's AbstractModule in a few ways:
 *
 *  - More distinct name
 *  - Includes everything in {@link Binders}
 *  - {@link #configure()} is optional
 *  - We can put custom scanners and other fun stuff in here later
 */
public class Manifest implements Module, Binders {

    private @Nullable Binders binder;

    @Override
    public Binders forwardedBinder() {
        return binder();
    }

    protected Binders binder() {
        if(binder == null) {
            throw new IllegalStateException("Binder is only usable during configuration");
        }
        return binder;
    }

    @Override
    public void configure(Binder binder) {
        this.binder = Binders.wrap(binder.skipSources(Manifest.class));
        try {
            configure();
        } finally {
            this.binder = null;
        }
    }

    protected void configure() {}
}
