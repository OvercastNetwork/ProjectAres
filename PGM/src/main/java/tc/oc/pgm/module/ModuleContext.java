package tc.oc.pgm.module;

import com.google.inject.Injector;
import com.google.inject.Key;
import tc.oc.commons.core.inject.Injection;
import tc.oc.commons.core.inject.InjectionScopable;
import tc.oc.commons.core.inject.InjectionScope;
import tc.oc.commons.core.inject.InjectionStore;
import tc.oc.commons.core.inject.Keys;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.util.ThrowingRunnable;
import tc.oc.commons.core.util.ThrowingSupplier;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Loads and stores modules descended from some base type {@link Base}, as well as the
 * {@link InjectionStore} for a scope with annotation {@link Scope}.
 *
 * This is mostly a relic of the old module system and doesn't do much anymore. It
 * just provisions all known modules at load time and keeps a list of the ones
 * that loaded.
 *
 * The module getter methods in this class should NOT be used by new code. All they
 * do is delegate to the {@link Injector}, and anything they provide can just be
 * injected directly.
 */
public abstract class ModuleContext<Base, Scope extends Annotation> implements ModuleExceptionHandler, InjectionScopable<Scope> {

    @Inject protected Injector injector;
    @Inject protected InjectionScope<Scope> injectionScope;
    @Inject protected InjectionStore<Scope> injectionStore;
    @Inject protected Collection<Provider<ProvisionWrapper<? extends Base>>> moduleProviders;

    protected Logger logger;
    @Inject void initLogger(Loggers loggers) {
        logger = loggers.get(getClass());
    }

    private final List<Base> loadedModules = new ArrayList<>();
    private final List<ModuleLoadException> errors = new ArrayList<>();

    public Logger logger() {
        return logger;
    }

    @Override
    public InjectionScope<Scope> injectionScope() {
        return injectionScope;
    }

    @Override
    public InjectionStore<Scope> injectionStore() {
        return injectionStore;
    }

    public void load() {
        asCurrentScope(() -> moduleProviders.forEach(Provider::get));
    }

    /**
     * Return all modules in dependency order
     */
    public Collection<Base> loadedModules() {
        return loadedModules;
    }

    private <T> T getInstance(Key<T> key) {
        // Check the store directly first, so we can avoid a scope change most of the time
        return injectionStore.provide(key, () ->
            this.<T, RuntimeException>asCurrentScope(
                () -> injector.getInstance(key)
            )
        );
    }

    public boolean hasModule(Class<? extends Base> type) {
        return module(type).isPresent();
    }

    public <M extends Base> Optional<M> module(Class<M> type) {
        return getInstance(Keys.optional(type));
    }

    public @Nullable <M extends Base> M getModule(Class<M> type) {
        return module(type).orElse(null);
    }

    public <M extends Base> M needModule(Class<M> type) {
        return getInstance(Key.get(type));
    }

    @Override
    public void propagatingFailures(ThrowingRunnable<ModuleLoadException> block) {
        try {
            Injection.unwrappingExceptions(ModuleLoadException.class, block);
        } catch(ModuleLoadException e) {
            addError(e);
            throw new UpstreamProvisionFailure();
        }
    }

    @Override
    public <T> T propagatingFailures(ThrowingSupplier<T, ModuleLoadException> block) {
        try {
            return Injection.unwrappingExceptions(ModuleLoadException.class, block);
        } catch(ModuleLoadException e) {
            addError(e);
            throw new UpstreamProvisionFailure();
        }
    }

    @Override
    public void ignoringFailures(ThrowingRunnable<ModuleLoadException> block) {
        try {
            propagatingFailures(block);
        } catch(UpstreamProvisionFailure ignored) {}
    }

    @Override
    public <T> Optional<T> ignoringFailures(ThrowingSupplier<T, ModuleLoadException> block) {
        try {
            return propagatingFailures(() -> Optional.of(block.getThrows()));
        } catch(UpstreamProvisionFailure ignored) {
            return Optional.empty();
        }
    }

    <M extends Base> Optional<M> loadModule(ThrowingSupplier<Optional<M>, ModuleLoadException> loader) {
        final Optional<M> module = propagatingFailures(loader);
        module.ifPresent(this::addModule);
        return module;
    }

    protected void addModule(Base module) {
        loadedModules.add(module);
    }

    public List<ModuleLoadException> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    protected void addError(ModuleLoadException e) {
        this.errors.add(e);
    }

    protected void addErrors(Collection<? extends ModuleLoadException> errors) {
        this.errors.addAll(errors);
    }
}
