package tc.oc.commons.core.inject;

import java.util.Arrays;
import java.util.Set;
import javax.inject.Inject;

import com.google.common.collect.Iterables;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Creates child {@link Injector}s configured by a {@link ChildConfigurator}s
 * of the same type {@link T}.
 *
 * The default parent injector is whatever Guice passes to the @Inject constructor.
 * If you care what that is, be sure to bind this factory explicitly in the injector
 * that you want as the parent. If you let it be a JIT binding, the parent injector
 * can be unpredictable.
 */
public class ChildInjectorFactory<T> {

    private final Injector parent;
    private final Set<ChildModule<T>> modules;

    @Inject private ChildInjectorFactory(Injector parent, Set<ChildModule<T>> modules) {
        this.parent = parent;
        this.modules = modules;
    }

    public Injector createChildInjector(Module... modules) {
        return createChildInjector(parent, modules);
    }

    public Injector createChildInjector(Iterable<? extends Module> modules) {
        return createChildInjector(parent, modules);
    }

    public Injector createChildInjector(Injector parent, Module... modules) {
        return createChildInjector(parent, Arrays.asList(modules));
    }

    public Injector createChildInjector(Injector parent, Iterable<? extends Module> modules) {
        return parent.createChildInjector(Iterables.concat(this.modules, modules));
    }
}
