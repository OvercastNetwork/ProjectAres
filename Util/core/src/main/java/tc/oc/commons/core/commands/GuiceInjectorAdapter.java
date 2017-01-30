package tc.oc.commons.core.commands;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.inject.Injector;

/**
 * Adapts a Guice {@link com.google.inject.Injector} to a command framework {@link com.sk89q.minecraft.util.commands.Injector}
 */
public class GuiceInjectorAdapter implements com.sk89q.minecraft.util.commands.Injector {

    private final Injector injector;

    @Inject public GuiceInjectorAdapter(Injector injector) {
        this.injector = injector;
    }

    @Override
    public <T> Provider<? extends T> getProviderOrNull(Class<T> cls) {
        return injector.getProvider(cls);
    }

    @Override
    public Object getInstance(Class<?> cls) {
        return injector.getInstance(cls);
    }
}
