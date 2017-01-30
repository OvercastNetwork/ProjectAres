package tc.oc.commons.core.inject;

import tc.oc.inject.ForwardingBinder;
import tc.oc.inject.ForwardingPrivateBinder;
import tc.oc.inject.ForwardingProtectedBinder;
import tc.oc.inject.ProtectedBinder;

public interface ProtectedBinders extends PrivateBinders, ForwardingProtectedBinder {

    static ProtectedBinders wrap(ProtectedBinder binder) {
        if(binder instanceof ProtectedBinders) {
            return (ProtectedBinders) binder;
        }
        final ProtectedBinder skipped = binder.skipSources(Binders.class,
                                                           PrivateBinders.class,
                                                           ProtectedBinders.class,
                                                           ForwardingBinder.class,
                                                           ForwardingPrivateBinder.class,
                                                           ForwardingProtectedBinder.class);
        return () -> skipped;
    }

    @Override
    default Binders publicBinder() {
        return Binders.wrap(ForwardingProtectedBinder.super.publicBinder());
    }

    @Override
    default ProtectedBinders withSource(Object source) {
        return wrap(ForwardingProtectedBinder.super.withSource(source));
    }

    @Override
    default ProtectedBinders skipSources(Class[] classesToSkip) {
        return wrap(ForwardingProtectedBinder.super.skipSources(classesToSkip));
    }
}
