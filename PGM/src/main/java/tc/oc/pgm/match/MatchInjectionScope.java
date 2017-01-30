package tc.oc.pgm.match;

import java.util.Optional;
import javax.inject.Inject;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.bukkit.World;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.inject.InjectionScopable;
import tc.oc.commons.core.inject.InjectionScope;
import tc.oc.commons.core.inject.InjectionStore;
import tc.oc.commons.core.util.Optionals;
import tc.oc.commons.core.util.ThrowingSupplier;
import tc.oc.pgm.match.inject.MatchScoped;
import tc.oc.pgm.module.ModuleLoadException;

public class MatchInjectionScope extends InjectionScope<MatchScoped> {

    @Inject private MatchFinder matchFinder;

    @Override
    protected Optional<InjectionStore<MatchScoped>> currentStore(Key<?> key) {
        return Optionals.first(
            super.currentStore(key),
            Optional.ofNullable(matchFinder)
                    .flatMap(MatchFinder::currentMatch)
                    .map(InjectionScopable::injectionStore)
        );
    }

    public <T> T withNewStore(World world, ThrowingSupplier<T, ModuleLoadException> block) throws ModuleLoadException {
        final InjectionStore<MatchScoped> store = new InjectionStore<>();
        store.store(Key.get(World.class), world);
        return withCurrentStore(store, block);
    }

    public class Manifest extends HybridManifest {
        @Override
        protected void configure() {
            publicBinder().bindScope(MatchScoped.class, MatchInjectionScope.this);

            bind(scopeKey()).to(MatchInjectionScope.class);
            bind(MatchInjectionScope.class).toInstance(MatchInjectionScope.this);

            bindSeeded(binder(), storeKey());
            bindSeeded(binder(), Key.get(World.class));
        }
    }
}
