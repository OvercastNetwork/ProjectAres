package tc.oc.commons.core.commands;

import javax.annotation.Nullable;
import javax.inject.Provider;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.multibindings.Multibinder;

/**
 * Binds command {@link Class}es to be registered with the {@link CommandRegistry}.
 */
public class CommandBinder {

    private final Binder binder;
    private final Multibinder<Binding> bindings;

    public CommandBinder(Binder binder) {
        this.binder = binder;
        this.bindings = Multibinder.newSetBinder(binder, Binding.class);
    }

    /**
     * Register the given command {@link Class} with the command framework.
     * If the class has any non-static command methods, then a binding must
     * exist for the exact class given.
     */
    public <T> void register(Class<T> type) {
        register(type, (Provider<T>) null);
    }

    /**
     * Register the given command {@link Class} with the command framework,
     * and use the given {@link Key} to provision instances of it from the
     * Injector.
     */
    public <T> void register(Class<T> type, Key<? extends T> key) {
        register(type, binder.getProvider(key));
    }

    /**
     * Register the given command {@link Class} with the command framework,
     * and use the given {@link Provider} to provision instances of it.
     */
    public <T> void register(Class<T> type, @Nullable Provider<? extends T> provider) {
        bindings.addBinding().toInstance(new Binding(type, provider));
    }

    static class Binding<T> {
        final Class<T> type;
        final @Nullable Provider<? extends T> provider;

        private Binding(Class<T> type, @Nullable Provider<? extends T> provider) {
            this.type = type;
            this.provider = provider;
        }
    }
}
