package tc.oc.pgm.map;

import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.ScopedBindingBuilder;
import tc.oc.commons.core.inject.Injection;
import tc.oc.commons.core.inject.KeyedManifest;
import tc.oc.pgm.map.inject.MapBinders;
import tc.oc.pgm.map.inject.MapScoped;
import tc.oc.pgm.xml.InvalidXMLException;

/**
 * Registers a {@link MapRootParser} that just provisions the given {@link Key}
 * in its {@link MapRootParser#parse()} method.
 *
 * This is roughly equivalent to {@link ScopedBindingBuilder#asEagerSingleton()},
 * but for {@link MapScoped} instead of {@link Singleton}.
 */
public class ProvisionAtParseTime<T> extends KeyedManifest implements MapBinders {

    private final Key<T> key;

    public ProvisionAtParseTime(Class<T> type) {
        this(Key.get(type));
    }

    public ProvisionAtParseTime(TypeLiteral<T> type) {
        this(Key.get(type));
    }

    public ProvisionAtParseTime(Key<T> key) {
        this.key = key;
    }

    @Override
    protected Object manifestKey() {
        return key;
    }

    @Override
    protected void configure() {
        final Provider<T> provider = getProvider(key);
        rootParsers().addBinding().toInstance(
            () -> Injection.unwrappingExceptions(InvalidXMLException.class, provider)
        );
    }
}
