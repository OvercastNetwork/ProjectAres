package tc.oc.commons.core.commands;

import javax.annotation.Nullable;
import javax.inject.Provider;

/**
 * Platform-neutral interface used to register classes containing commands.
 *
 * Generally, you should use a {@link CommandBinder} to register command classes,
 * rather than using this interface directly.
 */
public interface CommandRegistry {

    /**
     * Register the given command class.
     *
     * At startup, the command framework will scan the class for command methods,
     * but will not yet try to provision an instance of the class.
     *
     * If a {@link Provider} is given, then the command framework will use it to
     * provision an instance of the command class <em>every time the command
     * is executed</em>, which allows is to be scoped. If you want a single instance
     * of the class to be reused forever, then it should be bound in @Singleton scope.
     *
     * If the provider is null, and the class contains non-static command methods,
     * then the command framework will try to get a provider from the Guice Injector,
     * so a binding must exist for the exact {@code clazz} that is registered.
     * Any nested command classes will be provisioned in the same way.
     *
     * The binding can provide a subclass of {@link T} if desired, but only methods on
     * {@code clazz} will be seen by the reflective scanner.
     */
    <T> void register(Class<T> clazz, @Nullable Provider<? extends T> provider);

    default void register(Class<?> clazz) {
        register(clazz, null);
    }
}
