package tc.oc.commons.core.inject;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.inject.MembersInjector;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * Detects repeated member injection of the same object
 *
 * Guice does nothing to prevent {@link MembersInjector#injectMembers(Object)}
 * from being called on the same object multiple times, which can easily have
 * unwanted side-effects.
 *
 * This module installs a custom universal {@link MembersInjector} that can
 * detect repeat application to the same object. To do this, it must hold a
 * weak reference to every injected object indefinitely, so this should not
 * be used in a production environment.
 */
public class RepeatInjectionDetector extends SingletonManifest {
    @Override
    protected void configure() {
        final ReferenceQueue<Object> queue = new ReferenceQueue<>();
        final Set<Object> injected = Collections.newSetFromMap(new IdentityHashMap<>());
        final AtomicBoolean running = new AtomicBoolean(true);

        final Thread reaper = new Thread(() -> {
            while(running.get()) {
                try {
                    final Object instance = queue.remove().get();
                    if(instance != null) synchronized(injected) {
                        injected.remove(instance);
                    }
                } catch(InterruptedException e) {
                    // ignored
                }
            }
        });
        reaper.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running.set(false);
            reaper.interrupt();
        }));

        bindListener(com.google.inject.matcher.Matchers.any(), new TypeListener() {
            @Override
            public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                encounter.register(new MembersInjector<I>() {
                    @Override
                    public void injectMembers(I instance) {
                        final boolean added;
                        synchronized(injected) {
                            added = injected.add(instance);
                        }
                        if(added) {
                            new WeakReference<>(instance, queue);
                        } else {
                            throw new ProvisionException("Multiple injections for " + instance);
                        }
                    }
                });
            }
        });
    }
}
