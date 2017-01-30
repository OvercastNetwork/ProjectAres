package tc.oc.commons.core.inject;

import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.PrivateBinder;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import tc.oc.inject.ForwardingBinder;
import tc.oc.inject.ForwardingPrivateBinder;

public interface PrivateBinders extends Binders, ForwardingPrivateBinder {

    static PrivateBinders wrap(PrivateBinder binder) {
        if(binder instanceof PrivateBinders) {
            return (PrivateBinders) binder;
        }
        final PrivateBinder skipped = binder.skipSources(Binders.class,
                                                         PrivateBinders.class,
                                                         ForwardingBinder.class,
                                                         ForwardingPrivateBinder.class);
        return () -> skipped;
    }

    default <T> LinkedBindingBuilder<T> bindAndExpose(Key<T> key) {
        expose(key);
        return bind(key);
    }

    default <T> LinkedBindingBuilder<T> bindAndExpose(TypeLiteral<T> type) {
        return bindAndExpose(Key.get(type));
    }

    default <T> LinkedBindingBuilder<T> bindAndExpose(Class<T> type) {
        return bindAndExpose(Key.get(type));
    }

    /**
     * Expose all bindings in the given module.
     */
    default void expose(Module module) {
        ElementUtils.expose(forwardedBinder(), module);
    }

    default void installAndExpose(Module module) {
        install(module);
        expose(module);
    }

    @Override
    default PrivateBinders withSource(Object source) {
        return wrap(ForwardingPrivateBinder.super.withSource(source));
    }

    @Override
    default PrivateBinders skipSources(Class... classesToSkip) {
        return wrap(ForwardingPrivateBinder.super.skipSources(classesToSkip));
    }
}
