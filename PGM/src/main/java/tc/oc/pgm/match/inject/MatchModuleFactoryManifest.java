package tc.oc.pgm.match.inject;

import java.util.Optional;
import javax.inject.Provider;

import com.google.inject.Key;
import com.google.inject.MembersInjector;
import tc.oc.commons.core.inject.Injection;
import tc.oc.commons.core.inject.Keys;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchModuleFactory;

/**
 * Manifest for a {@link MatchModule} that is created by a {@link MatchModuleFactory}
 */
public class MatchModuleFactoryManifest<M extends MatchModule, F extends MatchModuleFactory<M>> extends MatchModuleManifest<M> {

    private final Key<Optional<F>> factoryKey;

    public MatchModuleFactoryManifest(Key<F> factoryKey) {
        super(MatchModuleFactory.matchModuleType(factoryKey.getTypeLiteral()));
        this.factoryKey = Keys.optional(factoryKey);
    }

    @Override
    protected void configure() {
        super.configure();

        factoryProvider = getProvider(factoryKey);
        membersInjector = getMembersInjector(type);
    }

    private Provider<Optional<F>> factoryProvider;
    private MembersInjector<M> membersInjector;

    @Override
    protected Optional<M> provisionModuleWithoutFiltering() {
        return factoryProvider.get().flatMap(Injection.wrappingExceptions(factory -> {
            final M module = factory.createMatchModule(matchProvider.get());
            if(module != null) {
                membersInjector.injectMembers(module);
            }
            return Optional.ofNullable(module);
        }));
    }
}
