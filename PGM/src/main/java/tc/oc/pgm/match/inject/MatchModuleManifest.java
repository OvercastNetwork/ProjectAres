package tc.oc.pgm.match.inject;

import java.util.Optional;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.inject.TypeLiteral;
import org.bukkit.event.Listener;
import tc.oc.api.connectable.Connector;
import tc.oc.api.annotations.ApiRequired;
import tc.oc.commons.core.reflect.Types;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchModuleContext;
import tc.oc.pgm.module.ModuleLoadException;
import tc.oc.pgm.module.ModuleManifest;

/**
 * Base manifest for configuring a {@link MatchModule}
 *
 * If the module has the @ApiRequired annotation, and there is no API connection,
 * the module is not provisioned.
 *
 * If the module returns false from its {@link MatchModule#shouldLoad()} method,
 * it is discarded.
 */
public abstract class MatchModuleManifest<M extends MatchModule> extends ModuleManifest<MatchModule, MatchScoped, MatchModuleContext, M> {

    protected final boolean apiRequired;

    protected MatchModuleManifest() {
        this(null);
    }

    protected MatchModuleManifest(@Nullable TypeLiteral<M> type) {
        super(type);
        this.apiRequired = rawType.isAnnotationPresent(ApiRequired.class);
    }

    @Override
    protected void configure() {
        super.configure();

        // Register the module as a Listener if it is one
        if(Types.isAssignable(Listener.class, type)) {
            matchOptionalListener((Class<? extends Listener>) rawType);
        }
    }

    @Inject protected Provider<Match> matchProvider;
    @Inject private Connector connector;

    @Override
    protected final Optional<M> provisionModuleWithoutDependencies() throws ModuleLoadException {
        if(apiRequired && !connector.isConnected()) {
            return Optional.empty();
        }
        return provisionModuleWithoutFiltering()
            .filter(MatchModule::shouldLoad);
    }

    protected abstract Optional<M> provisionModuleWithoutFiltering() throws ModuleLoadException;
}
