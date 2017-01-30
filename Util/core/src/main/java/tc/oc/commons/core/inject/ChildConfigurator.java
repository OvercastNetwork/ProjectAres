package tc.oc.commons.core.inject;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import tc.oc.commons.core.reflect.ResolvableType;
import tc.oc.commons.core.reflect.TypeArgument;

/**
 * Collects {@link Module}s to be used to create child {@link Injector}s
 * with a {@link ChildInjectorFactory} of the same type {@link T}. The type
 * is only used as a key to distinguish different configurations.
 *
 * The modules are stored in a bound container that the {@link ChildInjectorFactory}
 * depends on. The modules are installed directly into the child injector,
 * as if they were passed to {@link Injector#createChildInjector}.
 *
 * TODO: Reproduce the original module path in error messages from the child injector.
 * Guice doesn't seem to have any way to inspect or override module sources, so I have
 * no idea how this could be done.
 *
 * It would also be good to verify the modules up-front, at least partially.
 * Currently, no verification is done at all until the child injector is created.
 */
public class ChildConfigurator<T> {

    private final Key<T> key;
    private final TypeArgument<T> typeArg;
    private final Key<ChildModule<T>> childModuleKey;
    private final Multibinder<ChildModule<T>> deferredModules;

    public ChildConfigurator(Binder binder, Class<T> type) {
        this(binder, Key.get(type));
    }

    public ChildConfigurator(Binder binder, TypeLiteral<T> type) {
        this(binder, Key.get(type));
    }

    public ChildConfigurator(Binder binder, Key<T> key) {
        this.key = key;
        this.typeArg = new TypeArgument<T>(this.key.getTypeLiteral()){};
        this.childModuleKey = this.key.ofType(new ResolvableType<ChildModule<T>>(){}.with(typeArg));
        this.deferredModules = Multibinder.newSetBinder(binder, childModuleKey);
    }

    public void install(Module module) {
        deferredModules.addBinding().toInstance(new ChildModule<>(module));
    }
}

class ChildModule<T> implements Module {
    private final Module module;

    ChildModule(Module module) {
        this.module = module;
    }

    @Override
    public void configure(Binder binder) {
        // TODO: Figure out how to show the original module path in error messages.
        // Guice doesn't seem to have any way to override module sources.
        binder.skipSources(ChildModule.class)
              .install(module);
    }

    @Override
    public int hashCode() {
        return module.hashCode();
    }

    @Override
    public boolean equals(Object that) {
        return that instanceof ChildModule &&
               this.module.equals(((ChildModule) that).module);
    }
}