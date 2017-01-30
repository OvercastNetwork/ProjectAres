package tc.oc.pgm.match.inject;

import java.util.Optional;
import javax.annotation.Nullable;

import com.google.inject.TypeLiteral;
import tc.oc.commons.core.inject.InjectingFactory;
import tc.oc.pgm.match.MatchModule;

/**
 * Configures a {@link MatchModule} that loads for every match.
 *
 * The transforms bound in the superclass manifests can still
 * prevent the module from loading under certain conditions.
 */
public abstract class MatchModuleFixtureManifest<M extends MatchModule> extends MatchModuleManifest<M> {

    protected MatchModuleFixtureManifest() {
        this(null);
    }

    public MatchModuleFixtureManifest(@Nullable TypeLiteral<M> type) {
        super(type);
    }

    @Override
    protected void configure() {
        super.configure();
        factory = new InjectingFactory<>(binder(), type);
    }

    private InjectingFactory<M> factory;

    protected boolean shouldLoad() {
        return true;
    }

    @Override
    protected Optional<M> provisionModuleWithoutFiltering() {
        return shouldLoad() ? Optional.of(factory.get())
                            : Optional.empty();
    }
}
