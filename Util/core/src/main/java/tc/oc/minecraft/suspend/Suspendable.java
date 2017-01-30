package tc.oc.minecraft.suspend;

import tc.oc.commons.core.util.ThrowingRunnable;

/**
 * Something that reacts to the server going into a suspended state.
 *
 * Implementors can override {@link #suspend()} and/or {@link #resume()}
 * to be called before/after suspension, or they can override
 * {@link #suspend(ThrowingRunnable)} to be called around suspension.
 *
 * Suspendables are registered through a {@link SuspendableBinder}.
 */
public interface Suspendable {

    /**
     * Called when the server suspends. MUST call yielder.
     */
    default <X extends Throwable> void suspend(ThrowingRunnable<X> yielder) throws X {
        suspend();
        try {
            yielder.runThrows();
        } finally {
            resume();
        }
    }

    default void suspend() {}

    default void resume() {}
}
