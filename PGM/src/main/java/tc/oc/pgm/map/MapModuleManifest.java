package tc.oc.pgm.map;

import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.inject.MembersInjector;
import tc.oc.commons.core.reflect.Types;
import tc.oc.pgm.map.inject.MapScoped;
import tc.oc.pgm.match.MatchModuleFactory;
import tc.oc.pgm.match.inject.MatchModuleFactoryManifest;
import tc.oc.pgm.module.ModuleLoadException;
import tc.oc.pgm.module.ModuleManifest;

/**
 * Configures a {@link MapModule} that is created by a {@link MapModuleParser}.
 *
 * {@link MapModuleParser#parse} can return null to omit the module.
 */
public abstract class MapModuleManifest<M extends MapModule> extends ModuleManifest<MapModule, MapScoped, MapModuleContext, M> {

    protected MapModuleManifest() {
        super(null);
    }

    @Override
    protected void configure() {
        super.configure();

        // Eagerly acquire the parser
        parser = parser();

        // If the MapModule is also a MatchModuleFactory, configure that as well.
        if(Types.isAssignable(MatchModuleFactory.class, type)) {
            install(new MatchModuleFactoryManifest(key));
        }
    }

    protected abstract MapModuleParser<M> parser();

    private MapModuleParser<M> parser;

    private @Inject MembersInjector<M> injector;
    private @Inject Provider<MapModuleContext> contextProvider;

    @Override
    protected Optional<M> provisionModuleWithoutDependencies() throws ModuleLoadException {
        final MapModuleContext context = contextProvider.get();
        final M module = parser.parse(context, context.logger(), context.xmlDocument());
        if(module != null) {
            injector.injectMembers(module);
        }
        return Optional.ofNullable(module);
    }
}
