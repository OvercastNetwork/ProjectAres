package tc.oc.pgm.map.inject;

import java.util.Optional;
import javax.inject.Inject;

import com.google.inject.Key;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.inject.InjectionScope;
import tc.oc.commons.core.inject.InjectionStore;
import tc.oc.commons.core.util.Optionals;
import tc.oc.commons.core.util.ThrowingSupplier;
import tc.oc.pgm.map.MapDefinition;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchInjectionScope;
import tc.oc.pgm.module.ModuleLoadException;

public class MapInjectionScope extends InjectionScope<MapScoped> {

    @Inject private MatchInjectionScope matchScope;

    @Override
    protected Optional<InjectionStore<MapScoped>> currentStore(Key key) {
        // If there is no explicit map in scope, try to get one from the current Match
        return Optionals.first(
            super.currentStore(key),
            Optional.ofNullable(matchScope)
                    .flatMap(scope -> scope.currentInstance(Match.class))
                    .map(match -> match.getModuleContext().injectionStore())
        );
    }

    public <T> T withNewStore(MapDefinition map, ThrowingSupplier<T, ModuleLoadException> block) throws ModuleLoadException {
        final InjectionStore<MapScoped> store = new InjectionStore<>();
        store.store(Key.get(MapDefinition.class), map);
        return withCurrentStore(store, block);
    }

    public class Manifest extends HybridManifest {
        @Override
        protected void configure() {
            publicBinder().bindScope(MapScoped.class, MapInjectionScope.this);

            bind(scopeKey()).to(MapInjectionScope.class);
            bind(MapInjectionScope.class).toInstance(MapInjectionScope.this);

            bindSeeded(binder(), storeKey());
            bindSeeded(binder(), Key.get(MapDefinition.class));
        }
    }
}
