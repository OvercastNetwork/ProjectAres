package tc.oc.pgm.module;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Provider;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.ProviderWithDependencies;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.inject.Keys;
import tc.oc.commons.core.reflect.ResolvableType;
import tc.oc.commons.core.reflect.Types;
import tc.oc.pgm.match.inject.MatchBinders;

/**
 * Common manifest for map/match modules.
 *
 * Binds {@link Module} and {@link Optional< Module >} to a provider that ultimately calls
 * {@link #provisionModuleWithoutDependencies()} to provision the module instance.
 * The optional binding is whatever that method returns, and the direct binding
 * throws an exception if the optional is not present. The scoping for these
 * bindings is determined from the {@link Scope} parameter.
 *
 * Before trying to provision the module itself, any explicit dependencies
 * in a {@link ModuleDescription} are provisioned. If any necessary dependencies
 * do not load then no attempt is made to provision this module.
 *
 * @param <Base> Base type for all modules in the scope
 * @param <Scope> Scope annotation type
 * @param <Context> Context type
 * @param <Module> Type of this module
 */
public abstract class ModuleManifest<
    Base,
    Scope extends Annotation,
    Context extends ModuleContext<Base, Scope>,
    Module extends Base
    > extends HybridManifest implements MatchBinders {

    protected final TypeLiteral<Module> type;
    protected final Class<Module> rawType;
    protected final Class<Scope> scope;
    protected final String simpleName;
    protected final Key<Module> key;
    protected final Key<Optional<Module>> optionalKey;
    protected final Key<ProvisionWrapper<Module>> wrapperKey;
    protected final Key<Context> contextKey;
    protected final Key<ProvisionWrapper<? extends Base>> setElementKey;

    public ModuleManifest(@Nullable TypeLiteral<Module> type) {
        // Figure out the module type. If the given type is null,
        // then try to resolve it from this object's superclass.
        this.type = type != null ? Types.assertFullySpecified(type)
                                 : new ResolvableType<Module>(){}.in(getClass());
        this.rawType = (Class<Module>) this.type.getRawType();
        this.simpleName = rawType.getSimpleName();

        // Resolve the scope type automatically
        this.scope = (Class<Scope>) new ResolvableType<Scope>(){}.in(getClass()).getRawType();

        // Construct various keys related to the module/scope
        this.key = Key.get(this.type);
        this.optionalKey = Keys.optional(this.key);
        this.wrapperKey = ProvisionWrapper.keyOf(this.type);
        this.contextKey = Key.get(new ResolvableType<Context>(){}.in(getClass()));
        this.setElementKey = Key.get(new ResolvableType<ProvisionWrapper<? extends Base>>(){}.in(getClass()));
    }

    @Override
    protected void configure() {
        // Inject this object. A few subclasses rely on this.
        requestInjection(this);

        // Register the module in the big list of all modules
        inSet(setElementKey).addBinding().to(wrapperKey);

        // Bind the wrapper to the provider that does everything
        bind(wrapperKey).toProvider(new WrapperProvider()).in(scope);

        // Link M and Optional<M> to the wrapper
        final Provider<ProvisionWrapper<Module>> wrapperProvider = getProvider(wrapperKey);
        bind(optionalKey).toProvider(() -> wrapperProvider.get().optional()).in(scope);
        bind(key).toProvider(() -> wrapperProvider.get().require(null)).in(scope);
    }

    /**
     * Provisions any the explicit dependencies of the module (from a {@link ModuleDescription} annotation),
     * followed by the module itself.
     *
     * Also implements {@link com.google.inject.spi.HasDependencies}, though I have no idea
     * if that does anything useful.
     *
     * TODO: Replace all explicit dependencies with plain old injections so we don't need
     * all this complex reflection and logic.
     *
     * @see ModuleDescription
     * @see ProvisionWrapper
     */
    private class WrapperProvider implements ProviderWithDependencies<ProvisionWrapper<Module>> {

        final Provider<Context> contextProvider = getProvider(contextKey);

        final Set<Dependency<?>> dependencies = new HashSet<>();
        final List<Provider<? extends ProvisionWrapper<?>>> requires = new ArrayList<>();
        final List<Provider<? extends ProvisionWrapper<?>>> depends = new ArrayList<>();
        final List<Provider<? extends ProvisionWrapper<?>>> follows = new ArrayList<>();

        WrapperProvider() {
            final ModuleDescription annotation = type.getRawType().getAnnotation(ModuleDescription.class);
            if(annotation != null) {
                for(Class<?> cls : annotation.requires()) {
                    addDependency(requires, cls);
                }
                for(Class<?> cls : annotation.depends()) {
                    addDependency(depends, cls);
                }
                for(Class<?> cls : annotation.follows()) {
                    addDependency(follows, cls);
                }
            }
        }

        <T> void addDependency(List<Provider<? extends ProvisionWrapper<?>>> list, Class<T> module) {
            final Key<ProvisionWrapper<T>> key = ProvisionWrapper.keyOf(TypeLiteral.get(module));
            final Provider<ProvisionWrapper<T>> provider = getProvider(key);
            dependencies.add(Dependency.get(key));
            list.add(provider);
        }

        @Override
        public Set<Dependency<?>> getDependencies() {
            return dependencies;
        }

        @Override
        public ProvisionWrapper<Module> get() {
            // Note that all dependencies are provisioned, in the same order,
            // regardless of any intermediate result. This keeps the loading
            // process predictable, even when there are errors.

            boolean failed = false;
            boolean absent = false;

            // Required modules - These are expected to be present
            for(Provider<? extends ProvisionWrapper<?>> provider : requires) {
                final ProvisionWrapper<?> wrapper = provider.get();
                switch(wrapper.result) {
                    case FAILED:
                        failed = true;
                        break;
                    default:
                        wrapper.require(type);
                        break;
                }
            }

            // Chained module - If any of these are absent, so are we
            for(Provider<? extends ProvisionWrapper<?>> provider : depends) {
                final ProvisionWrapper<?> wrapper = provider.get();
                switch(wrapper.result) {
                    case FAILED:
                        failed = true;
                        break;
                    case ABSENT:
                        absent = true;
                        break;
                }
            }

            // Prior module - Provision these first, but we don't care if they are absent
            for(Provider<? extends ProvisionWrapper<?>> provider : follows) {
                final ProvisionWrapper<?> wrapper = provider.get();
                switch(wrapper.result) {
                    case FAILED:
                        failed = true;
                        break;
                }
            }

            if(absent) {
                // Decline to load because an upstream module also declined
                return new ProvisionWrapper<>(type, ProvisionResult.ABSENT);
            } else if(failed) {
                // Fail because an upstream module failed
                return new ProvisionWrapper<>(type, ProvisionResult.FAILED);
            } else {
                try {
                    // Load this module and add it to the context (if present)
                    return contextProvider.get()
                                          .loadModule(ModuleManifest.this::provisionModuleWithoutDependencies)
                                          .map(module -> new ProvisionWrapper<>(type, ProvisionResult.PRESENT, module))
                                          .orElseGet(() -> new ProvisionWrapper<>(type, ProvisionResult.ABSENT));
                } catch(UpstreamProvisionFailure e) {
                    // Upstream module failed, we don't have to report this one failing too
                    return new ProvisionWrapper<>(type, ProvisionResult.FAILED);
                }
            }
        }
    }

    /**
     * Provision the module, assuming explicit dependencies have already been provisioned
     *
     * @return The module instance, or empty if the module declines to load
     *
     * @throws ModuleLoadException If a *user* error occurred while loading the module i.e.
     *                             something that should be reported to the mapmaker.
     *                             The error is reported to the {@link ModuleContext} and
     *                             loading continues, skipping any modules dependent on this one.
     */
    protected abstract Optional<Module> provisionModuleWithoutDependencies() throws ModuleLoadException;
}
